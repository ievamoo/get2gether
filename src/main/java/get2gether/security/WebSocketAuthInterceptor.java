package get2gether.security;

import get2gether.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
/// ensure it's used before spring securities interceptor
public class WebSocketAuthInterceptor implements ChannelInterceptor {


    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Intercepts WebSocket messages before they are sent to validate authentication for STOMP connect frames.
     * For CONNECT commands, it:
     * 1. Extracts the JWT token from the Authorization header
     * 2. Validates the token and extracts the username
     * 3. Loads the user details
     * 4. Sets the authenticated user as the principal in the WebSocket context
     * 
     * Other STOMP frames (SEND, SUBSCRIBE) use the principal set during connection.
     *
     * @param message the message being sent
     * @param channel the channel the message is being sent through
     * @return the original message, potentially modified with authentication information
     */
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        assert accessor != null;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            /// this checks if the command being sent is a CONNECT command
            /// a user will not be able to send any frames unless they are connected to a STOMP protocol
            var authHeaderList = accessor.getNativeHeader("Authorization");

            assert authHeaderList != null;
            ///header returns a list of strings
            String authHeader = authHeaderList.get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String jwt = authHeader.substring(7);
                String username = jwtUtil.extractUsername(jwt);
                log.info("username: {}", username);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticatedUser = new UsernamePasswordAuthenticationToken(userDetails,
                        null,
                        userDetails.getAuthorities());
                accessor.setUser(authenticatedUser); ///setting the context of the user as the principal

            } else {
                log.info("Authorization header not present");
            }

        }
        /// if any other frames are being sent they don't need authentication as it is already set
        return message;
    }
}
