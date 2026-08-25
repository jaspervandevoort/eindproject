package be.ucll.backend.eindproject.repository;

import be.ucll.backend.eindproject.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Zoeken op naam (case-insensitive, partial match)
    List<Event> findByNameContainingIgnoreCase(String name);

    // Evenementen per venue
    List<Event> findByVenueId(Long venueId);

    // Evenementen per stad (via venue)
    List<Event> findByVenue_City(String city);
}