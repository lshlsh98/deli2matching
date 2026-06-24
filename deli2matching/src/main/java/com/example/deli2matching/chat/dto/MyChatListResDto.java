package com.example.deli2matching.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Alias("myChatListResDto")
public class MyChatListResDto {

	private Long roomId;
	private String roomName;
	private Integer isGroupChat;
	private Integer unReadCount;

}
