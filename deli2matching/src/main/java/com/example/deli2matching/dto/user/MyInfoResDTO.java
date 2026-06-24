package com.example.deli2matching.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyInfoResDTO {

    private String loginId;
    private String nickname;
    private String email;
    private String userLocation;
}
