package dow.service;

import dow.model.CustomUserDetails;
import dow.model.entities.Player;
import dow.repository.PlayerRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UtilsService {

    private final PlayerRepository playerRepository;

    public UtilsService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    private CustomUserDetails principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken
                || !(auth.getPrincipal() instanceof CustomUserDetails ud)) {
            throw new IllegalStateException("Utilisateur non authentifié");
        }
        return ud;
    }

    public String getPseudo() {
        return principal().getPseudo();
    }

    public UUID getId() {
        return principal().getId();
    }

    public Player getPlayer() {
        return playerRepository.findById(getId())
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé"));
    }
}