package be.ucll.backend.eindproject.controller;

import be.ucll.backend.eindproject.dto.OrganizerRequest;
import be.ucll.backend.eindproject.dto.OrganizerValidationUrlRequest;
import be.ucll.backend.eindproject.exception.EmailAddressNotUniqueException;
import be.ucll.backend.eindproject.model.Organizer;
import be.ucll.backend.eindproject.service.OrganizerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizers")
public class OrganizerController {

    private final OrganizerService organizerService;

    public OrganizerController(OrganizerService organizerService) {
        this.organizerService = organizerService;
    }

    @PostMapping
    public ResponseEntity<Organizer> createOrganizer(@Valid @RequestBody OrganizerRequest request) throws EmailAddressNotUniqueException {
        Organizer createdOrganizer = organizerService.createOrganizer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrganizer);
    }

    @PutMapping("/{id}/validation-url")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ORGANIZER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<Organizer> updateValidationUrl(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody OrganizerValidationUrlRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        Organizer updatedOrganizer = organizerService.updateValidationUrl(id, request.getValidationUrl(), userId);
        return ResponseEntity.ok(updatedOrganizer);
    }
}
