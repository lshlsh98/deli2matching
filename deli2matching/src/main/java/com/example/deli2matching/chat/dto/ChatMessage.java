package com.example.deli2matching.chat.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;

@Data
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
