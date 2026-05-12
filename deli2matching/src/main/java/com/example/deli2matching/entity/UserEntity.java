package com.example.deli2matching.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * UserEntity - JPA @Entity 제거, MyBatis에서는 순수 POJO로 사용
 * DB 매핑은 UserMapper.xml에서 담당
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("userEntity")
public class UserEntity {

    private Long id;
    private String loginId;
    private String password;
    private String username;
    private String role;
    private String authProvider;
}
