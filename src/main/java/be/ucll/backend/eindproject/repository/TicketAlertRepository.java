package be.ucll.backend.eindproject.repository;

import be.ucll.backend.eindproject.model.TicketAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketAlertRepository extends JpaRepository<TicketAlert, Long> {

    List<TicketAlert> findByEventId(Long eventId);

    Optional<TicketAlert> findByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    void deleteByUserIdAndEventId(Long userId, Long eventId);
}
