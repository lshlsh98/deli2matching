package com.example.deli2matching.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	private final StompHandler stompHandler;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// 클라이언트가 WebSocket 연결을 맺을 때 사용할 엔드포인트 경로를 /connect로 지정
		registry.addEndpoint("/connect")
				.setAllowedOriginPatterns("http://localhost:5173", "https://d13l6nklwxfs5s.cloudfront.net")
				// ws:// 가 아닌 http:// 엔드포인트를 사용할 수 있게 해주는 sockJS 라이브러리를 통한 요청을 허용하는 설정
				.withSockJS();
	}//
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// /publish/1 형태로 메시지를 발행해야 함을 설정
		// /publish 로 시작하는 url 패턴으로 메시지가 발행되면 @Controller 객체의 @MessageMapping 메서드로 라우팅
		// 클라이언트가 서버로 메시지를 보낼 때 사용하는 prefix
		// /publish로 시작하는 경로로 보낸 메시지는 서버의 @MessageMapping으로 라우팅
		registry.setApplicationDestinationPrefixes("/publish");
		
		// /topic/1 형태로 메시지를 수신(subscribe)해야 함을 설정
		// 클라이언트가 구독(subscribe)한 /topic/... 경로로 메시지를 전달(브로드캐스트)
		registry.enableSimpleBroker("/topic");
	}//
	
	// 웹소켓 요청(connect, subscribe, disconnect) 등의 요청시에는 http header 등 http 메시지를 넣어올 수 있고, 이를 interceptor 를 통해 가로채 토큰등을 검증할 수 있음 
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompHandler);
	}//
}
