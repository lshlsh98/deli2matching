package com.example.deli2matching.security.vo;


import com.example.deli2matching.security.dto.OAuthAttributes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;

/**
 * CustomUser - 소셜 로그인 사용자 정보 객체
 *
 * 스프링 시큐리티의 DefaultOAuth2User를 확장해서 만든 커스텀 사용자 클래스
 *
 * 왜 커스텀이 필요한가?
 *  DefaultOAuth2User의 getName()은 소셜 플랫폼의 ID(sub, 또는 login 등)를 반환함
 *  하지만 우리는 JWT 토큰에 "우리 DB의 user id"를 담아야 함
 *  그래서 getName()을 오버라이드해서 DB id를 반환하도록 함
 *
 * 이 객체가 어디서 쓰이나요?
 *  - CustomOAuth2UserService.loadUser()에서 생성
 *  - Authentication.getPrincipal()로 접근 가능
 *  - OAuthSuccessHandler에서 tokenProvider.create(authentication) 호출 시 사용
 *    → TokenProvider.create(Authentication)에서 authentication.getPrincipal()로 꺼냄
 *    → userPrincipal.getName()이 DB id를 반환 → JWT subject = DB user id
 *
 * DefaultOAuth2User: 스프링이 제공하는 OAuth2 사용자 기본 클래스
 * =====================================================================
 */
public class CustomUser extends DefaultOAuth2User {

    // 직렬화를 위한 버전 ID (Serializable 구현 시 필요)
    private static final long serialVersionUID = 1L;

    /**
     * id - 우리 DB의 user_entity 테이블 기본 키 (PK)
     * 소셜 플랫폼의 ID가 아닌, 우리 데이터베이스에서 부여한 ID입니다.
     * JWT 토큰의 subject(subject)로 사용됩니다.
     */
    private Long id;

    /** email - 소셜 플랫폼에서 가져온 이메일 */
    private String email;

    /** username - 소셜 플랫폼에서 가져온 이름/닉네임 */
    private String username;

    /**
     * 생성자
     *
     * @param id          우리 DB의 user id (JWT subject로 사용됨)
     * @param email       소셜 로그인 이메일
     * @param username    소셜 로그인 이름
     * @param authorities 권한 목록 (예: ROLE_USER)
     * @param attributes  소셜 플랫폼 원본 attributes (OAuthAttributes 포함)
     */
    public CustomUser(Long id, String email, String username,
                      Collection<? extends GrantedAuthority> authorities,
                      OAuthAttributes attributes) {
        // 부모 클래스 DefaultOAuth2User에 권한, 원본attributes, nameAttributeKey 전달
        super(authorities, attributes.getAttributes(), attributes.getNameAttributeKey());

        this.id = id;
        this.email = email;
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    /**
     * getName() - 핵심 오버라이드 메서드!
     *
     * DefaultOAuth2User의 getName()은 nameAttributeKey에 해당하는 소셜 플랫폼 ID를 반환합니다.
     * 우리는 이것을 오버라이드해서 "우리 DB의 user id"를 반환합니다.
     *
     * 왜 중요한가?
     *  TokenProvider.create(Authentication)에서:
     *    CustomUser userPrincipal = (CustomUser) authentication.getPrincipal();
     *    .setSubject(userPrincipal.getName()) ← 이 getName()이 DB id를 반환
     *
     *  JwtAuthenticationFilter에서:
     *    String userId = tokenProvider.validateAndGetUserId(token);
     *    (사용자 ID를 꺼내서 @AuthenticationPrincipal로 컨트롤러에 전달)
     *
     * @return 우리 DB의 user id (Long을 String으로 변환)
     */
    @Override
    public String getName() {
        return "" + this.id; // DB의 user id를 문자열로 반환
    }

}

