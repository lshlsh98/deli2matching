package com.example.deli2matching.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * URL 접두사: /todo
 * WebSecurityConfig에서 /todo/** 는 인증(로그인)이 필요하도록 설정됨
 * → JWT 토큰 없이 접근하면 403 Forbidden 에러!
 *
 * 제공하는 API (모두 로그인 필수):
 *  - POST   /todo : 새 할 일 추가
 *  - GET    /todo : 내 할 일 목록 조회
 *  - PUT    /todo : 할 일 수정
 *  - DELETE /todo : 할 일 삭제
 *
 * @AuthenticationPrincipal String userId:
 *  JwtAuthenticationFilter가 SecurityContext에 저장한 사용자 ID를 꺼냄
 * =====================================================================
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("todo")
public class TodoController {

    private final TodoService service;

    /**
     * POST /todo - 새 할 일 추가
     *
     * 요청 헤더: Authorization: Bearer eyJhbGc...
     * 요청 Body (JSON): { "title": "스프링 공부하기", "done": false }
     *
     * 성공 응답: 내 전체 할 일 목록 (새로 추가된 것 포함)
     *
     * @param userId JwtAuthenticationFilter가 JWT에서 꺼낸 사용자 DB id
     * @param dto    클라이언트가 보낸 새 할 일 정보
     */
    @PostMapping
    public ResponseEntity<?> createTodo(
            @AuthenticationPrincipal String userId, // JWT에서 추출한 사용자 ID
            @RequestBody TodoDTO dto) {             // 요청 Body의 JSON을 DTO로 변환

        try {
            // DTO → Entity 변환 (API 통신용 객체 → DB 저장용 객체)
            TodoEntity entity = TodoDTO.toEntity(dto);

            // id는 DB에서 자동 생성하므로 null로 설정
            entity.setId(null);

            // JWT에서 꺼낸 사용자 ID를 할 일 소유자로 설정
            // Long.parseLong(): String "123" → Long 123
            entity.setUserId(Long.parseLong(userId));

            // DB에 저장하고 이 사용자의 전체 할 일 목록 반환
            List<TodoEntity> entities = service.create(entity);

            // Entity 목록 → DTO 목록 변환 (응답용)
            List<TodoDTO> dtos = entities.stream()
                    .map(TodoDTO::new)       // TodoEntity → TodoDTO 변환
                    .collect(Collectors.toList());

            // 응답 감싸기
            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder()
                    .data(dtos).build();

            return ResponseEntity.ok().body(response); // 200 OK
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder()
                    .error(error).build();
            return ResponseEntity.badRequest().body(response); // 400 Bad Request
        }
    }

    /**
     * GET /todo - 내 할 일 목록 조회
     *
     * 요청 헤더: Authorization: Bearer eyJhbGc...
     * 성공 응답: 로그인한 사람의 할 일 목록만 반환 (다른 사람 것은 안 보임!)
     *
     * @param userId JWT에서 추출한 사용자 ID (누구의 목록을 조회할지)
     */
    @GetMapping
    public ResponseEntity<?> retrieveTodoList(@AuthenticationPrincipal String userId) {
        // 이 사용자 ID에 해당하는 할 일만 조회 (본인 것만!)
        List<TodoEntity> entities = service.retrieve(Long.parseLong(userId));

        List<TodoDTO> dtos = entities.stream()
                .map(TodoDTO::new)
                .collect(Collectors.toList());

        ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder()
                .data(dtos).build();

        return ResponseEntity.ok().body(response); // 200 OK
    }

    /**
     * PUT /todo - 할 일 수정
     *
     * 요청 헤더: Authorization: Bearer eyJhbGc...
     * 요청 Body (JSON): { "id": 1, "title": "수정된 내용", "done": true }
     *
     * @param userId JWT에서 추출한 사용자 ID
     * @param dto    수정할 할 일 정보 (id 포함!)
     */
    @PutMapping
    public ResponseEntity<?> updateTodo(
            @AuthenticationPrincipal String userId,
            @RequestBody TodoDTO dto) {

        TodoEntity entity = TodoDTO.toEntity(dto);
        entity.setUserId(Long.parseLong(userId));

        // 수정 후 최신 목록 반환
        List<TodoEntity> entities = service.update(entity);

        List<TodoDTO> dtos = entities.stream()
                .map(TodoDTO::new)
                .collect(Collectors.toList());

        ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder()
                .data(dtos).build();

        return ResponseEntity.ok().body(response); // 200 OK
    }

    /**
     * DELETE /todo - 할 일 삭제
     *
     * 요청 헤더: Authorization: Bearer eyJhbGc...
     * 요청 Body (JSON): { "id": 1 }  (삭제할 항목의 id)
     *
     * @param userId JWT에서 추출한 사용자 ID
     * @param dto    삭제할 할 일 정보 (id만 있으면 됨)
     */
    @DeleteMapping
    public ResponseEntity<?> deleteTodo(
            @AuthenticationPrincipal String userId,
            @RequestBody TodoDTO dto) {

        try {
            TodoEntity entity = TodoDTO.toEntity(dto);
            entity.setUserId(Long.parseLong(userId));

            // 삭제 후 남은 목록 반환
            List<TodoEntity> entities = service.delete(entity);

            List<TodoDTO> dtos = entities.stream()
                    .map(TodoDTO::new)
                    .collect(Collectors.toList());

            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder()
                    .data(dtos).build();

            return ResponseEntity.ok().body(response); // 200 OK
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder()
                    .error(error).build();
            return ResponseEntity.badRequest().body(response); // 400 Bad Request
        }
    }

}
