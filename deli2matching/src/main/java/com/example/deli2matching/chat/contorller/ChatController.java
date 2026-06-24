package com.example.deli2matching.chat.contorller;

import com.example.deli2matching.chat.dto.ChatMessageDto;
import com.example.deli2matching.chat.dto.ChatRoom;
import com.example.deli2matching.chat.dto.ChatRoomListResDto;
import com.example.deli2matching.chat.dto.MyChatListResDto;
import com.example.deli2matching.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

	private final ChatService chatService;

	// 그룹 채팅방 개설
	@PostMapping("/room/group/create")
	public ResponseEntity<?> createGroupRoom(@RequestParam String roomName) {
		chatService.createGroupRoom(roomName);

		return ResponseEntity.ok().build();
	}//

	// 그룹 채팅 목록 조회
	@GetMapping("/room/group/list")
	public ResponseEntity<?> getGroupChatRooms() {
		List<ChatRoomListResDto> chatRooms = chatService.getGroupChatRooms();

		return ResponseEntity.ok(chatRooms);
	}//

	// 그룹 채팅방 참여
	@PostMapping("/room/group/{roomId}/join")
	public ResponseEntity<?> joinGroupChatRoom(@PathVariable Long roomId) {
		chatService.addParticipantToGroupChat(roomId);

		return ResponseEntity.ok().build();
	}//

	// 이전 메시지 조회
	@GetMapping("/history/{roomId}")
	public ResponseEntity<?> getChatHistory(@PathVariable Long roomId){
		List<ChatMessageDto> list = chatService.getChatHistory(roomId);
		ChatRoom chatRoom = chatService.findChatRoomById(roomId);

		Map<String, Object> histories = new HashMap<>();
		histories.put("messages", list);
		histories.put("roomName", chatRoom.getName());

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

	// 채팅방 나가기
	@DeleteMapping("/room/group/{roomId}/leave")
	public ResponseEntity<?> leaveGroupChatRoom(@PathVariable Long roomId) {
		chatService.leaveGroupChatRoom(roomId);

		return ResponseEntity.ok().build();
	}

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













