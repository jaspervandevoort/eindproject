package be.ucll.backend.eindproject.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SENT_TICKET_NOTIFICATION", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ticket_alert_id", "ticket_id"})
})
public class SentTicketNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_alert_id", nullable = false)
    private TicketAlert ticketAlert;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public SentTicketNotification() {}

    public SentTicketNotification(TicketAlert ticketAlert, Ticket ticket) {
        this.ticketAlert = ticketAlert;
        this.ticket = ticket;
        this.sentAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public TicketAlert getTicketAlert() {
        return ticketAlert;
    }

    public void setTicketAlert(TicketAlert ticketAlert) {
        this.ticketAlert = ticketAlert;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
