package com.example.deli2matching.service;

import com.example.deli2matching.chat.dao.ChatDao;
import com.example.deli2matching.chat.dto.ChatParticipant;
import com.example.deli2matching.chat.dto.ChatRoom;
import com.example.deli2matching.chat.dto.GroupChatCreateDto;
import com.example.deli2matching.dao.DeliveryDao;
import com.example.deli2matching.dto.delivery.DeliveryCreateReqDTO;
import com.example.deli2matching.dto.delivery.DeliveryListReqDTO;
import com.example.deli2matching.entity.delivery.DeliveryList;
import com.example.deli2matching.entity.delivery.DeliveryView;
import com.example.deli2matching.entity.delivery.Participant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeliveryService {

    private final DeliveryDao deliveryDao;
    private final ChatDao chatDao;

    public List<DeliveryList> getList(DeliveryListReqDTO req) {
        return deliveryDao.getList(req);
    }//

    public int getListCount(DeliveryListReqDTO req) {
       return deliveryDao.getListCount(req);
    }//

    @Transactional
    public void createDelivery(DeliveryCreateReqDTO req) {
        boolean exists = deliveryDao.existsParticipantByUserId(req.getUserId());
        if (exists) {
            throw new IllegalStateException("Already Participant");
        }

        deliveryDao.createDelivery(req);
    }//

    public DeliveryView deliveryView(Long postId) {
       return deliveryDao.deliveryView(postId);
    }//

    public List<Participant> getParticipants(Long postId) {
       return deliveryDao.getParticipants(postId);
    }//

    public Long getHostUserId(Long postId) {
       return deliveryDao.getHostUserId(postId);
    }//

    @Transactional
    public void deleteDelivery(Long postId) {
       deliveryDao.deleteDelivery(postId);
    }//

    @Transactional
    public void joinDelivery(Long postId, Long userId) {
        boolean post_exists = deliveryDao.existsParticipantByUserId(userId);
        boolean chat_exists = chatDao.existsParticipantByUserId(userId);
        if (post_exists || chat_exists) {
            throw new IllegalStateException("Already Participant");
        }

        deliveryDao.joinDelivery(postId, userId);
        deliveryDao.addCurrentMembers(postId);
    }//

    @Transactional
    public void deleteJoin(Long postId, long userId) {
        deliveryDao.deleteJoin(postId, userId);
        deliveryDao.subtractCurrentMembers(postId);
    }//

    public DeliveryList getMyJoin(long userId) {
       return deliveryDao.getMyJoin(userId);
    }//

    @Transactional
    public void closeDeliveryAndCreateGroupRoom(GroupChatCreateDto req) {
        deliveryDao.closeDelivery(req.getPostId());

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(req.getRoomName())
                .isGroupChat(1)
                .postId(req.getPostId())
                .build();
        chatDao.saveChatRoom(chatRoom);

        // chat_participants 삽입
        List<ChatParticipant> participants = chatDao.getParticipants(chatRoom.getRoomId(), req.getPostId());
        chatDao.insertChatParticipants(participants);

        // post_participants 삭제
//        chatDao.deletePostParticipants(req.getPostId());
    }//
}
