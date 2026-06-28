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
    @NotBlank(message = "아이디를 입력해주세요.")
    private String memberId;
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String memberPw;
    @NotBlank(message = "이름을 입력해주세요.")
    private String memberName;
    @NotBlank(message = "이메일을 입력해주세요.")
    private String memberEmail;
    @NotBlank(message = "주소를 입력해주세요.")
    private String memberAddr;
    private String token;
}
