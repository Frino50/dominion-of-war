package perso.dow.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import perso.dow.model.dto.RoleDto;
import perso.dow.model.entities.Role;
import perso.dow.repository.RoleRepository;

import java.util.List;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAllAsDto();
    }

    public RoleDto createRole(RoleDto dto) {
        validateRoleDto(dto);
        String trimmedName = dto.getName().trim();

        if (roleRepository.existsByName(trimmedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le nom de rôle est déjà pris");
        }

        Role role = new Role(null, trimmedName);
        Role saved = roleRepository.save(role);
        return toDto(saved);
    }

    public RoleDto updateRole(Long id, RoleDto dto) {
        validateRoleDto(dto);

        Role existing = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rôle introuvable"));

        String trimmedName = dto.getName().trim();

        if (!existing.getName().equals(trimmedName) &&
                roleRepository.existsByNameAndIdNot(trimmedName, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le nom de rôle est déjà pris");
        }

        existing.setName(trimmedName);
        Role saved = roleRepository.save(existing);
        return toDto(saved);
    }

    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rôle introuvable");
        }
        roleRepository.deleteById(id);
    }

    private void validateRoleDto(RoleDto dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les données du rôle sont manquantes");
        }

        String name = dto.getName();
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom du rôle est obligatoire");
        }
    }

    private RoleDto toDto(Role role) {
        return new RoleDto(role.getId(), role.getName());
    }
}