package dow.service;

import dow.model.CustomUserDetails;
import dow.model.entities.Player;
import dow.repository.PlayerRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public Set<String> getRoles() {
        try {
            CustomUserDetails principal = principal();

            if (principal == null) {
                return Collections.emptySet();
            }

            return principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
        } catch (IllegalStateException e) {
            return Collections.emptySet();
        }
    }

    public Player getPlayer() {
        String pseudo = getPseudo();
        return playerRepository.findByPseudo(pseudo)
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé : " + pseudo));
    }
}