package perso.dow.service;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import perso.dow.exception.AlreadyExist;
import perso.dow.model.dto.RouteDto;
import perso.dow.model.entities.Player;
import perso.dow.model.entities.Role;
import perso.dow.model.entities.Route;
import perso.dow.repository.RoleRepository;
import perso.dow.repository.RouteRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RouteService {

    private final RouteRepository routeRepository;
    private final RoleRepository roleRepository;
    private final UtilsService utilsService;

    public RouteService(RouteRepository routeRepository, RoleRepository roleRepository, UtilsService utilsService) {
        this.routeRepository = routeRepository;
        this.roleRepository = roleRepository;
        this.utilsService = utilsService;
    }

    public List<RouteDto> getAvailableRoutes() {
        Set<Role> userRoles = new LinkedHashSet<>();

        try {
            Player player = utilsService.getPlayer();
            if (player != null && player.getRoles() != null) {
                userRoles = player.getRoles();
            }
        } catch (Exception e) {
            // Utilisateur non connecté ou non trouvé : userRoles reste vide
        }

        return routeRepository.findAvailableRoutesAsDto(userRoles);
    }

    public List<RouteDto> getAll() {
        return routeRepository.findAllRoutesAsDto();
    }

    public RouteDto create(RouteDto dto) {
        validateUniqueRouteName(dto.getName(), null);

        Route route = buildRouteFromDto(new Route(), dto);

        return toDto(route);
    }

    public RouteDto update(RouteDto dto) {
        Route route = routeRepository.findById(dto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route introuvable"));

        if (!route.getName().equals(dto.getName())) {
            validateUniqueRouteName(dto.getName(), dto.getId());
        }

        route = buildRouteFromDto(route, dto);

        return toDto(route);
    }

    public void delete(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Route introuvable");
        }
        routeRepository.deleteById(id);
    }

    private void validateUniqueRouteName(String name, Long excludeId) {
        if (excludeId == null) {
            if (routeRepository.existsByName(name)) {
                throw new AlreadyExist(
                        "Route avec le nom: '" + name + "' déjà existante."
                );
            }
        } else {
            if (routeRepository.existsByNameAndIdNot(name, excludeId)) {
                throw new AlreadyExist(
                        "Route avec le nom: '" + name + "' déjà existante."
                );
            }
        }
    }

    private Route buildRouteFromDto(Route route, RouteDto dto) {
        route.setName(dto.getName());
        route.setComponentPath(dto.getComponentPath());
        route.setNeedAuth(dto.isNeedAuth());

        if (dto.isNeedAuth() && dto.getRoleName() != null && !dto.getRoleName().isBlank()) {
            Role role = roleRepository.findByName(dto.getRoleName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le rôle spécifié est introuvable"));
            route.setRole(role);
        } else {
            route.setRole(null);
        }

        return routeRepository.save(route);
    }

    private RouteDto toDto(Route route) {
        return new RouteDto(
                route.getId(),
                route.getName(),
                route.getComponentPath(),
                route.getRole() != null ? route.getRole().getName() : null,
                route.isNeedAuth()
        );
    }
}