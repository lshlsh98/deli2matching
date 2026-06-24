package com.example.deli2matching.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomAndMemberReqDto {
	
	private Long chatRoomId;
	private Long userId;
}
