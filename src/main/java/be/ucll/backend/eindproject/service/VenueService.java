package be.ucll.backend.eindproject.service;

import be.ucll.backend.eindproject.model.Venue;
import be.ucll.backend.eindproject.repository.VenueRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueService {
    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public Venue createVenue(Venue venue) {
        return venueRepository.save(venue);
    }

    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    public Venue getVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Venue not found"));
    }
}
