package com.example.deli2matching.chat.dao;

import com.example.deli2matching.chat.dto.*;
import com.example.deli2matching.entity.user.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatDao {

    ChatRoom findChatRoomById(Long roomId);

    UserEntity findMemberById(String loginId);

    void saveChatMessage(ChatMessage chatMessage);

    List<ChatParticipant> findChatParticipantAllById(Long roomId);

    void saveReadStatus(ReadStatus readStatus);

    void saveChatRoom(ChatRoom chatRoom);

    void saveChatParticipant(ChatParticipant chatParticipant);

    Long getGroupChatRooms(Long userId);

    ChatParticipant findByChatRoomAndUser(@Param("roomId") Long roomId,
                                          @Param("userId") Long userId);

    List<ChatMessageDto> findByChatRoomId(Long roomId);

    UserEntity findMemberByUserId(Long userId);

    void updateIsRead(ChatRoomAndMemberReqDto req);

    List<MyChatListResDto> getMyChatRooms(Long userId);

    Integer getCountIsReadZero(ChatRoomAndMemberReqDto req);

    void deleteParticipant(ChatParticipant c);

    void deleteChatRoom(ChatRoom chatRoom);

    boolean existsParticipantByUserId(Long userId);


    List<ChatParticipant> getParticipants(@Param("roomId") Long roomId,
                                          @Param("postId") Long postId);

    void insertChatParticipants(@Param("participants") List<ChatParticipant> participants);

    void deletePostParticipants(Long postId);

    void deleteJoin(@Param("roomId") Long postId,
                    @Param("userId") Long userId);

}
