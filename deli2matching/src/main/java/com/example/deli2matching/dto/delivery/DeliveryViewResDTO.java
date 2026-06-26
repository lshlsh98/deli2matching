package com.example.deli2matching.dto.delivery;

import com.example.deli2matching.entity.delivery.Participant;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DeliveryViewResDTO {

    private Long hostUserId;
    private String restaurantName;
    private Integer currentMembers;
    private Integer targetMembers;
    private String pickupLocation;
    private String detailLocation;
    private String memo;
    private Integer minutesUntilDeadline;
    private List<Participant> participants;
    private String status;
}
