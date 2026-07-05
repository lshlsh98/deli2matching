package com.example.deli2matching.security.vo;


import com.example.deli2matching.security.dto.OAuthAttributes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;

public class CustomUser extends DefaultOAuth2User {

    // 직렬화를 위한 버전 ID (Serializable 구현 시 필요)
    private static final long serialVersionUID = 1L;


    private Long id;
    private String email;
    private String username;

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


    @Override
    public String getName() {
        return "" + this.id; // DB의 user id를 문자열로 반환
    }

}

