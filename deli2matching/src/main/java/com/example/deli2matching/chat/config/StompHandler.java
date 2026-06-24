package com.example.deli2matching.chat.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
	
	@Value("${jwt.secret-key}")
	private String secretKey;
	
	private final ChatService chatService;
	
	@Override
	public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
		final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (accessor.getCommand() == null) {
		    return message;
		}

		SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
		if(StompCommand.CONNECT == accessor.getCommand()) {
//			System.out.println("connect 요청 시 토큰 유효성 검증");
			String token = accessor.getFirstNativeHeader("Authorization");
			
			// 토큰 검증
			Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
//			System.out.println("토큰 검증 완료");
		}
		
		if(StompCommand.SUBSCRIBE == accessor.getCommand()) {
//			System.out.println("subscribe 검증");
			String token = accessor.getFirstNativeHeader("Authorization");
			
			// 토큰 검증
			Claims claims = Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
			
			String memberId = claims.get("memberId", String.class);
			String roomId = accessor.getDestination().split("/")[2];
			if(!chatService.isRoomParticipant(memberId, Long.parseLong(roomId))) {
				throw new AuthenticationServiceException("해당 room에 권한이 없습니다.");
			}
		}
		
		return message;
	}//
	
	
}
