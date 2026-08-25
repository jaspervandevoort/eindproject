package be.ucll.backend.eindproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAddressNotUniqueException extends RuntimeException {

    public EmailAddressNotUniqueException(String email) {
        super("Email address already in use: " + email);
    }
}
