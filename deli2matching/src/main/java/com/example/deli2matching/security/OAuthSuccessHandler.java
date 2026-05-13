package com.example.deli2matching.security;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static com.example.deli2matching.security.RedirectUrlCookieFilter.REDIRECT_URI_PARAM;

/**
 * OAuthSuccessHandler - 소셜 로그인 성공 처리기
 *
 * 소셜 로그인(구글, 네이버, 카카오, 깃허브)이 성공하면 이 클래스가 실행
 *
 * 역할:
 *  1. 로그인 성공 후 JWT 토큰을 생성
 *  2. 쿠키에 저장해둔 redirect_url을 읽음
 *  3. 프론트엔드로 토큰을 전달하면서 리다이렉트 함
 *
 * 최종 리다이렉트 URL 형식:
 *   http://localhost:5173/sociallogin?token=eyJhbGc...
 *   (프론트엔드 주소)/sociallogin?token=(JWT 토큰)
 *
 * 프론트엔드는 이 URL에서 token 파라미터를 꺼내 저장하고,
 * 이후 모든 API 요청 시 "Authorization: Bearer <토큰>" 헤더로 전송
 *
 * SimpleUrlAuthenticationSuccessHandler: 스프링이 제공하는 성공 핸들러 기본 클래스
 */

@Slf4j
@AllArgsConstructor
@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    /**
     * LOCAL_REDIRECT_URL - redirect_url 쿠키가 없을 때 기본 리다이렉트 주소
     * 개발 환경에서 프론트엔드가 localhost:5173에서 실행될 때 사용
     */
    private static final String LOCAL_REDIRECT_URL = "http://localhost:5173";

    /**
     * onAuthenticationSuccess - 소셜 로그인 성공 시 호출되는 메서드
     *
     * 스프링 시큐리티가 OAuth2 인증을 완료하면 자동으로 이 메서드를 호출
     *
     * @param request        HTTP 요청 (쿠키에서 redirect_url 꺼내기 위해 필요)
     * @param response       HTTP 응답 (리다이렉트를 보내기 위해 필요)
     * @param authentication 소셜 로그인으로 인증된 사용자 정보 (CustomUser 포함)
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 1단계: JWT 토큰 생성
        // authentication 안에는 CustomOAuth2UserService가 반환한 CustomUser가 들어있음
        // CustomUser.getName()이 DB의 user id를 반환하므로, 토큰 subject = user id
        TokenProvider tokenProvider = new TokenProvider();
        String token = tokenProvider.create(authentication);
        log.info("token {}", token);

        // 2단계: 쿠키에서 redirect_url 꺼내기
        // RedirectUrlCookieFilter가 저장한 "redirect_url" 쿠키를 찾습니다
        Optional<Cookie> oCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(REDIRECT_URI_PARAM)) // 이름이 "redirect_url"인 쿠키 필터링
                .findFirst(); // 첫 번째(= 유일한) 결과 가져오기

        // Optional로 감싸서 null 안전하게 처리
        Optional<String> redirectUri = oCookie.map(cookie -> cookie.getValue());
        log.info("redirectUri {}", redirectUri);

        // 3단계: 리다이렉트 URL 조합
        // 쿠키에 redirect_url이 있으면 그 주소로, 없으면 기본 주소로
        // 토큰은 쿼리 파라미터로 전달: ?token=eyJhbGc...
        String targetUrl = redirectUri.orElseGet(() -> LOCAL_REDIRECT_URL) + "/sociallogin?token=" + token;
        log.info("targetUrl {}", targetUrl);

        // 4단계: 프론트엔드로 리다이렉트
        // 브라우저가 이 URL로 자동으로 이동합니다
        // 프론트엔드는 /sociallogin 페이지에서 토큰을 꺼내 localStorage에 저장
        response.sendRedirect(targetUrl);
    }

}
