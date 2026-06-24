package com.example.deli2matching.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("chatRoomListResDto")
public class ChatRoomListResDto {
	
	private Long roomId;
	private String roomName;
}
