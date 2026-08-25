package be.ucll.backend.eindproject.controller;

import be.ucll.backend.eindproject.model.Event;
import be.ucll.backend.eindproject.model.TicketAlert;
import be.ucll.backend.eindproject.model.User;
import be.ucll.backend.eindproject.repository.EventRepository;
import be.ucll.backend.eindproject.repository.TicketAlertRepository;
import be.ucll.backend.eindproject.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events/{eventId}/alerts")
public class TicketAlertController {

    private final TicketAlertRepository ticketAlertRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public TicketAlertController(TicketAlertRepository ticketAlertRepository,
                                  EventRepository eventRepository,
                                  UserRepository userRepository) {
        this.ticketAlertRepository = ticketAlertRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<TicketAlert> subscribe(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long eventId) {

        Long userId = Long.parseLong(jwt.getSubject());

        if (ticketAlertRepository.existsByUserIdAndEventId(userId, eventId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        TicketAlert alert = new TicketAlert(user, event);
        TicketAlert savedAlert = ticketAlertRepository.save(alert);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedAlert);
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<Void> unsubscribe(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long eventId) {

        Long userId = Long.parseLong(jwt.getSubject());

        if (!ticketAlertRepository.existsByUserIdAndEventId(userId, eventId)) {
            return ResponseEntity.notFound().build();
        }

        ticketAlertRepository.deleteByUserIdAndEventId(userId, eventId);
        return ResponseEntity.noContent().build();
    }
}
