package com.example.deli2matching.dao;


import com.example.deli2matching.dto.user.MyInfoReqDTO;
import com.example.deli2matching.entity.user.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {

    // 사용자 저장 (INSERT)
    void insert(UserEntity userEntity);

    // loginId로 사용자 조회
    UserEntity findByLoginId(String loginId);

    // username 존재 여부 확인
    boolean existsByLoginId(String LoginId);

    // id로 사용자 조회
    UserEntity findByUserId(Long userId);

    // memberId 존재 여부 확인
    int idExists(String loginId);

    // memberName 존재 여부 확인
    int nameExists(String memberName);

    String findLoginIdByUserId(String userId);

    void updateMyInfo(MyInfoReqDTO req);

}
