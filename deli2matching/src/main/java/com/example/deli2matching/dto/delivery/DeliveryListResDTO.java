package com.example.deli2matching.dto.delivery;

import com.example.deli2matching.entity.delivery.DeliveryList;
import lombok.Builder;

import java.util.List;

@Builder
public class DeliveryListResDTO {

    private List<DeliveryList> list;
    private Integer totalPage;
}
