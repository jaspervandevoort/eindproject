package be.ucll.backend.eindproject.security;

import be.ucll.backend.eindproject.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService, UserDetailsPasswordService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String emailLowercase = username.toLowerCase();

        return userRepository.findByEmail(emailLowercase)
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + emailLowercase));
    }

    @Override
    public UserDetails updatePassword(UserDetails userDetails, String newPassword) {
        if (!(userDetails instanceof UserDetailsImpl)) {
            // Don't know how to update this
            return userDetails;
        }
        final var oldUser = ((UserDetailsImpl) userDetails).user();
        oldUser.setHashedPassword(newPassword);
        final var updatedUser = userRepository.save(oldUser);
        return new UserDetailsImpl(updatedUser);
    }
}
