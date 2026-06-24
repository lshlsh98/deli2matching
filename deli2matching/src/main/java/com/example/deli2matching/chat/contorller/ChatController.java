package com.example.deli2matching.chat.contorller;

import kr.co.iei.chat.model.service.ChatService;
import kr.co.iei.chat.model.vo.ChatMessageDto;
import kr.co.iei.chat.model.vo.ChatRoom;
import kr.co.iei.chat.model.vo.MyChatListResDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/chat")
public class ChatController {
	
	@Autowired
	private ChatService chatService;

	// 이전 메시지 조회
	@GetMapping("/history/{roomId}")
	public ResponseEntity<?> getChatHistory(@PathVariable Long roomId){
		List<ChatMessageDto> list = chatService.getChatHistory(roomId);
		ChatRoom chatRoom = chatService.findChatRoomById(roomId);
		
		Map<String, Object> histories = new HashMap<>();
		histories.put("messages", list);
		histories.put("roomName", chatRoom.getName());
		histories.put("marketNo", chatRoom.getMarketNo());
				
		return ResponseEntity.ok(histories);
	}//

	// 채팅 메시지 읽음 처리
	@PostMapping("/room/{roomId}/read")
	public ResponseEntity<?> messageRead(@PathVariable Long roomId){
		chatService.messageRead(roomId);
		
		return ResponseEntity.ok().build();
	}//

	// 내 채팅방 목록 조회: roomId, roomName, 그룹채팅 여부, 메시지 읽음 갯수
	@GetMapping("/my/rooms")
	public ResponseEntity<?> getMyChatRooms(){
		List<MyChatListResDto> list = chatService.getMyChatRooms();
		
		return ResponseEntity.ok(list);
	}//

	// 개인 채팅방 개설 또는 기존 roomId return
	@PostMapping("/room/private/create")
	public ResponseEntity<?> getOrCreatePrivateRoom(@RequestParam String otherMemberId, @RequestParam Long marketNo){
		Long roomId = chatService.getOrCreatePrivateRoom(otherMemberId, marketNo);
		
		return ResponseEntity.ok(roomId);
	}//
	
	// 거래완료 시 marketNo에 해당하는 chatRoom 제거
	@DeleteMapping("/room/private/{marketNo}")
	public ResponseEntity<?> deleteChatRoomByMarketNo(@PathVariable Long marketNo){
		chatService.deleteChatRoomByMarketNo(marketNo);
		
		return ResponseEntity.ok().build();
	}//
	
	

}













