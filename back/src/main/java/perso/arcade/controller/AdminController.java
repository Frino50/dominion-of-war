package perso.arcade.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import perso.arcade.model.dto.PlayerRolesDto;
import perso.arcade.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoles() {
        return ResponseEntity.ok(adminService.getAllRoleNames());
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