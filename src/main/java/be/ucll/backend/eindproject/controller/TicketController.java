package be.ucll.backend.eindproject.controller;

import be.ucll.backend.eindproject.dto.TicketRequest;
import be.ucll.backend.eindproject.model.Ticket;
import be.ucll.backend.eindproject.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ORGANIZER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<List<Ticket>> getTicketsForEvent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long eventId) {
        Long userId = Long.parseLong(jwt.getSubject());
        List<Ticket> tickets = ticketService.getTicketsForOrganizerEvent(eventId, userId);
        return ResponseEntity.ok(tickets);
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody TicketRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        Ticket createdTicket = ticketService.createTicket(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ORGANIZER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<Ticket> approveTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        Ticket approvedTicket = ticketService.approveTicket(id, userId);
        return ResponseEntity.ok(approvedTicket);
    }

    @PutMapping("/{id}/price")
    public ResponseEntity<Ticket> updatePrice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody Map<String, Float> body) {
        Long userId = Long.parseLong(jwt.getSubject());
        float newPrice = body.get("price");
        Ticket updatedTicket = ticketService.updatePrice(id, newPrice, userId);
        return ResponseEntity.ok(updatedTicket);
    }

    @PutMapping("/{id}/sell")
    public ResponseEntity<Ticket> setTicketForSale(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody Map<String, Float> body) {
        Long userId = Long.parseLong(jwt.getSubject());
        float newPrice = body.get("price");
        Ticket updatedTicket = ticketService.setTicketForSale(id, newPrice, userId);
        return ResponseEntity.ok(updatedTicket);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        ticketService.deleteTicket(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<Ticket> purchaseTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        Ticket purchasedTicket = ticketService.purchaseTicket(id, userId);
        return ResponseEntity.ok(purchasedTicket);
    }
}
