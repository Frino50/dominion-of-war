package dow.exception;

public class GameBadPasswordException extends RuntimeException {
    public GameBadPasswordException(String message) {
        super(message);
    }
}
