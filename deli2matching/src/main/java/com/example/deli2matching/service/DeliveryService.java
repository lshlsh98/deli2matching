package com.example.deli2matching.service;

import com.example.deli2matching.dao.DeliveryDao;
import com.example.deli2matching.dto.delivery.DeliveryCreateReqDTO;
import com.example.deli2matching.dto.delivery.DeliveryListReqDTO;
import com.example.deli2matching.entity.delivery.DeliveryList;
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
    public int createDelivery(DeliveryCreateReqDTO req) {
        return deliveryDao.createDelivery(req);
    }//
}
