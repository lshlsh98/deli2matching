package com.example.deli2matching.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Alias("todoEntity")
public class TodoEntity {

    private Long id;

    private Long userId;

    private String title;

    private boolean done;

}