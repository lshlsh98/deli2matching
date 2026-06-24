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

//	// 방법1. MessageMapping(수신)과 SendTo(topic 에 메시지 전달) 한꺼번에 처리
//	@MessageMapping("/{roomId}") 	// 클라이언트에서 특정 publish/roomId 형태로 메시지를 발행 시 MessageMapping 수신
//	@SendTo("/topic/{roomId}") 		// 해당 roomId 에 메시지에 발행하여 구독중인 클라이언트에게 메시지 전송
//	// DestinationVariable: @MessageMapping 에노테이션으로 정의된 Websocket Controller 내에서만 사용
//	public String sendMessage(@DestinationVariable Long roomId, String message) {
//		log.info(message);
//
//		return message;
//	}//
	
	// 방법2. MessageMapping 에노테이션에만 활용
	@MessageMapping("/{roomId}")
	public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageReqDto) {
		chatService.saveMessage(roomId, chatMessageReqDto);

		// @SendTo 의 역할
		messageTemplate.convertAndSend("/topic/" + roomId, chatMessageReqDto);
	}//
//
}
