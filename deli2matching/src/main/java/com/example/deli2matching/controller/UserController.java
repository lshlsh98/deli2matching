package com.example.deli2matching.controller;


import com.example.deli2matching.dto.user.*;
import com.example.deli2matching.entity.user.UserEntity;
import com.example.deli2matching.security.TokenProvider;
import com.example.deli2matching.service.UserService;
import com.example.deli2matching.utils.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final EmailSender sender;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    // 회원가입 아이디 중복체크
    @GetMapping("/idExists")
    public ResponseEntity<?> idExists(@RequestBody String memberId) {
        int result = userService.idExists(memberId);

        return ResponseEntity.ok(result > 0);
    }//

    // 회원가입 닉네임 중복체크
    @GetMapping("/nameExists")
    public ResponseEntity<?> nameExists(@RequestBody String memberName) {
        int result = userService.nameExists(memberName);

        return ResponseEntity.ok(result > 0);
    }//

    // 이메일 인증
    @PostMapping(value = "/email-verification")
    public ResponseEntity<?> sendMail(@RequestBody Map<String, String> requestData) {
        String emailTitle = "같이 시켜 이메일 인증번호입니다.";

        String receiverEmail = requestData.get("memberEmail");

        Random r = new Random();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < 6; i++) {
            int flag = r.nextInt(3); // 0, 1, 2 -> 숫자, 대문자, 소문자 어떤걸 추출할지 랜덤으로 결정
            if (flag == 0) {
                sb.append(r.nextInt(10));
            } else if (flag == 1) {
                sb.append((char) (r.nextInt(26) + 65));
            } else if (flag == 2) {
                sb.append((char) (r.nextInt(26) + 97));
            }
        }
        String authCode = sb.toString();

        String emailContent = "<h1>안녕하세요. 같이 시켜 입니다.</h1>" + "<h3>인증번호는 [<b>" + authCode
                + "</b>] 입니다.</h3>" + "<h3>화면으로 돌아가 인증번호를 입력해 주세요.</h3>";

        sender.sendMail(emailTitle, receiverEmail, emailContent);

        return ResponseEntity.ok(authCode);
    }//

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            // UserEntity 생성: DB에 저장할 형태로 변환
            UserEntity user = UserEntity.builder()
                    .loginId(userDTO.getMemberId())
                    .password(passwordEncoder.encode(userDTO.getMemberPw()))
                    .email(userDTO.getMemberEmail())
                    .nickname(userDTO.getMemberName())
                    .userLocation(userDTO.getMemberAddr())
                    .build();

            UserEntity registeredUser = userService.create(user);

            // 응답용 DTO: 비밀번호는 포함하지 않음
            UserDTO responseUserDTO = UserDTO.builder()
                    .userId(registeredUser.getUserId())
                    .memberId(registeredUser.getLoginId())
                    .memberName(registeredUser.getNickname())
                    .build();

            return ResponseEntity.ok().body(responseUserDTO);

        } catch (Exception e) {
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .error(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(responseDTO);
        }
    }//

    @PostMapping("/signin")
    public ResponseEntity<?> authenticate(@RequestBody UserDTO userDTO) {
        // username으로 사용자 조회 + 비밀번호 검증
        // BCrypt는 "1234"가 "$2a$10$..." 해시와 일치하는지 확인해줌
        UserEntity user = userService.getByCredentials(
                userDTO.getMemberId(),
                userDTO.getMemberPw(),
                passwordEncoder
        );

        if (user != null) {
            // 로그인 성공 JWT 토큰 발급
            final String token = tokenProvider.create(user);

            // 응답: 사용자 정보 + 토큰
            final UserDTO responseUserDTO = UserDTO.builder()
                    .userId(user.getUserId())
                    .memberId(user.getLoginId())
                    .memberName(user.getNickname())
                    .memberEmail(user.getEmail())
                    .memberAddr(user.getUserLocation())
                    .token(token) // ← 클라이언트가 이걸 저장해서 이후 요청에 사용
                    .build();

            return ResponseEntity.ok().body(responseUserDTO); // 200 OK
        } else {
            // 로그인 실패 (username이 없거나 비밀번호 불일치)
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .error("Login failed.")
                    .build();

            return ResponseEntity.badRequest().body(responseDTO); // 400 Bad Request
        }
    }//

    // 비밀번호 확인
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody PasswordRequestDTO req, @AuthenticationPrincipal String userId) {
        String loginId = userService.findLoginIdByUserId(userId);

        String password = req.getPassword();

        UserEntity user = userService.getByCredentials(
                loginId,
                password,
                passwordEncoder
        );

        if (user != null) {
            return ResponseEntity.ok("ok");
        }

        return ResponseEntity.badRequest().build();
    }//

    // myInfo 조회
    @GetMapping("/myInfo")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal String userId) {
        UserEntity user = userService.getMyInfo(Long.parseLong(userId));

        MyInfoResDTO res = MyInfoResDTO.builder()
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .userLocation(user.getUserLocation())
                .build();

        return ResponseEntity.ok(res);
    }//

    // myInfo 수정
    @PutMapping("/myInfo")
    public ResponseEntity<?> updateMyInfo(@RequestBody MyInfoReqDTO req) {
        userService.updateMyInfo(req);

        return ResponseEntity.ok("ok");
    }//


}

