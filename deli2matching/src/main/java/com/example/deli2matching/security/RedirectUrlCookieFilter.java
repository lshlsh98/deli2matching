package com.example.deli2matching.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RedirectUrlCookieFilter extends OncePerRequestFilter {

    public static final String REDIRECT_URI_PARAM = "redirect_url";
    private static final int MAX_AGE = 180;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 소셜 로그인 시작 URL인 경우에만 처리
        // 예: /oauth2/authorization/google, /oauth2/authorization/naver 등
        if (request.getRequestURI().startsWith("/oauth2/authorization")) {
            try {
                // 쿼리 파라미터에서 redirect_url 값 추출
                // 예: ?redirect_url=http://localhost:5173 → "http://localhost:5173"
                String redirectUrl = request.getParameter(REDIRECT_URI_PARAM);

                // 쿠키 생성: 이름 = "redirect_url", 값 = 프론트엔드 주소
                Cookie cookie = new Cookie(REDIRECT_URI_PARAM, redirectUrl);
                cookie.setPath("/");       // 모든 경로에서 이 쿠키 사용 가능
                cookie.setHttpOnly(true);  // JavaScript에서 쿠키 접근 불가 (XSS 공격 방지)
                cookie.setMaxAge(MAX_AGE); // 쿠키 유효 시간 3분

                // 응답에 쿠키 추가 (브라우저가 이 쿠키를 저장함)
                response.addCookie(cookie);

            } catch (Exception ex) {
                log.error("Could not set user authentication in security context", ex);
                log.info("Unauthorized request");
            }
        }

        // 소셜 로그인 시작 URL이 아닌 경우엔 그냥 다음 필터로 넘어감
        filterChain.doFilter(request, response);
    }

}

