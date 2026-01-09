package perso.arcade.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    public ResponseEntity<Map<String, String>> handleUsernameAlreadyTaken(UsernameAlreadyTakenException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "USERNAME_ALREADY_TAKEN");
        body.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "INVALID_CREDENTIALS");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(SpriteNameAlreadyExist.class)
    public ResponseEntity<Map<String, String>> handleSpriteNameAlreadyExist(SpriteNameAlreadyExist ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "SPRITE_NAME_ALREADY_EXIST");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions() {
        Map<String, String> body = new HashMap<>();
        body.put("error", "INTERNAL_ERROR");
        body.put("message", "Erreur dans le back");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}