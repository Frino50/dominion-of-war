package perso.dow.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import perso.dow.model.CustomUserDetails;
import perso.dow.model.entities.Player;
import perso.dow.repository.PlayerRepository;

@Service
public class PlayerService implements UserDetailsService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CustomUserDetails loadUserByUsername(String pseudo) throws UsernameNotFoundException {
        Player player = playerRepository.findWithRolesByPseudo(pseudo)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec le pseudo: " + pseudo));
        return new CustomUserDetails(player);
    }
}