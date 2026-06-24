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
@Alias("chatRoom")
public class ChatRoom {
	
	private Long roomId;
	private String name;			// 채팅방 이름
	private Integer isGroupChat;	// 0: 그룹X / 1: 그룹O
}
