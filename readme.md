## 전체 구조

```
deli2matching/
├── .env.example                  # 환경 변수 템플릿
├── docker-compose.yml            # Docker 배포 설정 (Backend + Frontend)
├── deli2matching/                # Spring Boot 백엔드
└── deli2matching_front/          # React 프론트엔드
```

---

## Backend (`deli2matching/`)

**Spring Boot 4.0.6 · Java 17 · MyBatis · MySQL · Spring Security · OAuth2 · WebSocket(STOMP)**

```
deli2matching/
├── Dockerfile
├── build.gradle
└── src/main/
    ├── java/com/example/deli2matching/
    │   ├── Deli2matchingApplication.java        # 애플리케이션 진입점
    │   │
    │   ├── config/
    │   │   └── WebSecurityConfig.java           # Spring Security 전체 설정
    │   │                                        # (JWT 필터, OAuth2 소셜 로그인,
    │   │                                        #  CORS, 세션리스 STATELESS, URL 접근 제어)
    │   │
    │   ├── controller/
    │   │   ├── DeliveryController.java          # 배달 모집 REST API
    │   │   │                                    # (목록 조회, 생성, 상세, 모집 완료,
    │   │   │                                    #  삭제, 참여/취소, 내 참여 모집 조회)
    │   │   └── UserController.java              # 회원 REST API
    │   │                                        # (회원가입, 로그인, 아이디/닉네임 중복확인,
    │   │                                        #  이메일 인증, 내 정보 조회·수정, 비밀번호 확인)
    │   │
    │   ├── service/
    │   │   ├── DeliveryService.java             # 배달 모집 비즈니스 로직
    │   │   │                                    # (모집 완료 시 그룹 채팅방 생성을 하나의 트랜잭션으로 처리)
    │   │   └── UserService.java                 # 회원 비즈니스 로직
    │   │                                        # (회원가입, 자격증명 검증, 정보 수정)
    │   │
    │   ├── dao/
    │   │   ├── DeliveryDao.java                 # 배달 모집 MyBatis Mapper 인터페이스
    │   │   └── UserDao.java                     # 회원 MyBatis Mapper 인터페이스
    │   │
    │   ├── entity/
    │   │   ├── delivery/
    │   │   │   ├── DeliveryList.java            # 모집 목록 조회용 엔티티
    │   │   │   ├── DeliveryView.java            # 모집 상세 조회용 엔티티
    │   │   │   └── Participant.java             # 참여자 정보 엔티티 (userId, nickname)
    │   │   └── user/
    │   │       └── UserEntity.java              # 회원 엔티티
    │   │                                        # (loginId, password, email, nickname,
    │   │                                        #  userLocation, provider, providerId 등)
    │   │
    │   ├── dto/
    │   │   ├── delivery/
    │   │   │   ├── DeliveryCreateReqDTO.java    # 모집 생성 요청 DTO
    │   │   │   ├── DeliveryListReqDTO.java      # 목록 조회 요청 DTO (위치, 키워드, 정렬, 페이지)
    │   │   │   ├── DeliveryListResDTO.java      # 목록 조회 응답 DTO (list, totalPage)
    │   │   │   └── DeliveryViewResDTO.java      # 상세 조회 응답 DTO (참여자 포함)
    │   │   └── user/
    │   │       ├── UserDTO.java                 # 회원가입·로그인 요청/응답 DTO
    │   │       ├── MyInfoReqDTO.java            # 내 정보 수정 요청 DTO
    │   │       ├── MyInfoResDTO.java            # 내 정보 조회 응답 DTO
    │   │       ├── PasswordRequestDTO.java      # 비밀번호 확인 요청 DTO
    │   │       └── ResponseDTO.java             # 에러 응답 공통 DTO
    │   │
    │   ├── security/                            # 인증·인가 관련
    │   │   ├── TokenProvider.java               # JWT 토큰 생성·검증
    │   │   ├── JwtAuthenticationFilter.java     # 매 요청 JWT 검증 필터
    │   │   │                                    # (Authorization 헤더 파싱 → SecurityContext 주입)
    │   │   ├── OAuthSuccessHandler.java         # 소셜 로그인 성공 핸들러
    │   │   │                                    # (JWT 생성 → 쿠키의 redirect_url로 프론트에 토큰 전달)
    │   │   ├── RedirectUrlCookieFilter.java     # 소셜 로그인 전 redirect_url을 쿠키에 저장
    │   │   ├── dto/
    │   │   │   └── OAuthAttributes.java         # 소셜 플랫폼별 사용자 정보 통합 파싱
    │   │   │                                    # (Google·Naver·Kakao·GitHub의 서로 다른
    │   │   │                                    #  JSON 구조를 단일 객체로 정규화)
    │   │   ├── service/
    │   │   │   └── CustomOAuth2UserService.java # OAuth2 로그인 처리 서비스
    │   │   │                                    # (사용자 정보 로드 → DB 자동 회원가입 또는 조회
    │   │   │                                    #  → GitHub 이메일 비공개 시 별도 API 호출)
    │   │   └── vo/
    │   │       └── CustomUser.java              # Spring Security Principal 객체
    │   │                                        # (userId를 getName()으로 반환)
    │   │
    │   ├── chat/                                # 실시간 채팅 도메인 (STOMP WebSocket)
    │   │   ├── config/
    │   │   │   ├── StompWebSocketConfig.java    # WebSocket 엔드포인트(/connect) 및 브로커 설정
    │   │   │   │                                # (/publish → @MessageMapping, /topic → 구독)
    │   │   │   ├── StompHandler.java            # STOMP 연결 시 JWT 토큰 검증 인터셉터
    │   │   │   └── StompEventListener.java      # 연결·구독·해제 이벤트 처리
    │   │   ├── contorller/
    │   │   │   ├── ChatController.java          # 채팅 REST API
    │   │   │   │                                # (채팅방 목록, 이전 메시지 내역)
    │   │   │   └── StompController.java         # 메시지 발행 처리 (@MessageMapping)
    │   │   ├── service/
    │   │   │   └── ChatService.java             # 채팅 비즈니스 로직
    │   │   │                                    # (그룹방 생성, 메시지 저장·조회)
    │   │   ├── dao/
    │   │   │   └── ChatDao.java                 # 채팅 MyBatis Mapper 인터페이스
    │   │   ├── dto/
    │   │   │   ├── ChatRoom.java                # 채팅방 DTO
    │   │   │   ├── ChatMessage.java             # 채팅 메시지 DTO
    │   │   │   ├── ChatMessageDto.java          # 메시지 전송용 DTO
    │   │   │   ├── ChatParticipant.java         # 채팅 참여자 DTO
    │   │   │   ├── ChatRoomListResDto.java      # 채팅방 목록 응답 DTO
    │   │   │   ├── MyChatListResDto.java        # 내 채팅 목록 응답 DTO
    │   │   │   ├── ChatRoomAndMemberReqDto.java # 채팅방+회원 요청 DTO
    │   │   │   └── GroupChatCreateDto.java      # 그룹 채팅방 생성 요청 DTO
    │   │   │
    │   │   └── exception/
    │   │       └── NotFoundException.java       # 채팅 리소스 없음 예외
    │   │
    │   └── utils/
    │       └── EmailSender.java                 # Gmail SMTP 이메일 발송 (회원가입 인증)
    │
    └── resources/
        ├── application.properties               # DB, JWT, 이메일, OAuth2(Google·Naver·Kakao·GitHub) 설정
        └── mapper/                              # MyBatis SQL XML
            ├── UserMapper.xml
            ├── DeliveryMapper.xml
            └── ChatMapper.xml
```

---

## Frontend (`deli2matching_front/`)

**React 19 · Vite 8 · React Router v7 · Zustand · Axios · MUI · STOMP.js**

```
deli2matching_front/
├── package.json
├── vite.config.js
└── src/
    ├── main.jsx                              # 애플리케이션 진입점 (BrowserRouter 설정)
    ├── App.jsx                               # 라우팅 정의 (전체 페이지 경로 관리)
    │
    ├── pages/
    │   ├── user/
    │   │   ├── Login.jsx                     # 일반 로그인 (아이디·비밀번호)
    │   │   ├── SignUp.jsx                    # 회원가입 (아이디·닉네임 중복확인, 이메일 인증, 주소 검색)
    │   │   └── SocialLogin.jsx               # 소셜 로그인 콜백 처리
    │   │                                     # (URL의 ?token= 파라미터를 꺼내 Zustand에 저장 후 메인으로 이동)
    │   │
    │   ├── delivery/
    │   │   ├── DeliveryList.jsx              # 배달 모집 목록 (주소 필터, 키워드 검색, 정렬, 페이지네이션)
    │   │   ├── DeliveryRegist.jsx            # 배달 모집 등록 (식당명, 마감시간, 위치, 인원 설정)
    │   │   └── DeliveryView.jsx              # 배달 모집 상세
    │   │                                     # (참여자 목록, 참여/취소, 모집 완료 → 그룹 채팅방 자동 생성,
    │   │                                     #  삭제, 배달 완료 처리)
    │   │
    │   ├── mypage/
    │   │   ├── Mypage.jsx                    # 마이페이지 레이아웃 (사이드 메뉴 + 중첩 라우팅)
    │   │   ├── MyInfo.jsx                    # 내 정보 조회·수정·회원탈퇴
    │   │   │                                 # (소셜 로그인 사용자는 비밀번호 변경 불가)
    │   │   └── MyDelivery.jsx                # 내 참여 중인 배달 모집 조회 및 취소
    │   │
    │   ├── chat/
    │   │   ├── StompChatPage.jsx             # 실시간 채팅방
    │   │                                     # (STOMP WebSocket 연결, 이전 메시지 내역 로드,
    │   │                                     #  메시지 송수신, 읽음 처리, 모집 게시글 이동)
    │   │
    │   │
    │   ├── commons/
    │   │   └── Header.jsx                    # 전역 헤더 (로그인 상태, 소셜 로그인 버튼, 로그아웃)
    │   │
    │   └── ui/
    │       ├── BasicSelect.jsx               # 공통 셀렉트 박스 (MUI 기반)
    │       └── Pagination.jsx                # 공통 페이지네이션
    │
    └── utils/
        ├── useAuthStore.js                   # Zustand 전역 인증 상태 관리
        │                                     # (userId, memberId, memberName, memberEmail,
        │                                     #  memberAddr, token — localStorage 영속화)
        └── axios.js                          # Axios 인스턴스 (baseURL 설정)
                                              # · 요청 인터셉터: JWT 자동 헤더 주입
                                              # · 응답 인터셉터: 401 → 로그아웃 후 /login 이동
```

---

## 주요 기술 스택 요약

| 영역                | 기술                                    |
| ------------------- | --------------------------------------- |
| **Frontend**        | React 19, Vite 8, React Router v7       |
| **상태 관리**       | Zustand (localStorage 영속화)           |
| **UI 라이브러리**   | MUI v9, React Icons, SweetAlert2        |
| **HTTP 클라이언트** | Axios (JWT 자동 주입, 401 인터셉터)     |
| **실시간 통신**     | STOMP.js + SockJS                       |
| **주소 검색**       | 카카오 우편번호 API                     |
| **Backend**         | Spring Boot 4.0.6, Java 17              |
| **데이터 접근**     | MyBatis                                 |
| **데이터베이스**    | MySQL (AWS RDS)                         |
| **인증**            | JWT (jjwt 0.11) + Spring Security       |
| **소셜 로그인**     | OAuth2 — Google, Naver, Kakao, GitHub   |
| **이메일**          | Gmail SMTP (Spring Mail, 회원가입 인증) |
| **WebSocket**       | STOMP over SockJS                       |
| **배포**            | Docker Compose (EC2 + RDS + CloudFront) |

---

## 인증 흐름 요약

**일반 로그인**

```
클라이언트 → POST /auth/signin → JWT 발급 → Zustand(localStorage) 저장
→ 이후 모든 요청: Authorization: Bearer <token>
```

**소셜 로그인 (OAuth2)**

```
클라이언트 → 소셜 제공자 인증 → OAuthSuccessHandler
→ JWT 생성 → /sociallogin?token=<JWT> 로 리다이렉트
→ SocialLogin.jsx에서 token 파싱 → Zustand 저장 → 메인 페이지 이동
```

**배달 모집 완료 → 채팅방 자동 생성**

```
호스트가 "모집 완료" 클릭 → PATCH /delivery/{postId}/close
→ DeliveryService.closeDeliveryAndCreateGroupRoom() (단일 트랜잭션)
   → delivery status: open → close
   → 참여자 전원을 멤버로 한 그룹 채팅방 자동 생성
→ 참여자들이 /mychat/{roomId} 에서 실시간 채팅 가능
```
