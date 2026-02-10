package dow.exception;

public class GameAlreadyInException extends RuntimeException {
    public GameAlreadyInException(String message) {
        super(message);
    }
}