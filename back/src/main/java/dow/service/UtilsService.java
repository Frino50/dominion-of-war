package dow.service;

import dow.model.entities.Player;
import dow.repository.PlayerRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UtilsService {

    private final PlayerRepository playerRepository;

    public UtilsService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public String getPseudo() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Utilisateur non authentifié");
        }

        return authentication.getName();
    }


    public Player getPlayer() {
        String pseudo = getPseudo();
        return playerRepository.findByPseudo(pseudo)
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé : " + pseudo));
    }
}