package com.example.deli2matching.service;


import com.example.deli2matching.dao.TodoDao;
import com.example.deli2matching.entity.TodoEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * =====================================================================
 * 🔧 TodoService - 할 일 비즈니스 로직 서비스
 * =====================================================================
 *
 * TodoController와 DB(TodoMapper) 사이에서 실제 로직을 처리합니다.
 *
 * CRUD 처리:
 *  - Create: 새 할 일 추가
 *  - Read:   사용자별 할 일 목록 조회
 *  - Update: 할 일 내용 / 완료 여부 수정
 *  - Delete: 할 일 삭제
 *
 * 계층 구조:
 *  TodoController → TodoService → TodoMapper → TodoMapper.xml(SQL) → MySQL DB
 * =====================================================================
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TodoService {

    /**
     * TodoMapper: MyBatis를 통해 DB와 통신
     * 실제 SQL은 resources/mapper/TodoMapper.xml에 정의
     */
    private final TodoDao todoDao;

    /**
     * create - 새 할 일 추가
     *
     * @param entity 저장할 할 일 (userId, title, done 포함)
     * @return 이 사용자의 전체 할 일 목록 (새로 추가된 것 포함)
     */
    public List<TodoEntity> create(final TodoEntity entity) {
        validate(entity); // 유효성 검사

        // MyBatis INSERT (useGeneratedKeys로 entity.id에 자동 생성된 PK 세팅)
        todoDao.insert(entity);

        log.info("Entity Id : {} is saved.", entity.getId());

        // 저장 후 이 사용자의 전체 목록 반환
        return todoDao.findByUserId(entity.getUserId());
    }

    /**
     * validate - 입력값 유효성 검사
     *
     * entity가 null이거나 userId가 없으면 예외를 던집니다.
     * 이렇게 하면 DB에 잘못된 데이터가 저장되는 것을 방지합니다.
     */
    private void validate(final TodoEntity entity) {
        if (entity == null) {
            log.warn("Entity cannot be null.");
            throw new RuntimeException("Entity cannot be null.");
        }

        if (entity.getUserId() == null) {
            log.warn("Unknown user.");
            throw new RuntimeException("Unknown user.");
        }
    }

    /**
     * retrieve - 사용자별 할 일 목록 조회
     *
     * userId에 해당하는 할 일만 반환합니다.
     * 다른 사용자의 할 일은 절대 보이지 않습니다!
     *
     * @param userId 조회할 사용자 ID (JWT에서 추출)
     * @return 이 사용자의 할 일 목록
     */
    public List<TodoEntity> retrieve(final Long userId) {
        return todoDao.findByUserId(userId);
    }

    /**
     * update - 할 일 수정 (제목, 완료 여부)
     *
     * 처리 순서:
     *  1. 유효성 검사
     *  2. id로 기존 할 일 조회
     *  3. 제목(title)과 완료 여부(done) 업데이트
     *  4. DB에 UPDATE
     *  5. 최신 목록 반환
     *
     * @param entity 수정 내용 (id, title, done, userId 포함)
     * @return 수정 후 이 사용자의 전체 할 일 목록
     */
    public List<TodoEntity> update(final TodoEntity entity) {
        validate(entity);

        // 기존 데이터 조회 (id로 단건 조회)
        final TodoEntity original = todoDao.findById(entity.getId());

        if (original != null) {
            // 수정할 필드만 업데이트 (userId는 변경하지 않음!)
            original.setTitle(entity.getTitle());
            original.setDone(entity.isDone());
            todoDao.update(original); // SQL UPDATE 실행
        }

        return retrieve(entity.getUserId()); // 최신 목록 반환
    }

    /**
     * delete - 할 일 삭제
     *
     * @param entity 삭제할 할 일 (id, userId 포함)
     * @return 삭제 후 이 사용자의 남은 할 일 목록
     */
    public List<TodoEntity> delete(final TodoEntity entity) {
        validate(entity);

        try {
            todoDao.delete(entity.getId()); // id로 SQL DELETE 실행
        } catch (Exception e) {
            log.error("error deleting entity {}", entity.getId(), e);
            throw new RuntimeException("error deleting entity " + entity.getId());
        }

        return retrieve(entity.getUserId()); // 남은 목록 반환
    }

}

