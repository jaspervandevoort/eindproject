package be.ucll.backend.eindproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TicketNotForSaleException extends RuntimeException {
    public TicketNotForSaleException(long id) {
        super("Ticket " + id + " is not currently for sale (it may already be sold)");
    }
}
