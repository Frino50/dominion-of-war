package perso.dow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import perso.dow.model.dto.PlayerRolesDto;
import perso.dow.model.dto.RoleDto;
import perso.dow.service.AdminService;
import perso.dow.service.RoleService;

import java.util.List;

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

    @PostMapping("/roles")
    public ResponseEntity<RoleDto> createRole(@RequestBody RoleDto body) {
        return ResponseEntity.ok(roleService.createRole(body));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Long id, @RequestBody RoleDto body) {
        return ResponseEntity.ok(roleService.updateRole(id, body));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/players")
    public ResponseEntity<List<PlayerRolesDto>> getPlayers() {
        return ResponseEntity.ok(adminService.getAllPlayers());
    }

    @PutMapping("/players/{id}/roles")
    public ResponseEntity<PlayerRolesDto> updatePlayerRoles(
            @PathVariable Long id,
            @RequestBody List<String> body) {
        return ResponseEntity.ok(adminService.updatePlayerRoles(id, body));
    }
}