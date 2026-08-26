package be.ucll.backend.eindproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TicketNotApprovedException extends RuntimeException {
    public TicketNotApprovedException(long id) {
        super("Ticket " + id + " is not yet approved");
    }
}
