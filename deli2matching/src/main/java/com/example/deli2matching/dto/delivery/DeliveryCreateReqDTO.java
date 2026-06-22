package com.example.deli2matching.dto.delivery;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeliveryCreateReqDTO {

    private Long postId;
    private Long userId;
    private String restaurantName;
    private int targetMembers;
    private String pickupLocation;
    private String detailLocation;
    private String memo;
    private LocalDateTime deadlineAt;
}
