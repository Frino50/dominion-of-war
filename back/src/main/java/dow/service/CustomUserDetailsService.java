package dow.service;

import dow.model.CustomUserDetails;
import dow.model.entities.Player;
import dow.repository.PlayerRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    PlayerRepository playerRepository;

    public CustomUserDetailsService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Appelé automatiquement par Spring Security via le contrat de l'interface {@link UserDetailsService}.
     * Il suffit d'implémenter cette interface pour que {@link DaoAuthenticationProvider}
     * invoque cette méthode lors de chaque tentative d'authentification,
     * sans aucun appel explicite dans le code métier.
     */
    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Player player = playerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        return new CustomUserDetails(player);
    }
}