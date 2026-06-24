package com.example.deli2matching.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Alias("chatMessage")
@Builder
public class ChatMessage {
	
	private Long messageId;
	private Long roomId;
	private Long userId;
	private String content;
}
