package com.example.deli2matching.controller;

import com.example.deli2matching.dto.delivery.DeliveryCreateReqDTO;
import com.example.deli2matching.dto.delivery.DeliveryListReqDTO;
import com.example.deli2matching.dto.delivery.DeliveryListResDTO;
import com.example.deli2matching.entity.delivery.DeliveryList;
import com.example.deli2matching.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    // 배달 모집 리스트 조회
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

    // 배달 모집 생성
    @PostMapping
    public ResponseEntity<?> createDelivery(@RequestBody DeliveryCreateReqDTO req,
                                               @AuthenticationPrincipal String userId) {

        if (userId != null) {
            req.setUserId(Long.parseLong(userId));
        }

        int result = deliveryService.createDelivery(req);

        return ResponseEntity.ok(result);
    }//
}
