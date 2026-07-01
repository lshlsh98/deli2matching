package com.example.deli2matching.chat.contorller;

import com.example.deli2matching.chat.dto.ChatMessageDto;
import com.example.deli2matching.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompController {
	
	private final SimpMessageSendingOperations messageTemplate;
	private final ChatService chatService;

	@MessageMapping("/{roomId}")
	public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageReqDto) {
		chatService.saveMessage(roomId, chatMessageReqDto);

		// @SendTo 의 역할
		messageTemplate.convertAndSend("/topic/" + roomId, chatMessageReqDto);
	}//
}
