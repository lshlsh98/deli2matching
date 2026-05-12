package com.example.deli2matching.dao;


import com.example.deli2matching.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserMapper - JpaRepository 대신 MyBatis @Mapper 인터페이스
 * 실제 SQL은 resources/mapper/UserMapper.xml에 정의
 */
@Mapper
public interface UserDao {

    // 사용자 저장 (INSERT)
    void insert(UserEntity userEntity);

    // username으로 사용자 조회
    UserEntity findByLoginId(String loginId);

    // username 존재 여부 확인
    boolean existsByLoginId(String LoginId);

    // id로 사용자 조회
    UserEntity findById(Long id);

}
