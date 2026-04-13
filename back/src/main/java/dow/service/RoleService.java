package dow.service;

import dow.exception.AlreadyExist;
import dow.model.dto.RoleDto;
import dow.model.entities.Role;
import dow.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    public RoleDto createRole(String name) {
        String trimmedName = validateRoleDto(name);

        if (roleRepository.existsByName(trimmedName)) {
            roleConflictNameThrow();
        }

        Role role = new Role(null, trimmedName);
        Role saved = roleRepository.save(role);
        return toDto(saved);
    }

    public RoleDto updateRole(RoleDto roledto) {
        String trimmedName = validateRoleDto(roledto.getName());

        Role existing = roleRepository.findById(roledto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rôle introuvable"));

        if (!existing.getName().equals(trimmedName) &&
                roleRepository.existsByNameAndIdNot(trimmedName, roledto.getId())) {
            roleConflictNameThrow();
        }

        existing.setName(trimmedName);
        Role saved = roleRepository.save(existing);
        return toDto(saved);
    }

    private void roleConflictNameThrow() {
        throw new AlreadyExist("Le nom de rôle est déjà pris");
    }

    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rôle introuvable");
        }
        roleRepository.deleteById(id);
    }

    private String validateRoleDto(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom du rôle est obligatoire");
        }

        return name.trim();
    }

    private RoleDto toDto(Role role) {
        return new RoleDto(role.getId(), role.getName());
    }
}