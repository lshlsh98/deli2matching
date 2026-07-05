package com.example.deli2matching.security.vo;

import com.example.deli2matching.security.dto.OAuthAttributes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;

// OAuth2 로그인 사용자 정보 담는 클래스
// Spring Security의 DefaultOAuth2User를 확장하여 사용자 ID, 이메일, 사용자명을 추가로 보관
public class CustomUser extends DefaultOAuth2User {

    // 직렬화를 위한 버전 ID (Serializable 구현 시 필요)
    private static final long serialVersionUID = 1L;
    private Long id; // DB PK
    private String email;
    private String username;

    public CustomUser(
            Long id, // PK
            String email,
            String username,
            Collection<? extends GrantedAuthority> authorities, // 권한 목록
            OAuthAttributes attributes // OAuth 사용자 정보 DTO
            ) {

        // 부모 클래스 DefaultOAuth2User에 권한, 원본attributes, nameAttributeKey 전달
        super(authorities, attributes.getAttributes(), attributes.getNameAttributeKey());

        this.id = id;
        this.email = email;
        this.username = username;
    }//

    public String getEmail() {
        return email;
    }//

    public String getUsername() {
        return username;
    }//

    @Override
    public String getName() {
        return "" + this.id; // DB의 PK를 문자열로 반환
    }//
}

