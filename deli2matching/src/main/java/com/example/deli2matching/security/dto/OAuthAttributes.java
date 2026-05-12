package com.example.deli2matching.security.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * OAuthAttributes - 소셜 플랫폼 사용자 정보 통합 DTO
 *
 * 문제:
 *  구글, 네이버, 카카오, 깃허브마다 사용자 정보를 다른 방식으로 줌
 *  예를 들어 이름 필드가:
 *   - 구글: attributes["name"]
 *   - 네이버: attributes["response"]["name"]  (중첩 구조)
 *   - 카카오: attributes["kakao_account"]["profile"]["nickname"]  (2단 중첩)
 *   - 깃허브: attributes["login"]
 *
 * 해결:
 *  OAuthAttributes가 플랫폼별로 다른 구조를 파싱해서
 *  하나의 통일된 객체로 만들기
 *  이후 코드는 OAuthAttributes만 보면 됨
 */

@Getter
@Slf4j
public class OAuthAttributes {

    // 원본 사용자 정보 맵 (소셜 플랫폼에서 받은 그대로)
    private Map<String, Object> attributes;

    // 사용자 식별 키 이름 (각 플랫폼의 사용자 ID 필드명)
    private String nameAttributeKey;

    private String name;    // 사용자 이름 (닉네임)
    private String email;   // 이메일
    private String picture; // 프로필 사진 URL
    private String id;      // 소셜 플랫폼 내 고유 ID

    /**
     * @Builder: Builder 패턴을 자동으로 만들어줌
     * OAuthAttributes.builder().name("홍길동").email("a@b.com").build() 처럼 사용
     */
    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
                           String name, String email, String picture, String id) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.id = id;
    }

    /**
     * of - 소셜 플랫폼에 맞는 OAuthAttributes 생성 (팩토리 메서드)
     *
     * registrationId로 어떤 플랫폼인지 판별하고, 해당 파싱 메서드를 호출합니다.
     *
     * @param registrationId      플랫폼 이름 ("google", "naver", "kakao", "github")
     * @param userNameAttributeName 사용자 식별 키 이름
     * @param attributes          소셜 플랫폼에서 받은 원본 사용자 정보 Map
     * @return 통일된 OAuthAttributes 객체
     */
    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
                                     Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        } else if ("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        } else if ("github".equals(registrationId)) {
            return ofGitHub("id", attributes);
        }
        // 기본값: 구글
        return ofGoogle(userNameAttributeName, attributes);
    }

    /**
     * ofGoogle - 구글 사용자 정보 파싱
     *
     * 구글 응답 예시 (평탄한 구조):
     * {
     *   "sub": "123456789",      ← 구글 고유 ID (userNameAttributeName = "sub")
     *   "name": "홍길동",
     *   "email": "user@gmail.com",
     *   "picture": "https://..."
     * }
     */
    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        log.info("attributes = " + attributes);
        log.info("userNameAttributeName = " + userNameAttributeName);

        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .id((String) attributes.get(userNameAttributeName)) // "sub" 값
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /**
     * ofNaver - 네이버 사용자 정보 파싱
     *
     * 네이버 응답 예시 (response 안에 중첩):
     * {
     *   "resultcode": "00",
     *   "message": "success",
     *   "response": {            ← 실제 사용자 정보는 response 안에!
     *     "id": "abcdef123",
     *     "name": "홍길동",
     *     "email": "user@naver.com",
     *     "profile_image": "https://..."
     *   }
     * }
     */
    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        // "response" 키로 중첩된 Map 꺼내기
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        log.info("response = " + response);
        log.info("userNameAttributeName = " + userNameAttributeName);

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .picture((String) response.get("profile_image")) // 키 이름이 다름!
                .id((String) response.get(userNameAttributeName)) // response 안의 "id"
                .attributes(response) // response 맵을 저장 (중첩 해제)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /**
     * ofKakao - 카카오 사용자 정보 파싱
     *
     * 카카오 응답 예시 (2단 중첩 구조):
     * {
     *   "id": 1234567890,          ← 카카오 고유 ID (Long 타입!)
     *   "kakao_account": {
     *     "email": "user@kakao.com",
     *     "profile": {             ← 프로필은 2단 중첩!
     *       "nickname": "홍길동",
     *       "profile_image_url": "https://..."
     *     }
     *   }
     * }
     */
    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        // 카카오 ID는 Long 타입 (구글/네이버와 다름!)
        Long id = (Long) attributes.get("id");

        // 1단계 중첩 해제
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        log.info("kakaoAccount = " + kakaoAccount);
        log.info("userNameAttributeName = " + userNameAttributeName);

        // 2단계 중첩 해제
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = (String) profile.get("nickname");
        String profileImageUrl = (String) profile.get("profile_image_url");

        String email = (String) kakaoAccount.get("email");

        return OAuthAttributes.builder()
                .name(nickname)
                .email(email)
                .picture(profileImageUrl)
                .id("" + id) // Long을 String으로 변환
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /**
     * ofGitHub - 깃허브 사용자 정보 파싱
     *
     * 깃허브 응답 예시 (평탄한 구조, 필드명이 다름):
     * {
     *   "id": 12345678,             ← Integer 타입!
     *   "login": "username",        ← 이름은 "login" 필드
     *   "avatar_url": "https://...", ← 프로필 사진은 "avatar_url"
     *   "email": null               ← 비공개 설정 시 null일 수 있음!
     * }
     */
    private static OAuthAttributes ofGitHub(String userNameAttributeName, Map<String, Object> attributes) {
        log.info("attributes = " + attributes);
        log.info("userNameAttributeName = " + userNameAttributeName);

        String username = (String) attributes.get("login"); // 깃허브 사용자명
        Integer id = (Integer) attributes.get("id");       // Integer 타입 (구글/네이버는 String)
        String nickname = username;
        String profileImageUrl = (String) attributes.get("avatar_url"); // 필드명 다름!
        String email = (String) attributes.get("email");   // null일 수 있음

        return OAuthAttributes.builder()
                .name(nickname)
                .email(email)  // null이면 CustomOAuth2UserService에서 별도 API 호출로 채움
                .picture(profileImageUrl)
                .id("" + id)   // Integer를 String으로 변환
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

}
