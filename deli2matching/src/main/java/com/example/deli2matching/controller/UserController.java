package com.example.deli2matching.controller;


import com.example.deli2matching.dto.ResponseDTO;
import com.example.deli2matching.dto.UserDTO;
import com.example.deli2matching.entity.UserEntity;
import com.example.deli2matching.security.TokenProvider;
import com.example.deli2matching.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

/**
 * =====================================================================
 * UserController - 일반 회원가입/로그인 API 컨트롤러
 * =====================================================================
 *
 * URL 접두사: /auth
 * WebSecurityConfig에서 /auth/** 는 인증 없이 접근 가능하도록 설정됨
 *
 * 제공하는 API:
 *  - POST /auth/signup : 일반 회원가입
 *  - POST /auth/signin : 일반 로그인 (성공 시 JWT 토큰 반환)
 *
 * 소셜 로그인은 여기서 처리X
 *    소셜 로그인은 스프링 시큐리티가 자동으로 처리하며,
 *    CustomOAuth2UserService → OAuthSuccessHandler 순서로 진행됩니다.
 * =====================================================================
 */

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final TokenProvider tokenProvider;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 회원가입 아이디 중복체크
    @GetMapping("/idExists")
    public ResponseEntity<?> idExists (@RequestBody String memberId) {
        int result = userService.idExists(memberId);

        return ResponseEntity.ok(result > 0);
    }//

    // 회원가입 닉네임 중복체크
    @GetMapping("/nameExists")
    public ResponseEntity<?> nameExists (@RequestBody String memberName) {
        int result = userService.nameExists(memberName);

        return ResponseEntity.ok(result > 0);
    }//

    // 이메일 인증
    @PostMapping(value = "/email-verification")
    public ResponseEntity<?> sendMail(@RequestBody Map<String, String> requestData) {
        String emailTitle = "같이시켜 이메일 인증번호입니다.";

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

        String emailContent = "<h1>안녕하세요. 같이시켜 입니다.</h1>" + "<h3>인증번호는 [<b>" + authCode
                + "</b>] 입니다.</h3>" + "<h3>화면으로 돌아가 인증번호를 입력해 주세요.</h3>";

        sender.sendMail(emailTitle, receiverEmail, emailContent);

        return ResponseEntity.ok(authCode);
    }//

    /**
     * POST /auth/signup - 일반 회원가입
     *
     * 요청 형식 (JSON):
     * {
     *   "username": "홍길동",
     *   "password": "1234"
     * }
     *
     * 성공 응답 (200 OK):
     * {
     *   "id": 1,
     *   "username": "홍길동"
     * }
     *
     * @param userDTO 클라이언트가 보낸 사용자 정보 (JSON → 자바 객체 자동 변환)
     * @return 등록된 사용자 정보 (비밀번호 제외)
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        try {
            // 비밀번호 유효성 검사
            if (userDTO == null || userDTO.getPassword() == null) {
                throw new RuntimeException("Invalid Password value.");
            }

            // UserEntity 생성: DB에 저장할 형태로 변환
            UserEntity user = UserEntity.builder()
                    .loginId(userDTO.getLoginId())
                    .password(passwordEncoder.encode(userDTO.getPassword()))
                    .nickname(userDTO.getUsername())
                    .provider("local")
                    .build();

            UserEntity registeredUser = userService.create(user);

            // 응답용 DTO: 비밀번호는 포함하지 않음
            UserDTO responseUserDTO = UserDTO.builder()
                    .id(registeredUser.getUserId())
                    .username(registeredUser.getNickname())
                    .build();

            return ResponseEntity.ok().body(responseUserDTO); // 200 OK
        } catch (Exception e) {
            // 에러 발생 시 (예: 중복 username)
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .error(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(responseDTO); // 400 Bad Request
        }
    }

    /**
     * POST /auth/signin - 일반 로그인
     *
     * 요청 형식 (JSON):
     * {
     *   "username": "홍길동",
     *   "password": "1234"
     * }
     *
     * 성공 응답 (200 OK):
     * {
     *   "id": 1,
     *   "username": "홍길동",
     *   "token": "eyJhbGc..."  ← 이 토큰을 이후 API 요청에 사용!
     * }
     *
     * @param userDTO 로그인 정보 (username + password)
     * @return 사용자 정보 + JWT 토큰
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticate(@RequestBody UserDTO userDTO) {
        // username으로 사용자 조회 + 비밀번호 검증
        // BCrypt는 "1234"가 "$2a$10$..." 해시와 일치하는지 확인해줌
        UserEntity user = userService.getByCredentials(
                userDTO.getLoginId(),
                userDTO.getPassword(),
                passwordEncoder
        );

        if (user != null) {
            // 로그인 성공 JWT 토큰 발급
            final String token = tokenProvider.create(user);

            // 응답: 사용자 정보 + 토큰
            final UserDTO responseUserDTO = UserDTO.builder()
                    .username(user.getNickname())
                    .id(user.getUserId())
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
    }

}

