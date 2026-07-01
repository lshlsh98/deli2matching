package com.example.deli2matching.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.example.deli2matching.entity.user.UserEntity;
import com.example.deli2matching.security.vo.CustomUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenProvider {

    private final Key SIGNING_KEY;

    public TokenProvider(
            @Value("${jwt.secret-key}") String SECRET_KEY
    ) {
        // SECRET_KEY를 HMAC-SHA 방식으로 사용할 수 있는 Key 객체로 변환
        this.SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

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
