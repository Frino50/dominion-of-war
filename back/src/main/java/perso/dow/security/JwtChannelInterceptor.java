package perso.dow.security;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import perso.dow.exception.JwtAuthenticationException;

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
            case DISCONNECT -> handleDisconnect(sessionId);
            default -> handleOtherCommand(accessor, sessionId, command);
        }
        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor, String sessionId) {
        String jwt = jwtUtils.extractToken(accessor.getFirstNativeHeader("Authorization"));
        try {
            UsernamePasswordAuthenticationToken auth = jwtUtils.getAuthenticationFromToken(jwt);
            accessor.setUser(auth);
            sessionUserMap.put(sessionId, auth);
            System.out.println("STOMP CONNECT de l'utilisateur : " + auth.getName());
        } catch (JwtAuthenticationException e) {
            System.out.println("STOMP CONNECT refusé : JWT invalide ou absent");
        }
    }

    private void handleDisconnect(String sessionId) {
        Principal user = sessionUserMap.remove(sessionId);
        if (user != null) System.out.println("Session STOMP déconnectée pour : " + user.getName());
    }

    private void handleOtherCommand(StompHeaderAccessor accessor, String sessionId, StompCommand command) {
        Principal user = accessor.getUser();
        if (user == null) {
            user = sessionUserMap.get(sessionId);
            if (user != null) accessor.setUser(user);
        }
        if (user == null) {
            System.out.println("Commande STOMP " + command + " bloquée : utilisateur non authentifié");
        } else {
            System.out.println("STOMP " + command + " de l'utilisateur : " + user.getName()
                    + " sur " + accessor.getDestination());
        }
    }
}