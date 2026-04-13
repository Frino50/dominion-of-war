package dow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyExist.class)
    public ResponseEntity<Map<String, String>> alreadyExist(AlreadyExist ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", HttpStatus.CONFLICT.name());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> notFoundException(NotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", HttpStatus.NOT_FOUND.name());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(GameAlreadyInException.class)
    public ResponseEntity<Map<String, String>> gameAlreadyInException(GameAlreadyInException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "GAME_ALREADY_IN");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(GameBadPasswordException.class)
    public ResponseEntity<Map<String, String>> GameBadPasswordException(GameBadPasswordException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "GAME_BAD_PASSWORD");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions() {
        Map<String, String> body = new HashMap<>();
        body.put("error", "INTERNAL_ERROR");
        body.put("message", "Erreur dans le back");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> RuntimeException(RuntimeException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "ERROR");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied() {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Accès refusé : vous n'avez pas le rôle requis pour cette action.");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "INVALID_CREDENTIALS");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
}