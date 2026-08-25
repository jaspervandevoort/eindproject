package be.ucll.backend.eindproject.message;

public record TicketAlertMessage(
        Long ticketAlertId,
        Long ticketId,
        String eventName,
        float askingPrice,
        String recipientEmail
) {}
