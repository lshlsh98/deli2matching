package com.example.deli2matching.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("chatParticipant")
public class ChatParticipant {
	
	private Long participantId;
	private Long roomId;
	private Long userId;
}
