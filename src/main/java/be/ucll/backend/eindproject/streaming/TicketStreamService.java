package be.ucll.backend.eindproject.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TicketStreamService {

    private static final Logger log = LoggerFactory.getLogger(TicketStreamService.class);

    private final Map<Long, Sinks.Many<TicketStreamMessage>> sinks = new ConcurrentHashMap<>();

    public Flux<TicketStreamMessage> getTicketStream(Long eventId) {
        Sinks.Many<TicketStreamMessage> sink = sinks.computeIfAbsent(eventId,
                k -> Sinks.many().multicast().onBackpressureBuffer());

        return sink.asFlux();
    }

    public void notifyNewTicket(Long eventId, TicketStreamMessage message) {
        Sinks.Many<TicketStreamMessage> sink = sinks.get(eventId);

        if (sink != null) {
            sink.tryEmitNext(message);
        }
    }
}
