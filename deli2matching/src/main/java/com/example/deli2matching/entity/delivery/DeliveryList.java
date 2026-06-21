package com.example.deli2matching.entity.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("deliveryList")
public class DeliveryList {

    private Long postId;
    private String restaurantName;
    private Integer targetMembers;
    private Integer currentMembers;
    private Date minutesUntilDeadline;
}
