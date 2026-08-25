package be.ucll.backend.eindproject.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(long id) {
        super("Could not find user with id " + id);
    }
}
