package com.example.deli2matching.security.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Getter
@Slf4j
@Builder
// 플랫폼마다 응답 형태가 제각각 -> OAuthattributes 객체에 필요한 정보만 뽑아서 정리하기 위해
public class OAuthAttributes {

    private Map<String, Object> attributes; // 원본 사용자 정보 맵 (소셜 플랫폼에서 받은 그대로)
    private String nameAttributeKey; // 사용자 식별 키 이름 (각 플랫폼의 사용자 ID 필드명 ex: sub, id 등)
    private String name;    // 사용자 이름 (닉네임)
    private String email;   // 이메일
    private String picture; // 프로필 사진 URL
    private String id;      // 소셜 플랫폼 내 고유 ID

    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
                                     Map<String, Object> attributes) {

        System.out.println(userNameAttributeName);

        if ("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        } else if ("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        } else if ("github".equals(registrationId)) {
            return ofGitHub("id", attributes);
        }
        // 기본값: 구글
        return ofGoogle(userNameAttributeName, attributes);
    }//

    /*
    Google 에서 제공하는 JSON 형태
    {
        "sub": "1234567890",
        "name": "홍길동",
        "email": "hong@abc.com",
        "picture": "https://profile.jpg"
    }
    */
    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        System.out.println("attributes = " + attributes);
        System.out.println("userNameAttributeName = " + userNameAttributeName);

        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .id((String) attributes.get(userNameAttributeName)) // "sub" 값
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }//

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        // "response" 키로 중첩된 Map 꺼내기
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        log.info("response = " + response);
        log.info("userNameAttributeName = " + userNameAttributeName);

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .picture((String) response.get("profile_image")) // 키 이름이 다름!
                .id((String) response.get(userNameAttributeName)) // response 안의 "id"
                .attributes(response) // response 맵을 저장 (중첩 해제)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        // 카카오 ID는 Long 타입 (구글/네이버와 다름!)
        Long id = (Long) attributes.get("id");

        // 1단계 중첩 해제
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        log.info("kakaoAccount = " + kakaoAccount);
        log.info("userNameAttributeName = " + userNameAttributeName);

        // 2단계 중첩 해제
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = (String) profile.get("nickname");
        String profileImageUrl = (String) profile.get("profile_image_url");

        String email = (String) kakaoAccount.get("email");

        return OAuthAttributes.builder()
                .name(nickname)
                .email(email)
                .picture(profileImageUrl)
                .id("" + id) // Long을 String으로 변환
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofGitHub(String userNameAttributeName, Map<String, Object> attributes) {
        log.info("attributes = " + attributes);
        log.info("userNameAttributeName = " + userNameAttributeName);

        String username = (String) attributes.get("login"); // 깃허브 사용자명
        Integer id = (Integer) attributes.get("id");       // Integer 타입 (구글/네이버는 String)
        String nickname = username;
        String profileImageUrl = (String) attributes.get("avatar_url"); // 필드명 다름!
        String email = (String) attributes.get("email");   // null일 수 있음

        return OAuthAttributes.builder()
                .name(nickname)
                .email(email)  // null이면 CustomOAuth2UserService에서 별도 API 호출로 채움
                .picture(profileImageUrl)
                .id("" + id)   // Integer를 String으로 변환
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

}
