package com.example.deli2matching.dto.user;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long userId;
    private String memberId;
    private String memberPw;
    private String memberName;
    private String memberEmail;
    private String memberAddr;
    private String token;
}
