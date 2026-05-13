package com.example.deli2matching.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter - JWT 토큰 검문소
 *
 * 이 필터는 모든 HTTP 요청이 들어올 때마다 실행되는 "검문소"
 *
 * 동작 방식:
 *  1. 요청 헤더에서 "Authorization: Bearer <토큰>" 형식의 토큰을 꺼냄
 *  2. 토큰이 있으면 TokenProvider로 유효성을 검증
 *  3. 유효하면 사용자 ID를 SecurityContext에 저장
 *  4. 이후 컨트롤러에서 @AuthenticationPrincipal로 사용자 ID를 꺼낼 수 있음
 *
 * OncePerRequestFilter: 같은 요청에서 이 필터가 딱 한 번만 실행되도록 보장
 */

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 토큰을 만들고 검증하는 도우미 객체
    private final TokenProvider tokenProvider;

    /**
     * shouldNotFilter - 이 필터를 건너뛸 조건 정의
     *
     * OPTIONS 요청: 브라우저가 실제 요청 전에 "이 요청 해도 돼?" 하고 물어보는 사전 요청
     * OPTIONS는 토큰 없이 보내므로 필터를 건너뜀
     *
     * @return true면 이 필터를 건너뜀, false면 필터 실행
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // OPTIONS 요청(브라우저의 CORS preflight)은 필터 통과
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        return false;
    }

    /**
     * doFilterInternal - 실제 필터 로직
     *
     * 매 요청마다 실행되며, JWT 토큰을 검사
     * 토큰이 유효하면 사용자 인증 정보를 SecurityContextHolder에 저장합니다.
     *
     * @param request  클라이언트의 HTTP 요청
     * @param response 서버의 HTTP 응답
     * @param filterChain 다음 필터로 요청을 넘기는 체인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1단계: 요청 헤더에서 Bearer 토큰 추출
            // "Authorization: Bearer eyJhbGc..." 형식에서 "eyJhbGc..." 부분만 꺼냄
            String token = parseBearerToken(request);

            // 2단계: 토큰이 있고 "null" 문자열이 아닌 경우에만 처리
            if (token != null && !token.equalsIgnoreCase("null")) {
                // 3단계: 토큰 유효성 검증 + 사용자 ID 추출
                // 토큰이 위조되거나 만료되면 예외 발생
                String userId = tokenProvider.validateAndGetUserId(token);
                log.info("Authenticated user ID : " + userId);

                // 4단계: 스프링 시큐리티 인증 객체 생성
                // userId를 principal(주체)로, 권한은 없음으로 설정
                AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,                         // principal: 사용자 ID (컨트롤러에서 @AuthenticationPrincipal로 꺼냄)
                        null,                           // credentials: 비밀번호 (JWT 방식에서는 필요 없음)
                        AuthorityUtils.NO_AUTHORITIES   // authorities: 권한 목록 (비어있음)
                );

                // 요청 정보(IP 주소 등)를 인증 객체에 추가
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5단계: SecurityContext에 인증 정보 저장
                // 이후 컨트롤러에서 SecurityContextHolder.getContext().getAuthentication()으로 꺼낼 수 있음
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                securityContext.setAuthentication(authentication);
                SecurityContextHolder.setContext(securityContext);
            }
            // 토큰이 없으면? 그냥 통과! (다음 필터로 넘어감)
            // 하지만 SecurityContext에 인증 정보가 없으므로 보호된 API는 접근 불가

        } catch (Exception ex) {
            // 토큰 검증 실패 시 에러 로그만 남기고 계속 진행
            // (다음 필터에서 인증 없음으로 처리됨)
            log.error("Could not set user authentication in security context", ex);
        }

        // 다음 필터로 요청을 넘깁니다 (필터 체인의 다음 단계로 이동)
        filterChain.doFilter(request, response);
    }

    /**
     * parseBearerToken - Authorization 헤더에서 Bearer 토큰 추출
     *
     * HTTP 요청 헤더 형식:
     *   Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOi...
     *                  ^^^^^^^ 이 부분을 제외하고 나머지만 반환
     *
     * @param request HTTP 요청 객체
     * @return 토큰 문자열, 없으면 null
     */
    private String parseBearerToken(HttpServletRequest request) {
        // Authorization 헤더 값 가져오기
        String bearerToken = request.getHeader("Authorization");

        // "Bearer "로 시작하는 경우에만 처리 (7글자 이후부터 실제 토큰)
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " (7글자) 제거
        }

        return null; // 토큰 없음
    }
}

