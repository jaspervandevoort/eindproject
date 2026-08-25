package be.ucll.backend.eindproject.repository;

import be.ucll.backend.eindproject.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEventId(Long eventId);
    List<Ticket> findByOwnerId(Long userId);
    boolean existsByEventIdAndCode(Long eventId, String code);
}