package be.ucll.backend.eindproject.service;

import be.ucll.backend.eindproject.dto.OrganizerRequest;
import be.ucll.backend.eindproject.exception.EmailAddressNotUniqueException;
import be.ucll.backend.eindproject.model.Organizer;
import be.ucll.backend.eindproject.model.Role;
import be.ucll.backend.eindproject.model.User;
import be.ucll.backend.eindproject.repository.OrganizerRepository;
import be.ucll.backend.eindproject.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class OrganizerService {

    private final OrganizerRepository organizerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OrganizerService(OrganizerRepository organizerRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.organizerRepository = organizerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public Organizer createOrganizer(OrganizerRequest request) throws EmailAddressNotUniqueException {
        String emailLowercase = request.getEmail().toLowerCase();

        if (userRepository.existsByEmail(emailLowercase)) {
            throw new EmailAddressNotUniqueException("Email address already in use");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getName(), emailLowercase, hashedPassword);
        user.setRole(Role.ORGANIZER);
        userRepository.save(user);

        Organizer organizer = new Organizer();
        organizer.setName(request.getName());
        organizer.setEmail(emailLowercase);
        organizer.setUser(user);
        organizer.setValidationUrl(request.getValidationUrl());

        return organizerRepository.save(organizer);
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SCOPE_ROLE_ADMIN"));
    }

    public Organizer updateValidationUrl(Long organizerId, String validationUrl, Long currentUserId) {
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new RuntimeException("Organizer not found with id: " + organizerId));

        User organizerUser = organizer.getUser();
        if (!isAdmin() && (organizerUser == null || !organizerUser.getId().equals(currentUserId))) {
            throw new AccessDeniedException("You are not this organizer");
        }

        organizer.setValidationUrl(validationUrl);
        return organizerRepository.save(organizer);
    }
}
