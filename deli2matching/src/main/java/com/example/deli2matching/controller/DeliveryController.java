package com.example.deli2matching.controller;

import com.example.deli2matching.dto.delivery.DeliveryCreateReqDTO;
import com.example.deli2matching.dto.delivery.DeliveryListReqDTO;
import com.example.deli2matching.dto.delivery.DeliveryListResDTO;
import com.example.deli2matching.dto.delivery.DeliveryViewResDTO;
import com.example.deli2matching.entity.delivery.DeliveryList;
import com.example.deli2matching.entity.delivery.DeliveryView;
import com.example.deli2matching.entity.delivery.Participant;
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

    // 배달 모집 생성 및 생성자 참여
    @PostMapping
    public ResponseEntity<?> createDelivery(@RequestBody DeliveryCreateReqDTO req,
                                            @AuthenticationPrincipal String userId) {

        if (userId != null) {
            req.setUserId(Long.parseLong(userId));
        }

        deliveryService.createDelivery(req);
        deliveryService.joinDelivery(req.getPostId(), Long.parseLong(userId));

        return ResponseEntity.ok("ok");
    }//

    // 모집 상세
    @GetMapping("/{postId}")
    public ResponseEntity<?> deliveryView(@PathVariable Long postId) {
        DeliveryView deliveryView = deliveryService.deliveryView(postId);
        List<Participant> participants = deliveryService.getParticipants(postId);

        DeliveryViewResDTO res = DeliveryViewResDTO.builder()
                .hostUserId(deliveryView.getHostUserId())
                .restaurantName(deliveryView.getRestaurantName())
                .currentMembers(deliveryView.getCurrentMembers())
                .targetMembers(deliveryView.getTargetMembers())
                .pickupLocation(deliveryView.getPickupLocation())
                .detailLocation(deliveryView.getDetailLocation())
                .memo(deliveryView.getMemo())
                .minutesUntilDeadline(deliveryView.getMinutesUntilDeadline())
                .participants(participants)
                .build();

        return ResponseEntity.ok(res);
    }//

    // 모집 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deleteDelivery(@PathVariable Long postId, @AuthenticationPrincipal String userId) {
        Long hostUserId = deliveryService.getHostUserId(postId);
        if (hostUserId != Long.parseLong(userId)) {
            return ResponseEntity.badRequest().body("권한이 없습니다.");
        }

        deliveryService.deleteDelivery(postId, Long.parseLong(userId));

        return ResponseEntity.ok("ok");
    }//

    // 모집 참여
    @PostMapping("/{postId}/join")
    public ResponseEntity<?> joinDelivery(@PathVariable Long postId, @AuthenticationPrincipal String userId) {
        deliveryService.joinDelivery(postId, Long.parseLong(userId));

       return ResponseEntity.ok("ok");
    }//

    // 참여 취소
    @DeleteMapping("/{postId}/join")
    public ResponseEntity<?> deleteJoin(@PathVariable Long postId, @AuthenticationPrincipal String userId) {
        deliveryService.deleteJoin(postId, Long.parseLong(userId));

        return ResponseEntity.ok("ok");
    }//
}
