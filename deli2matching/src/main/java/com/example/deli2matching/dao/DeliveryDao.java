package com.example.deli2matching.dao;

import com.example.deli2matching.dto.delivery.DeliveryCreateReqDTO;
import com.example.deli2matching.dto.delivery.DeliveryListReqDTO;
import com.example.deli2matching.entity.delivery.DeliveryList;
import com.example.deli2matching.entity.delivery.DeliveryView;
import com.example.deli2matching.entity.delivery.Participant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Mapper
public interface DeliveryDao {

    List<DeliveryList> getList(DeliveryListReqDTO req);

    int getListCount(DeliveryListReqDTO req);

    void createDelivery(DeliveryCreateReqDTO req);

    void joinDelivery(@Param("postId") Long postId,
                      @Param("userId") Long userId);

    DeliveryView deliveryView(Long postId);

    List<Participant> getParticipants(Long postId);

    Long getHostUserId(Long postId);

    void deleteDelivery(Long postId);

    void addCurrentMembers(Long postId);

    void deleteJoin(@Param("postId") Long postId,
                    @Param("userId") long userId);

    void subtractCurrentMembers(Long postId);

    DeliveryList getMyJoin(long userId);

}
