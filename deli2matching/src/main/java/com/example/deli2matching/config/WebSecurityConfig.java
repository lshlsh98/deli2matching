package com.example.deli2matching.config;


import com.example.deli2matching.security.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.deli2matching.security.JwtAuthenticationFilter;
import com.example.deli2matching.security.OAuthSuccessHandler;
import com.example.deli2matching.security.RedirectUrlCookieFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
// @EnableWebSecurity: 스프링 시큐리티(보안 기능)를 켜
@EnableWebSecurity
public class WebSecurityConfig {

    // JWT 토큰을 검사하는 필터 (매 요청마다 토큰 유효성 확인)
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 소셜 로그인 성공 후 처리를 담당하는 핸들러
    private final OAuthSuccessHandler oAuthSuccessHandler;

    // 소셜 로그인 전에 "돌아갈 주소(redirect_url)"를 쿠키에 저장하는 필터
    private final RedirectUrlCookieFilter redirectUrlFilter;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                             OAuthSuccessHandler oAuthSuccessHandler,
                             RedirectUrlCookieFilter redirectUrlFilter,
                             HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuthSuccessHandler = oAuthSuccessHandler;
        this.redirectUrlFilter = redirectUrlFilter;
        this.cookieAuthorizationRequestRepository = cookieAuthorizationRequestRepository;
    }

    @Bean
    // @Bean: 스프링이 이 메서드의 반환값을 관리하도록 등록
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용 (아래 corsConfigurationSource() 메서드에서 정의)
                .cors(cors -> {})

                // CSRF 보호 비활성화
                // SPA(Single Page Application) + JWT 방식에서는 CSRF 공격 위험이 낮아 비활성화
                .csrf(csrf -> csrf.disable())

                // HTTP Basic 인증 비활성화 (ID/PW를 헤더에 직접 넣는 구식 방식)
                .httpBasic(httpBasic -> httpBasic.disable())

                // 세션 사용 안 함 (STATELESS)
                // 서버가 사용자 정보를 메모리에 저장하지 않음
                // 대신 매 요청마다 JWT 토큰으로 사용자를 확인
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // "/" 와 "/auth/**" 경로는 로그인 없이 누구나 접근 가능
                        // (회원가입, 로그인 API)
                        .requestMatchers("/", "/auth", "/auth/**", "/delivery", "/delivery/**", "/connect/**",
                                "/oauth2/**", "/login/oauth2/**").permitAll()

                        // 나머지 모든 요청은 로그인(인증) 필수
                        .anyRequest().authenticated()
                )

                // JWT 검사 필터를 UsernamePasswordAuthenticationFilter 다음에 추가
                // 즉, 모든 요청에서 JWT 토큰을 검사함
                .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // OAuth2 소셜 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        // state를 세션 대신 쿠키에 저장 (STATELESS 세션과 호환)
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository)
                        )
                        .redirectionEndpoint(endpoint -> endpoint
                                .baseUri("/login/oauth2/code/*")
                        )
                        .successHandler(oAuthSuccessHandler)
                )

                // 인증 실패 시 처리
                // 로그인 안 된 상태에서 보호된 페이지 접근 시 403 에러 반환
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
                )

                // 소셜 로그인 요청 전에 redirect_url을 쿠키에 저장하는 필터 추가
                // OAuth2AuthorizationRequestRedirectFilter 보다 먼저 실행
                .addFilterBefore(redirectUrlFilter, OAuth2AuthorizationRequestRedirectFilter.class);

        return http.build(); // 설정 완료 보안 필터 체인 생성
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 쿠키 포함 요청 허용 (소셜 로그인 redirect_url 쿠키 전달에 필요)
        configuration.setAllowCredentials(true);

        // 허용할 프론트엔드 주소
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://d13l6nklwxfs5s.cloudfront.net"
        ));

        // 허용할 HTTP 메서드들
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 요청 헤더 (모두 허용)
        configuration.setAllowedHeaders(List.of("*"));

        // 응답에서 클라이언트가 읽을 수 있는 헤더 (모두 노출)
        configuration.setExposedHeaders(List.of("*"));

        // 모든 경로("/**")에 위 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}