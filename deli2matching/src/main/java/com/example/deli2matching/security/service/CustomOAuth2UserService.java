package com.example.deli2matching.security.service;


import com.example.deli2matching.entity.user.UserEntity;
import com.example.deli2matching.dao.UserDao;
import com.example.deli2matching.security.dto.OAuthAttributes;
import com.example.deli2matching.security.vo.CustomUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserDao userDao;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1단계: 기본 소셜 로그인 처리 (스프링 기본 제공)
        // DefaultOAuth2UserService가 accessToken으로 소셜 플랫폼 API를 호출해서
        // 사용자 정보(attributes Map)를 가져옴
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // registrationId: 어떤 소셜 플랫폼인지 ("google", "naver", "kakao", "github")
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // userNameAttributeName: 각 플랫폼에서 사용자 고유 ID를 나타내는 키 이름
        // 예: 구글="sub", 네이버="response", 카카오="id", 깃허브="id"
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        log.info("loadUser registrationId = " + registrationId);
        log.info("loadUser userNameAttributeName = " + userNameAttributeName);

        // 2단계: 플랫폼별 데이터 구조를 OAuthAttributes로 통일
        // 각 플랫폼마다 사용자 정보의 JSON 구조가 다름
        // OAuthAttributes.of()가 플랫폼을 판별하고 통일된 형태로 변환
        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId,          // 플랫폼 이름
                userNameAttributeName,   // 사용자 ID 키 이름
                oAuth2User.getAttributes() // 소셜 플랫폼에서 받은 원본 데이터
        );

        // 통일된 OAuthAttributes에서 필요한 정보 추출
        String nameAttributeKey = attributes.getNameAttributeKey(); // 사용자 ID 필드 키
        String name     = attributes.getName();    // 이름 (닉네임)
        String email    = attributes.getEmail();   // 이메일
        String picture  = attributes.getPicture(); // 프로필 사진 URL
        String id       = attributes.getId();      // 소셜 플랫폼 내 고유 ID
        String socialType = ""; // 소셜 타입 (나중에 id 접두사로 사용)

        // 3단계: 소셜 타입 결정 및 깃허브 이메일 특별 처리
        if ("naver".equals(registrationId)) {
            socialType = "naver";
        } else if ("kakao".equals(registrationId)) {
            socialType = "kakao";
        } else if ("github".equals(registrationId)) {
            socialType = "github";

            // 깃허브 특별 처리:
            // 깃허브 사용자가 이메일을 비공개로 설정하면 기본 API에서 email이 null로 옵니다.
            // 이때 별도의 GitHub API(/user/emails)를 호출해서 이메일을 가져옵니다.
            if (email == null) {
                email = getEmailFromGitHub(userRequest.getAccessToken().getTokenValue());
            }
        } else {
            socialType = "google";
        }

        log.info("nameAttributeKey = " + nameAttributeKey);
        log.info("loadUser id = " + id);
        log.info("loadUser socialType = " + socialType);
        log.info("loadUser name = " + name);
        log.info("loadUser email = " + email);

        // null 안전 처리 (이름이나 이메일이 없는 경우 빈 문자열)
        if (name == null)  name = "";
        if (email == null) email = "";

        // 4단계: 권한 설정
        // 모든 소셜 로그인 사용자에게 ROLE_USER 권한 부여
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        authorities.add(authority);

        // 5단계: DB에 사용자 저장 또는 조회
        // username = "소셜타입_이메일" 형식으로 저장
        // 예: "google_user@gmail.com", "naver_user@naver.com"

        String loginId = socialType + "_" + id; // unique (에: google_1234567890)
        String nickname = name + "_" + socialType + "_" + id; // unique (예: 홍길동_google_1234567890)
        UserEntity userEntity;

        if (!userDao.existsByLoginId(loginId)) {
            // 처음 소셜 로그인하는 사용자: 자동 회원가입!
            userEntity = UserEntity.builder()
                    .loginId(loginId)
                    .nickname(nickname)
                    .email(email)
                    .provider(socialType)
                    .providerId(id)
                    .build();
                    // password는 null (소셜 로그인 사용자는 비밀번호 없음)

            // MyBatis로 DB에 INSERT
            // useGeneratedKeys=true 설정으로 자동 생성된 userId가 userEntity.userId에 세팅됨
            userDao.insert(userEntity);
        } else {
            // 이미 가입한 사용자: DB에서 기존 정보 조회
            userEntity = userDao.findByLoginId(loginId);
        }

        log.info("Successfully pulled user info nickname {} provider {}", nickname, socialType);

        // 6단계: CustomUser 반환
        // 스프링 시큐리티의 Authentication.getPrincipal()로 접근 가능
        // OAuthSuccessHandler에서 이 객체를 사용해 JWT 토큰을 생성합니다
        return new CustomUser(userEntity.getUserId(), email, name, authorities, attributes);
    }

    private String getEmailFromGitHub(String accessToken) {
        String url = "https://api.github.com/user/emails";

        // RestTemplate: 다른 서버의 HTTP API를 호출하는 도구
        RestTemplate restTemplate = new RestTemplate();

        // Authorization 헤더에 깃허브 AccessToken 포함
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json"); // 깃허브 API v3 형식 요청

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // GET /user/emails 호출 → 이메일 목록 반환
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

        List<Map<String, Object>> emails = response.getBody();

        // 이메일 목록에서 primary=true인 이메일 찾아서 반환
        if (emails != null) {
            for (Map<String, Object> emailData : emails) {
                if ((Boolean) emailData.get("primary")) {
                    return (String) emailData.get("email");
                }
            }
        }

        return null; // 이메일을 못 찾으면 null
    }
}
