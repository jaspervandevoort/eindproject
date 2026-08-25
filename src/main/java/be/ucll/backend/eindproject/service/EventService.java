package be.ucll.backend.eindproject.service;

import be.ucll.backend.eindproject.dto.EventRequest;
import be.ucll.backend.eindproject.model.Event;
import be.ucll.backend.eindproject.model.Organizer;
import be.ucll.backend.eindproject.model.Venue;
import be.ucll.backend.eindproject.repository.EventRepository;
import be.ucll.backend.eindproject.repository.OrganizerRepository;
import be.ucll.backend.eindproject.repository.VenueRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final OrganizerRepository organizerRepository;

    public EventService(EventRepository eventRepository, VenueRepository venueRepository, OrganizerRepository organizerRepository) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.organizerRepository = organizerRepository;
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ORGANIZER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public Event createEvent(EventRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new RuntimeException("Venue not found"));
        Organizer organizer = organizerRepository.findById(request.getOrganizerId())
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        Event event = new Event(request.getName(), request.getDescription(), request.getPrice(), request.getDate());
        event.setVenue(venue);
        event.setOrganizer(organizer);

        return eventRepository.save(event);
    }


    public List<Event> getEvents(String city, String fromDate, String toDate) {
        List<Event> events;

        if (city != null && !city.isEmpty()) {
            events = eventRepository.findByVenue_City(city);
        } else {
            events = eventRepository.findAll();
        }


        if (fromDate != null && !fromDate.isEmpty()) {
            events = events.stream()
                    .filter(e -> e.getDate().compareTo(fromDate) >= 0)
                    .collect(Collectors.toList());
        }

        if (toDate != null && !toDate.isEmpty()) {
            events = events.stream()
                    .filter(e -> e.getDate().compareTo(toDate) <= 0)
                    .collect(Collectors.toList());
        }

        return events;
    }


    public List<Event> searchByName(String name) {
        return eventRepository.findByNameContainingIgnoreCase(name);
    }


    public List<Event> getEventsByVenue(Long venueId) {
        return eventRepository.findByVenueId(venueId);
    }
}
