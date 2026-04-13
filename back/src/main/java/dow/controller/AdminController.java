package dow.controller;

import dow.model.dto.PlayerRolesDto;
import dow.model.dto.RoleDto;
import dow.service.AdminService;
import dow.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final RoleService roleService;

    public AdminController(AdminService adminService, RoleService roleService) {
        this.adminService = adminService;
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAllRoleNames() {
        return ResponseEntity.ok(adminService.getAllRoleNames());
    }

    @GetMapping("/roles/all")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @PostMapping("/roles/{name}")
    public ResponseEntity<RoleDto> createRole(@PathVariable String name) {
        return ResponseEntity.ok(roleService.createRole(name));
    }

    @PutMapping("/roles")
    public ResponseEntity<RoleDto> updateRole(@RequestBody RoleDto roledto) {
        return ResponseEntity.ok(roleService.updateRole(roledto));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/players")
    public ResponseEntity<List<PlayerRolesDto>> getAllPlayers() {
        return ResponseEntity.ok(adminService.getAllPlayers());
    }

    @PutMapping("/players/{id}/roles")
    public ResponseEntity<PlayerRolesDto> updatePlayerRoles(
            @PathVariable UUID id,
            @RequestBody List<String> body) {
        return ResponseEntity.ok(adminService.updatePlayerRoles(id, body));
    }
}