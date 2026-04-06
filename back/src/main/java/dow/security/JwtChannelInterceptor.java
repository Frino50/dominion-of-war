package dow.security;

import dow.exception.JwtAuthenticationException;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final Map<String, Principal> sessionUserMap = new ConcurrentHashMap<>();

    public JwtChannelInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        if (command == null) return message;

        String sessionId = accessor.getSessionId();

        switch (command) {
            case CONNECT -> handleConnect(accessor, sessionId);
            case DISCONNECT -> sessionUserMap.remove(sessionId);
            default -> restoreUser(accessor, sessionId);
        }
        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor, String sessionId) {
        String jwt = jwtUtils.extractToken(accessor.getFirstNativeHeader("Authorization"));
        try {
            UsernamePasswordAuthenticationToken auth = jwtUtils.getAuthenticationFromToken(jwt);
            accessor.setUser(auth);
            sessionUserMap.put(sessionId, auth);
        } catch (JwtAuthenticationException e) {
            throw new JwtAuthenticationException("INVALID_OR_EXPIRED_TOKEN");
        }
    }

    private void restoreUser(StompHeaderAccessor accessor, String sessionId) {
        if (accessor.getUser() == null) {
            Principal user = sessionUserMap.get(sessionId);
            if (user != null) accessor.setUser(user);
        }
    }
}