package be.ucll.backend.eindproject.service;

import be.ucll.backend.eindproject.dto.UserDto;
import be.ucll.backend.eindproject.exception.EmailAddressNotUniqueException;
import be.ucll.backend.eindproject.exception.UserNotFoundException;
import be.ucll.backend.eindproject.model.User;
import be.ucll.backend.eindproject.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserDto dto) {
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        String emailLowercase = dto.getEmail().toLowerCase();

        User user = new User(dto.getName(), emailLowercase, hashedPassword);

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAddressNotUniqueException(emailLowercase);
        }
    }

    public User getUser(Long id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public User updateUser(Long id, UserDto userDto) throws UserNotFoundException, EmailAddressNotUniqueException {
        final var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setEmail(userDto.getEmail());
        final var hashedPassword = passwordEncoder.encode(userDto.getPassword());
        user.setHashedPassword(hashedPassword);
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAddressNotUniqueException(userDto.getEmail());
        }
    }
}
