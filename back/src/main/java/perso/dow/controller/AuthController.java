package perso.dow.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import perso.dow.model.dto.ConnexionDto;
import perso.dow.model.dto.LoginResponseDto;
import perso.dow.model.entities.Player;
import perso.dow.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Player register(@RequestBody ConnexionDto connexionDto) {
        return authService.register(connexionDto);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody ConnexionDto connexionDto) {
        return authService.login(connexionDto);
    }
}