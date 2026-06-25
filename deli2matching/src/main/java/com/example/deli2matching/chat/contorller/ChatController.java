package com.example.deli2matching.chat.contorller;

import com.example.deli2matching.chat.dto.*;
import com.example.deli2matching.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

	private final ChatService chatService;

	// 그룹 채팅방 개설
	@PostMapping("/room/group/create")
	public ResponseEntity<?> createGroupRoom(@RequestBody GroupChatCreateDto req) {
		try {
			chatService.createGroupRoom(req);
			return ResponseEntity.ok().build();

		} catch (IllegalStateException e) {

			if ("Already Participant".equals(e.getMessage())) {
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body("현재 진행 중인 배달 모집이 있습니다.");
			}

			throw e;
		}
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
	}//

	// 그룹 채팅방 삭제 (호스트)
	@DeleteMapping("/room/group/{postId}/delete")
	public ResponseEntity<?> deleteGroupChatRoom(@PathVariable Long postId) {
		chatService.deleteGroupChatRoom(postId);

		return ResponseEntity.ok().build();

	}//
}













