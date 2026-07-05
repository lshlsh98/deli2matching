package com.example.deli2matching.security;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static com.example.deli2matching.security.RedirectUrlCookieFilter.REDIRECT_URI_PARAM;

@Slf4j
@AllArgsConstructor
@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private TokenProvider tokenProvider;

//    private static final String LOCAL_REDIRECT_URL = "http://localhost:5173";
    private static final String LOCAL_REDIRECT_URL = "https://d13l6nklwxfs5s.cloudfront.net";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 1단계: JWT 토큰 생성
        // authentication 안에는 CustomOAuth2UserService가 반환한 CustomUser가 들어있음
        // CustomUser.getName()이 DB의 user id를 반환하므로, 토큰 subject = user id
        String token = tokenProvider.create(authentication);
        log.info("token {}", token);

        // 2단계: 쿠키에서 redirect_url 꺼내기
        Cookie[] cookies = request.getCookies();
        Optional<String> redirectUri = (cookies == null) ? Optional.empty() :
                Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals(REDIRECT_URI_PARAM))
                        .findFirst()
                        .map(Cookie::getValue);
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
