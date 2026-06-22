package com.example.deli2matching.controller;

import com.example.deli2matching.dto.delivery.DeliveryListReqDTO;
import com.example.deli2matching.dto.delivery.DeliveryListResDTO;
import com.example.deli2matching.entity.delivery.DeliveryList;
import com.example.deli2matching.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping()
    public ResponseEntity<?> getList(@ModelAttribute DeliveryListReqDTO req) {
        List<DeliveryList> list = deliveryService.getList(req);

        int count = deliveryService.getListCount(req);
        int totalPage = (int) Math.ceil( count / (double) req.getSize() );

        DeliveryListResDTO res = DeliveryListResDTO.builder()
                .list(list)
                .totalPage(totalPage)
                .build();

        return ResponseEntity.ok(res);
    }//
}
