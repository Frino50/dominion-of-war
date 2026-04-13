package dow.service;

import dow.exception.AlreadyExist;
import dow.exception.InvalidCredentialsException;
import dow.model.dto.LoginDto;
import dow.model.dto.LoginResponseDto;
import dow.model.dto.RegisterDto;
import dow.model.entities.Player;
import dow.model.entities.Role;
import dow.repository.PlayerRepository;
import dow.repository.RoleRepository;
import dow.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class AuthService {

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
    private static final String PSEUDO_REGEX =
            "^[a-zA-Z0-9_\\-]{3,20}$";
    private final PlayerRepository playerRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UtilsService utilsService;

    public AuthService(PlayerRepository playerRepository, RoleRepository roleRepository,
                       AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                       BCryptPasswordEncoder passwordEncoder, UtilsService utilsService) {
        this.playerRepository = playerRepository;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.utilsService = utilsService;
    }

    public Player register(RegisterDto dto) {
        if (!dto.getEmail().matches(EMAIL_REGEX)) {
            throw new InvalidCredentialsException("Format d'email invalide.");
        }
        if (!dto.getPseudo().matches(PSEUDO_REGEX)) {
            throw new InvalidCredentialsException("Le pseudo doit faire 3 à 20 caractères (lettres, chiffres, _ ou -).");
        }
        if (playerRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new AlreadyExist("Cet email est déjà utilisé.");
        }
        if (playerRepository.findByPseudo(dto.getPseudo()).isPresent()) {
            throw new AlreadyExist("Ce pseudo est déjà utilisé.");
        }

        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        Player player = new Player(null, dto.getEmail(), dto.getPseudo(), hashedPassword);
        Role playerRole = roleRepository.findByName("ROLE_PLAYER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_PLAYER")));
        Set<Role> roles = new LinkedHashSet<>();
        roles.add(playerRole);
        player.setRoles(roles);
        return playerRepository.save(player);
    }

    public LoginResponseDto login(LoginDto dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            return new LoginResponseDto(jwt, utilsService.getPseudo());
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Email ou mot de passe incorrect.");
        }
    }
}
