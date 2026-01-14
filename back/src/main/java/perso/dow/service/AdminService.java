package perso.dow.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import perso.dow.model.dto.PlayerRolesDto;
import perso.dow.model.entities.Player;
import perso.dow.model.entities.Role;
import perso.dow.repository.PlayerRepository;
import perso.dow.repository.RoleRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final PlayerRepository playerRepository;
    private final RoleRepository roleRepository;

    public AdminService(PlayerRepository playerRepository, RoleRepository roleRepository) {
        this.playerRepository = playerRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<String> getAllRoleNames() {
        return roleRepository.findAllRolesNames();
    }

    @Transactional(readOnly = true)
    public List<PlayerRolesDto> getAllPlayers() {
        List<Player> players = playerRepository.findAllWithRoles();

        return players.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PlayerRolesDto updatePlayerRoles(Long playerId, List<String> updateDto) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Joueur introuvable"));

        List<String> roleNames = updateDto != null ? updateDto : List.of();

        Set<Role> roles = new LinkedHashSet<>(roleRepository.findByNameIn(roleNames));
        player.setRoles(roles);

        player = playerRepository.save(player);

        Player updatedPlayer = playerRepository.findByIdWithRoles(player.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la récupération"));

        return toDto(updatedPlayer);
    }

    private PlayerRolesDto toDto(Player player) {
        List<String> roleNames = player.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new PlayerRolesDto(player.getId(), player.getPseudo(), roleNames);
    }
}