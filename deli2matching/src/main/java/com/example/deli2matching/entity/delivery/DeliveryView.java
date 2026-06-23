package com.example.deli2matching.entity.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("deliveryView")
public class DeliveryView {

    private Long postId;
    private Long hostUserId;
    private String restaurantName;
    private Integer targetMembers;
    private Integer currentMembers;
    private String pickupLocation;
    private String detailLocation;
    private String memo;
    private Integer minutesUntilDeadline;
}
