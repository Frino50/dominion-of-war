package perso.arcade.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import perso.arcade.model.entities.Player;
import perso.arcade.repository.PlayerRepository;

@Service
public class UtilsService {

    private final PlayerRepository playerRepository;

    public UtilsService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public String getPseudo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public Player getPlayer() {
        String pseudo = getPseudo();
        return playerRepository.findByPseudo(pseudo)
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé : " + pseudo));
    }
    
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()));
    }
}