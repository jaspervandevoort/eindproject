package be.ucll.backend.eindproject.service;

import be.ucll.backend.eindproject.dto.TicketRequest;
import be.ucll.backend.eindproject.exception.DuplicateTicketCodeException;
import be.ucll.backend.eindproject.exception.EventNotFoundException;
import be.ucll.backend.eindproject.exception.InvalidTicketPriceException;
import be.ucll.backend.eindproject.exception.TicketDeletedException;
import be.ucll.backend.eindproject.exception.TicketNotForSaleException;
import be.ucll.backend.eindproject.exception.TicketNotFoundException;
import be.ucll.backend.eindproject.message.TicketAlertMessage;
import be.ucll.backend.eindproject.message.TicketAlertSender;
import be.ucll.backend.eindproject.message.TicketValidationMessage;
import be.ucll.backend.eindproject.message.TicketValidationSender;
import be.ucll.backend.eindproject.model.User;
import be.ucll.backend.eindproject.model.Event;
import be.ucll.backend.eindproject.model.Ticket;
import be.ucll.backend.eindproject.model.TicketAlert;
import be.ucll.backend.eindproject.repository.UserRepository;
import be.ucll.backend.eindproject.repository.EventRepository;
import be.ucll.backend.eindproject.repository.TicketRepository;
import be.ucll.backend.eindproject.repository.TicketAlertRepository;
import be.ucll.backend.eindproject.streaming.TicketStreamMessage;
import be.ucll.backend.eindproject.streaming.TicketStreamService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TicketAlertRepository ticketAlertRepository;
    private final TicketAlertSender ticketAlertSender;
    private final TicketStreamService ticketStreamService;
    private final TicketValidationSender ticketValidationSender;

    public TicketService(TicketRepository ticketRepository, EventRepository eventRepository,
                         UserRepository userRepository, TicketAlertRepository ticketAlertRepository,
                         TicketAlertSender ticketAlertSender, TicketStreamService ticketStreamService,
                         TicketValidationSender ticketValidationSender) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.ticketAlertRepository = ticketAlertRepository;
        this.ticketAlertSender = ticketAlertSender;
        this.ticketStreamService = ticketStreamService;
        this.ticketValidationSender = ticketValidationSender;
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SCOPE_ROLE_ADMIN"));
    }

    public List<Ticket> getAllTicketsForEvent(Long eventId){
        return ticketRepository.findByEventId(eventId);
    }

    public List<Ticket> getTicketsForOrganizerEvent(Long eventId, Long currentUserId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        User organizer = event.getOrganizer().getUser();
        if (!isAdmin() && (organizer == null || !organizer.getId().equals(currentUserId))) {
            throw new AccessDeniedException("You are not the organizer of this event");
        }

        return ticketRepository.findByEventId(eventId);
    }

    public List<Ticket> getTicketByOwner(Long ownerId){
        return ticketRepository.findByOwnerId(ownerId);
    }

    public Ticket approveTicket(Long ticketId, Long currentUserId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        User organizer = ticket.getEvent().getOrganizer().getUser();
        if (!isAdmin() && (organizer == null || !organizer.getId().equals(currentUserId))) {
            throw new AccessDeniedException("You are not the organizer of this event");
        }

        ticket.setApproved(true);
        return ticketRepository.save(ticket);
    }

    public Ticket createTicket(TicketRequest request, Long currentUserId) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EventNotFoundException(request.getEventId()));

        if (ticketRepository.existsByEventIdAndCode(request.getEventId(), request.getCode())) {
            throw new DuplicateTicketCodeException(request.getEventId(), request.getCode());
        }

        if (request.getAskingPrice() > event.getPrice()) {
            throw new InvalidTicketPriceException(
                    "Asking price must not exceed the original ticket price: " + event.getPrice());
        }

        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + currentUserId));

        Ticket ticket = new Ticket(
                request.getCode(),
                event,
                false,
                false,
                true,
                request.getAskingPrice(),
                owner
        );
        Ticket savedTicket = ticketRepository.save(ticket);

        // Vraag automatische validatie aan bij de externe validatieservice (via RabbitMQ)
        ticketValidationSender.send(new TicketValidationMessage(savedTicket.getId()));

        return savedTicket;
    }

    public Ticket setTicketForSale(Long ticketId, float newPrice, Long currentUserId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        User owner = ticket.getOwner();
        if (!isAdmin() && (owner == null || !owner.getId().equals(currentUserId))) {
            throw new AccessDeniedException("You are not the owner of this ticket");
        }

        if (ticket.getAskingPrice() < newPrice) {
            throw new RuntimeException("Price must be lower than current asking price: " + ticket.getAskingPrice());
        }

        ticket.setForSale(true);
        ticket.setAskingPrice(newPrice);
        Ticket savedTicket = ticketRepository.save(ticket);

        // Stuur alerts naar geïnteresseerde kopers (via RabbitMQ)
        sendTicketAlerts(savedTicket);

        // Notificeer streaming API subscribers
        notifyTicketStream(savedTicket);

        return savedTicket;
    }

    private void notifyTicketStream(Ticket ticket) {
        TicketStreamMessage streamMessage = new TicketStreamMessage(
                ticket.getId(),
                ticket.getEvent().getName(),
                ticket.getAskingPrice()
        );
        ticketStreamService.notifyNewTicket(ticket.getEvent().getId(), streamMessage);
    }

    private void sendTicketAlerts(Ticket ticket) {
        List<TicketAlert> alerts = ticketAlertRepository.findByEventId(ticket.getEvent().getId());

        for (TicketAlert alert : alerts) {
            TicketAlertMessage message = new TicketAlertMessage(
                    alert.getId(),
                    ticket.getId(),
                    ticket.getEvent().getName(),
                    ticket.getAskingPrice(),
                    alert.getUser().getEmail()
            );
            ticketAlertSender.send(message);
        }
    }

    public Ticket updatePrice(Long ticketId, float newPrice, Long currentUserId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        User owner = ticket.getOwner();
        if (!isAdmin() && (owner == null || !owner.getId().equals(currentUserId))) {
            throw new AccessDeniedException("You are not the owner of this ticket");
        }

        if (ticket.isDeleted()) {
            throw new TicketDeletedException(ticketId);
        }

        if (!ticket.isForSale()) {
            throw new TicketNotForSaleException(ticketId);
        }

        float originalPrice = ticket.getEvent().getPrice();
        if (newPrice >= originalPrice) {
            throw new InvalidTicketPriceException(
                    "New asking price must be lower than the original ticket price: " + originalPrice);
        }

        ticket.setAskingPrice(newPrice);
        return ticketRepository.save(ticket);
    }

    public void deleteTicket(Long ticketId, Long currentUserId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        User owner = ticket.getOwner();
        if (!isAdmin() && (owner == null || !owner.getId().equals(currentUserId))) {
            throw new AccessDeniedException("You are not the owner of this ticket");
        }

        if (!ticket.isForSale()) {
            throw new TicketNotForSaleException(ticketId);
        }

        ticket.setDeleted(true);
        ticketRepository.save(ticket);
    }

    public Ticket purchaseTicket(Long ticketId, Long currentUserId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        if (ticket.isDeleted()) {
            throw new RuntimeException("Ticket is not available");
        }

        /*
        if (!ticket.isApproved()) {
            throw new RuntimeException("Ticket is not approved");
        }
        */

        if (!ticket.isForSale()) {
            throw new RuntimeException("Ticket is not for sale");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + currentUserId));

        if (ticket.getOwner().getId().equals(currentUserId)) {
            throw new RuntimeException("You already own this ticket");
        }

        ticket.setOwner(currentUser);
        ticket.setForSale(false);
        return ticketRepository.save(ticket);
    }
}
