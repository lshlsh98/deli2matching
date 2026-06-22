package com.example.deli2matching.dto.delivery;

import lombok.Data;

@Data
public class DeliveryListReqDTO {

    private String location;
    private String keyword;
    private Integer order;
    private Integer page;
    private Integer size;

    public Integer getOffset() {
        return (page - 1) * size;
    }
}
