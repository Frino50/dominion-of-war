package dow.service;

import dow.model.CustomUserDetails;
import dow.model.entities.Player;
import dow.repository.PlayerRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService implements UserDetailsService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @NonNull
    public CustomUserDetails loadUserByUsername(String pseudo) throws UsernameNotFoundException {
        Player player = playerRepository.findWithRolesByPseudo(pseudo)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec le pseudo: " + pseudo));
        return new CustomUserDetails(player);
    }
}