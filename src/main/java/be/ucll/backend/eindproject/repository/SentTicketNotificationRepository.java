package be.ucll.backend.eindproject.repository;

import be.ucll.backend.eindproject.model.SentTicketNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentTicketNotificationRepository extends JpaRepository<SentTicketNotification, Long> {

    boolean existsByTicketAlertIdAndTicketId(Long ticketAlertId, Long ticketId);
}
