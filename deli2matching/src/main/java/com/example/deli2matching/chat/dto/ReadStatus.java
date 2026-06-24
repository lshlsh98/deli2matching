package com.example.deli2matching.chat.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("readStatus")
public class ReadStatus {

	private Long readId;
	private Long messageId;
	private Long userId;
	private Long roomId;
	private Integer isRead;	// 0: 안읽음 1: 읽음
}
