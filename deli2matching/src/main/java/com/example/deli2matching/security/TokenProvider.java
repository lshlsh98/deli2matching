package com.example.deli2matching.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.example.deli2matching.entity.UserEntity;
import com.example.deli2matching.security.vo.CustomUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * TokenProvider - JWT 토큰 발행소
 *
 * JWT 구조 (세 부분이 점(.)으로 구분):
 *  헤더.페이로드.서명
 *  - 헤더: 토큰 타입, 암호화 알고리즘 정보
 *  - 페이로드: 사용자 ID, 발급 시간, 만료 시간 등 실제 데이터
 *  - 서명: 위변조 방지를 위한 암호화된 서명
 *
 * 예시 JWT:
 *  eyJhbGc...헤더...  .eyJ1c2Vy...페이로드...  .SflKxw...서명...
 * =====================================================================
 */
@Component
public class TokenProvider {

    /**
     * SECRET_KEY - 토큰을 만들고 검증할 때 쓰는 비밀 열쇠
     *
     * 이 키로 서명하고, 같은 키로만 서명을 확인할 수 있음
     * 실제 서비스에서는 절대 코드에 직접 쓰면 안 됨
     * 환경변수나 별도 설정 파일에 저장해야 함
     */

    private final Key SIGNING_KEY;

    public TokenProvider(
            @Value("${jwt.secret-key}") String SECRET_KEY
    ) {
        // SECRET_KEY를 HMAC-SHA 방식으로 사용할 수 있는 Key 객체로 변환
        this.SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * create(UserEntity) - 일반 로그인용 JWT 토큰 생성
     *
     * 일반 회원가입/로그인 시 사용
     * UserEntity의 id를 토큰 안에 담아서 반환
     *
     * @param userEntity 토큰을 발급할 사용자 정보
     * @return 생성된 JWT 토큰 문자열
     */
    public String create(UserEntity userEntity) {
        // 토큰 만료 시간: 지금으로부터 1일 후
        Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

        return Jwts.builder()
                // 서명 알고리즘: HS512 (HMAC-SHA512, 매우 강력한 암호화)
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
                // subject: 토큰의 주인 (사용자 ID를 문자열로 저장)
                .setSubject(String.valueOf(userEntity.getUserId()))
                // issuer: 토큰 발급자 이름
                .setIssuer("d2m app")
                // issuedAt: 토큰 발급 시간 (지금)
                .setIssuedAt(new Date())
                // expiration: 토큰 만료 시간 (1일 후)
                .setExpiration(expiryDate)
                // compact(): 최종 JWT 문자열로 압축
                .compact();
    }

    /**
     * validateAndGetUserId - JWT 토큰 검증 및 사용자 ID 추출
     *
     * 클라이언트가 보낸 토큰이 유효한지 확인하고,
     * 토큰 안에 담긴 사용자 ID를 꺼내서 반환
     *
     * 토큰이 위조되었거나 만료되었으면 예외(Exception)가 발생
     *
     * @param token 검증할 JWT 토큰 문자열
     * @return 토큰 안에 담긴 사용자 ID
     */
    public String validateAndGetUserId(String token) {
        // Claims: 토큰 안에 담긴 데이터 꾸러미
        Claims claims = Jwts.parserBuilder()
                // 서명 검증에 사용할 키 설정 (발급할 때와 같은 키)
                .setSigningKey(SIGNING_KEY)
                .build()
                // 토큰 파싱 & 서명 검증 (위조 또는 만료 시 예외 발생)
                .parseClaimsJws(token)
                // 페이로드(데이터 부분)만 가져오기
                .getBody();

        // subject에서 사용자 ID 반환
        return claims.getSubject();
    }

    /**
     * create(Authentication) - 소셜 로그인용 JWT 토큰 생성
     *
     * 소셜 로그인 성공 후 OAuthSuccessHandler에서 호출
     * Authentication 객체 안의 CustomUser에서 사용자 ID를 꺼내 토큰을 만듬
     *
     * @param authentication 소셜 로그인 성공 후 스프링 시큐리티가 생성한 인증 정보
     * @return 생성된 JWT 토큰 문자열
     */
    public String create(final Authentication authentication) {
        // authentication.getPrincipal(): 인증된 사용자 정보 (CustomUser 객체)
        // CustomUser.getName()은 사용자의 DB id를 문자열로 반환 (CustomUser.java 참고)
        CustomUser userPrincipal = (CustomUser) authentication.getPrincipal();

        Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

        return Jwts.builder()
                // CustomUser.getName() = DB의 user id (Long을 String으로 변환한 값)
                .setSubject(userPrincipal.getName())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * createByUserId - 사용자 ID로 직접 JWT 토큰 생성
     *
     * 필요에 따라 사용자 ID만으로 토큰을 만들 때 사용합니다.
     *
     * @param userId DB에 저장된 사용자 고유 번호
     * @return 생성된 JWT 토큰 문자열
     */
    public String createByUserId(final Long userId) {
        Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
                .compact();
    }
}
