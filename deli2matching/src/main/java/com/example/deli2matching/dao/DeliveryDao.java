package com.example.deli2matching.dao;

import com.example.deli2matching.dto.delivery.DeliveryCreateReqDTO;
import com.example.deli2matching.dto.delivery.DeliveryListReqDTO;
import com.example.deli2matching.entity.delivery.DeliveryList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeliveryDao {

    List<DeliveryList> getList(DeliveryListReqDTO req);

    int getListCount(DeliveryListReqDTO req);

    void createDelivery(DeliveryCreateReqDTO req);

    void joinDelivery(@Param("postId") Long postId,
                      @Param("userId") String userId);
}
