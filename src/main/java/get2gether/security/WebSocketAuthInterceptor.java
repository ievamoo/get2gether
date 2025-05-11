package get2gether.security;

import get2gether.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("[WebSocket] Processing CONNECT command with auth header: {}", authHeader != null ? "present" : "missing");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);
                log.info("[WebSocket] Extracted username from token: {}", username);

                if (username != null && jwtUtil.isTokenValid(token)) {
                    try {
                        List<GrantedAuthority> authorities = jwtUtil.extractRoles(token);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);

                        accessor.setUser(authentication);

                        log.info("[WebSocket] Successfully authenticated user: {}", username);
                        log.info("[WebSocket] User principal set to: {}", accessor.getUser().getName());

                        // 👇 Ensure the authentication is also added to headers
                        return MessageBuilder.createMessage(
                                message.getPayload(),
                                accessor.getMessageHeaders()
                        );

                    } catch (Exception e) {
                        log.error("[WebSocket] Error during authentication: {}", e.getMessage());
                        return null;
                    }
                } else {
                    log.warn("[WebSocket] Invalid JWT or missing username");
                    return null;
                }
            } else {
                log.warn("[WebSocket] Missing or malformed Authorization header");
                return null;
            }
        }

        return message;
    }

}
