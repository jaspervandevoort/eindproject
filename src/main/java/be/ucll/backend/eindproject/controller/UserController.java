package be.ucll.backend.eindproject.controller;

import be.ucll.backend.eindproject.dto.UserDto;
import be.ucll.backend.eindproject.exception.EmailAddressNotUniqueException;
import be.ucll.backend.eindproject.exception.UserNotFoundException;
import be.ucll.backend.eindproject.model.User;
import be.ucll.backend.eindproject.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> registerUser(@RequestBody UserDto userDto){
        User createdUser = userService.registerUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{id}")
    @PreAuthorize("T(Long).parseLong(authentication.token.subject) == #id or hasAuthority('SCOPE_ROLE_ADMIN')")
    public User getUser(@PathVariable long id) throws UserNotFoundException {
        return userService.getUser(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("T(Long).parseLong(authentication.token.subject) == #id or hasAuthority('SCOPE_ROLE_ADMIN')")
    public User updateUser(@PathVariable long id, @Valid @RequestBody UserDto userDto) throws UserNotFoundException, EmailAddressNotUniqueException {
        return userService.updateUser(id, userDto);
    }
}
