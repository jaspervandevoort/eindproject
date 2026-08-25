package be.ucll.backend.eindproject.repository;

import be.ucll.backend.eindproject.model.User;
import be.ucll.backend.eindproject.model.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Long> {
    Optional<Organizer> findByUser(User user);
}