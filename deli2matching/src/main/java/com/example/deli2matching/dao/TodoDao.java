package com.example.deli2matching.dao;

import com.example.deli2matching.entity.TodoEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * TodoMapper - JpaRepository 대신 MyBatis @Mapper 인터페이스
 * 실제 SQL은 resources/mapper/TodoMapper.xml에 정의
 */
@Mapper
public interface TodoDao {

    // Todo 저장 (INSERT), 생성된 PK를 entity.id에 자동 세팅 (useGeneratedKeys)
    void insert(TodoEntity todoEntity);

    // userId로 Todo 목록 조회
    List<TodoEntity> findByUserId(Long userId);

    // id로 Todo 단건 조회
    TodoEntity findById(Long id);

    // Todo 수정 (UPDATE)
    void update(TodoEntity todoEntity);

    // Todo 삭제 (DELETE)
    void delete(Long id);

}
