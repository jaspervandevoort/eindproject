package be.ucll.backend.eindproject.controller;

import be.ucll.backend.eindproject.streaming.TicketStreamMessage;
import be.ucll.backend.eindproject.streaming.TicketStreamService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/events/{eventId}/tickets")
public class TicketStreamController {

    private final TicketStreamService ticketStreamService;

    public TicketStreamController(TicketStreamService ticketStreamService) {
        this.ticketStreamService = ticketStreamService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<TicketStreamMessage>> stream(@PathVariable Long eventId) {
        return ticketStreamService.getTicketStream(eventId)
                .map(message -> ServerSentEvent.<TicketStreamMessage>builder()
                        .id(String.valueOf(message.ticketId()))
                        .event("new-ticket")
                        .data(message)
                        .build());
    }
}
