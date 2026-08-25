package be.ucll.backend.eindproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateTicketCodeException extends RuntimeException {
    public DuplicateTicketCodeException(long eventId, String code) {
        super("A ticket with code '" + code + "' already exists for event " + eventId);
    }
}
