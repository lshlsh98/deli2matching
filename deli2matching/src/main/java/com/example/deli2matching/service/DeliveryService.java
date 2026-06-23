package com.example.deli2matching.service;

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

    public List<DeliveryList> getList(DeliveryListReqDTO req) {
        return deliveryDao.getList(req);
    }//

    public int getListCount(DeliveryListReqDTO req) {
       return deliveryDao.getListCount(req);
    }//

    @Transactional
    public void createDelivery(DeliveryCreateReqDTO req) {
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
    public void deleteDelivery(Long postId, long userId) {
       deliveryDao.deleteDelivery(postId, userId);
    }//

    @Transactional
    public void joinDelivery(Long postId, Long userId) {
        deliveryDao.joinDelivery(postId, userId);
        deliveryDao.addCurrentMembers(postId);
    }//

    @Transactional
    public void deleteJoin(Long postId, long userId) {
        deliveryDao.deleteJoin(postId, userId);
        deliveryDao.subtractCurrentMembers(postId);
    }//
}
