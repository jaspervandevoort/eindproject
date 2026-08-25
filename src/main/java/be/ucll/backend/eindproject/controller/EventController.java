package be.ucll.backend.eindproject.controller;

import be.ucll.backend.eindproject.dto.EventRequest;
import be.ucll.backend.eindproject.model.Event;
import be.ucll.backend.eindproject.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }


    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody EventRequest eventRequest) {
        Event createdEvent = eventService.createEvent(eventRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    // GET /events  lijst van evenementen met filters
    @GetMapping
    public ResponseEntity<List<Event>> getEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(eventService.getEvents(city, fromDate, toDate));
    }

    // GET /events/search?name=
    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(eventService.searchByName(name));
    }

    // GET /events/venue/{venueId} - events per locatie
    @GetMapping("/venue/{venueId}")
    public ResponseEntity<List<Event>> getEventsByVenue(@PathVariable Long venueId) {
        return ResponseEntity.ok(eventService.getEventsByVenue(venueId));
    }
}
