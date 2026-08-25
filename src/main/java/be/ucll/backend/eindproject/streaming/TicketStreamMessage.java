package be.ucll.backend.eindproject.streaming;

public record TicketStreamMessage(
        Long ticketId,
        String eventName,
        float askingPrice
) {}
