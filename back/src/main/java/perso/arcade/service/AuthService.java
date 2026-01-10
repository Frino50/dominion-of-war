package perso.arcade.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import perso.arcade.exception.InvalidCredentialsException;
import perso.arcade.exception.UsernameAlreadyTakenException;
import perso.arcade.model.dto.ConnexionDto;
import perso.arcade.model.dto.LoginResponseDto;
import perso.arcade.model.entities.Player;
import perso.arcade.model.entities.Role;
import perso.arcade.repository.PlayerRepository;
import perso.arcade.repository.RoleRepository;
import perso.arcade.security.JwtUtils;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class AuthService {

    private final PlayerRepository playerRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(PlayerRepository playerRepository, RoleRepository roleRepository, AuthenticationManager authenticationManager, JwtUtils jwtUtils, BCryptPasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    public Player register(ConnexionDto connexionDto) {
        if (playerRepository.findByPseudo(connexionDto.getPseudo()).isPresent()) {
            throw new UsernameAlreadyTakenException("Le pseudo est déjà utilisé");
        }
        String hashedPassword = passwordEncoder.encode(connexionDto.getPassword());
        Player player = new Player(null, connexionDto.getPseudo(), hashedPassword);
        Role playerRole = roleRepository.findByName("PLAYER").orElseGet(() -> roleRepository.save(new Role(null, "PLAYER")));
        Set<Role> roles = new LinkedHashSet<>();
        roles.add(playerRole);
        player.setRoles(roles);
        return playerRepository.save(player);
    }

    public LoginResponseDto login(ConnexionDto connexionDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(connexionDto.getPseudo(), connexionDto.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            return new LoginResponseDto(jwt, connexionDto.getPseudo());
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Pseudo ou mot de passe incorrect.");
        }
    }
}
