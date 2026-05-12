package com.example.deli2matching.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TodoEntity - JPA @Entity 제거, MyBatis에서는 순수 POJO로 사용
 * DB 매핑은 TodoMapper.xml에서 담당
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TodoEntity {

    private Long id;

    private Long userId;

    private String title;

    private boolean done;

}