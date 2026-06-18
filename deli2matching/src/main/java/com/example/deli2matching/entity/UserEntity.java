package com.example.deli2matching.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("userEntity")
public class UserEntity {

    private Long userId;
    private String loginId;
    private String password;
    private String email;
    private String nickname;
    private String provider;
    private String providerId;
}
