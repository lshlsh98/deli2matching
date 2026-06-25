package com.example.deli2matching.chat.service;

import com.example.deli2matching.chat.dao.ChatDao;
import com.example.deli2matching.chat.dto.*;
import com.example.deli2matching.chat.exception.NotFoundException;
import com.example.deli2matching.dao.UserDao;
import com.example.deli2matching.entity.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ChatService {

    private final ChatDao chatDao;
    private final UserDao userDao;

    public void saveMessage(Long roomId, ChatMessageDto chatMessageReqDto) {
        // 채팅방 조회
        ChatRoom chatRoom = chatDao.findChatRoomById(roomId);
        if (chatRoom == null) {
            throw new NotFoundException("room cannot be found");
        }

        // 보낸 사람 조회
        UserEntity sender = chatDao.findMemberById(chatMessageReqDto.getSenderId());
        if (sender == null) {
            throw new NotFoundException("member cannot be found");
        }

        // 메시지 저장 — useGeneratedKeys로 id 자동 세팅
        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(chatRoom.getRoomId())
                .content(chatMessageReqDto.getMessage())
                .userId(sender.getUserId())
                .build();
        chatDao.saveChatMessage(chatMessage);

        // 사용자 별로 읽음 여부 저장
        List<ChatParticipant> chatParticipants = chatDao.findChatParticipantAllById(chatRoom.getRoomId());
        for (ChatParticipant c : chatParticipants) {
            ReadStatus readStatus = new ReadStatus();
            readStatus.setRoomId(chatRoom.getRoomId());
            readStatus.setUserId(c.getUserId());
            readStatus.setMessageId(chatMessage.getMessageId());
            if (Objects.equals(sender.getUserId(), c.getUserId())) {
                readStatus.setIsRead(1); // 읽음
            } else {
                readStatus.setIsRead(0); // 안읽음
            }

            chatDao.saveReadStatus(readStatus);
        }
    }//

    // chatParticipant 객체 생성 후 저장 (그룹채팅, 1:1채팅 모두 사용)
    public void addParticipantToRoom(ChatRoom chatRoom, UserEntity user) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .roomId(chatRoom.getRoomId())
                .userId(user.getUserId())
                .build();

        chatDao.saveChatParticipant(chatParticipant);
    }//

    public List<ChatMessageDto> getChatHistory(Long roomId) {
        // 내가 해당 채팅방의 참여자가 아닐 경우 에러
        ChatRoom chatRoom = chatDao.findChatRoomById(roomId);
        if(chatRoom == null) {
            throw new NotFoundException("chatRoom can not be found");
        }

        UserEntity user = chatDao.findMemberById(SecurityContextHolder.getContext().getAuthentication().getName());
        if(user == null) {
            throw new NotFoundException("user can not be found");
        }

        List<ChatParticipant> chatParticipants = chatDao.findChatParticipantAllById(chatRoom.getRoomId());
        boolean check = false;
        for(ChatParticipant c : chatParticipants) {
            if(c.getUserId().equals(user.getUserId())) {
                check = true;
            }
        }
        if(!check) {
            throw new IllegalArgumentException("본인이 속하지 않은 채팅방 입니다.");
        }

        // 본인이 속한 채팅방의 경우 history 반환
        List<ChatMessageDto> list = chatDao.findByChatRoomId(chatRoom.getRoomId());

        return list;
    }//

    public boolean isRoomParticipant(Long userId, Long roomId) {
        ChatRoom chatRoom = chatDao.findChatRoomById(roomId);
        if(chatRoom == null) {
            throw new NotFoundException("chatRoom can not be found");
        }

        UserEntity user = chatDao.findMemberByUserId(userId);
        if(user == null) {
            throw new NotFoundException("member can not be found");
        }

        List<ChatParticipant> chatParticipants = chatDao.findChatParticipantAllById(chatRoom.getRoomId());
        for(ChatParticipant c : chatParticipants) {
            if(c.getUserId().equals(user.getUserId())) {
                return true;
            }
        }

        return false;
    }//

    public void messageRead(Long roomId) {
        ChatRoom chatRoom = chatDao.findChatRoomById(roomId);
        if(chatRoom == null) {
            throw new NotFoundException("chatRoom can not be found");
        }

        UserEntity sender = userDao.findByUserId(Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName()));
        if (sender == null) {
            throw new NotFoundException("member cannot be found");
        }

        ChatRoomAndMemberReqDto req = new ChatRoomAndMemberReqDto(chatRoom.getRoomId(), sender.getUserId());
        chatDao.updateIsRead(req);
    }//

    public List<MyChatListResDto> getMyChatRooms() {
        UserEntity sender = userDao.findByUserId(Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName()));
        if(sender == null) {
            throw new NotFoundException("sender can not be found");
        }

        List<MyChatListResDto> MyChatListResDtos = chatDao.getMyChatRooms(sender.getUserId());
        for(MyChatListResDto d :  MyChatListResDtos) {
            ChatRoomAndMemberReqDto req = new ChatRoomAndMemberReqDto(d.getRoomId(), sender.getUserId());
            Integer count = chatDao.getCountIsReadZero(req); // 안읽은것 0 의 숫자
            d.setUnReadCount(count == null ? 0 : count);
        }

        return MyChatListResDtos;
    }//

    /*
    public Long getOrCreatePrivateRoom(String otherMemberId, Long marketNo) {
        Member member = chatDao.findMemberById(SecurityContextHolder.getContext().getAuthentication().getName());
        if(member == null) {
            throw new NotFoundException("member can not be found");
        }

        Member otherMember = chatDao.findMemberById(otherMemberId);
        if(otherMember == null) {
            throw new NotFoundException("member can not be found");
        }

        // 나와 상대방이 1:1 채팅에 이미 참석하고 있다면 해당 roomId return
        CreatePrivateRoomReqDto req = CreatePrivateRoomReqDto.builder()
                .memberId(member.getMemberId())
                .otherMemberId(otherMemberId)
                .marketNo(marketNo)
                .build();
        ChatRoom chatRoom = chatDao.findExistingPrivateRoom(req);

        if(chatRoom != null) {
            return chatRoom.getId();
        }

        // 만약 1:1 채팅방이 없을 경우 채팅방 개설
        Market market = marketDao.findOneMarketByMarketNo(Math.toIntExact(marketNo));

        // useGeneratedKeys로 id 자동 세팅
        ChatRoom newRoom = ChatRoom.builder()
                .isGroupChat(1)
                .name(market.getMarketTitle())
                .marketNo(marketNo)
                .myName(member.getMemberName())
                .myId(member.getMemberId())
                .otherName(otherMember.getMemberName())
                .otherId(otherMemberId)
                .build();
        chatDao.saveChatRoom(newRoom);

        // 두 사람 모두 참여자로 새롭게 추가
        addParticipantToRoom(newRoom, member);
        addParticipantToRoom(newRoom, otherMember);

        return newRoom.getId();
    }//
    */

    /*
    public void deleteChatRoomByMarketNo(Long marketNo) {
        chatDao.deleteChatRoomByMarketNo(marketNo);
    }//
    */

    public ChatRoom findChatRoomById(Long roomId) {
        return chatDao.findChatRoomById(roomId);
    }//

    // 그룹 채팅방 개설
    public void createGroupRoom(GroupChatCreateDto req) {
        UserEntity user = userDao.findByUserId(Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName()));

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(req.getRoomName())
                .isGroupChat(1)
                .postId(req.getPostId())
                .build();
        chatDao.saveChatRoom(chatRoom);

        // 채팅 참여자로 개설자를 추가
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .roomId(chatRoom.getRoomId())
                .userId(user.getUserId())
                .build();
        chatDao.saveChatParticipant(chatParticipant);
    }//

    // 그룹 채팅 목록 조회
    public List<ChatRoomListResDto> getGroupChatRooms() {
        return chatDao.getGroupChatRooms();
    }//

    // 그룹 채팅방 참여
    public void addParticipantToGroupChat(Long roomId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatDao.findChatRoomById(roomId);

        // user 조회
        UserEntity user = userDao.findByUserId(Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName()));

        // 이미 참여자인지 검증
        ChatParticipant participant = chatDao.findByChatRoomAndUser(chatRoom.getRoomId(), user.getUserId());
        if (participant == null) {
            addParticipantToRoom(chatRoom, user);
        }
    }//

    // 채팅 나가기
    public void leaveGroupChatRoom(Long roomId) {
        ChatRoom chatRoom = chatDao.findChatRoomById(roomId);
        if(chatRoom == null) {
            throw new NotFoundException("chatRoom can not be found");
        }

        UserEntity user = userDao.findByUserId(Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName()));
        if (user == null) {
            throw new NotFoundException("user cannot be found");
        }

        if (chatRoom.getIsGroupChat() == 0) {
            throw new IllegalArgumentException("단체 채팅방이 아닙니다.");
        }

        ChatParticipant c = chatDao.findByChatRoomAndUser(roomId, user.getUserId());
        chatDao.deleteParticipant(c);

        List<ChatParticipant> chatParticipants = chatDao.findChatParticipantAllById(roomId);
        if (chatParticipants.isEmpty()) {
            chatDao.deleteChatRoom(chatRoom);
        }
    }//
}
