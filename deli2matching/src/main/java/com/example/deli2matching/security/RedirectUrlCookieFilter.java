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

/**
 * RedirectUrlCookieFilter - "돌아갈 주소" 쿠키 저장 필터
 *
 * 소셜 로그인의 핵심 문제:
 *  사용자가 "구글로 로그인" 버튼을 클릭하면 구글 페이지로 이동
 *  구글에서 로그인을 마치면 우리 서버로 돌아오는데,
 *  "어디로 다시 보내줘야 하지?" 라는 정보가 필요
 *
 * 해결 방법:
 *  소셜 로그인 요청 시 redirect_url 파라미터로 "돌아갈 주소"를 전달
 *  이 필터는 그 주소를 쿠키에 저장해두었다가,
 *  로그인 성공 후 OAuthSuccessHandler에서 꺼내 사용
 *
 * 흐름:
 *  [프론트] 클릭 → /oauth2/authorization/google?redirect_url=http://localhost:5173
 *          ↓
 *  [이 필터] redirect_url 값을 쿠키에 저장
 *          ↓
 *  [구글] 로그인 페이지
 *          ↓
 *  [우리 서버] 로그인 성공 처리
 *          ↓
 *  [OAuthSuccessHandler] 쿠키에서 redirect_url 꺼내서 프론트로 리다이렉트
 * =====================================================================
 */

@Slf4j
@Component
public class RedirectUrlCookieFilter extends OncePerRequestFilter {

    /**
     * REDIRECT_URI_PARAM - 쿠키 이름이자 쿼리 파라미터 이름
     *
     * 프론트엔드가 요청할 때:
     *   /oauth2/authorization/google?redirect_url=http://localhost:5173
     *   여기서 "redirect_url"이 파라미터 이름
     *
     * OAuthSuccessHandler에서 같은 상수를 사용해서 쿠키를 읽습니다.
     */
    public static final String REDIRECT_URI_PARAM = "redirect_url";

    /**
     * MAX_AGE - 쿠키 유효 시간 (초 단위)
     * 180초 = 3분
     * 소셜 로그인은 보통 3분 안에 완료되므로 충분
     */
    private static final int MAX_AGE = 180;

    /**
     * doFilterInternal - 실제 필터 로직
     *
     * /oauth2/authorization 으로 시작하는 요청(소셜 로그인 시작)에서만 동작
     * redirect_url 파라미터를 읽어 쿠키에 저장
     */
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

