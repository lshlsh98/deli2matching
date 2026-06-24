package com.example.deli2matching.dto.user;

import lombok.Data;

@Data
public class MyInfoReqDTO {

    private String loginId;
    private String nickname;
    private String userLocation;
    private String email;
}
