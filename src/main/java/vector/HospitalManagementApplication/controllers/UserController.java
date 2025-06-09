package vector.HospitalManagementApplication.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vector.HospitalManagementApplication.models.ChangePasswordRequest;
import vector.HospitalManagementApplication.models.User;
import vector.HospitalManagementApplication.models.UserDTO;
import vector.HospitalManagementApplication.services.UserService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        logger.debug("Creating user: {}", user.getEmail());
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.debug("Fetching all users");
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.debug("Fetching user ID: {}", id);
        return userService.getUserById(id)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseGet(() -> {
                    logger.warn("User ID {} not found", id);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        logger.debug("Updating user ID: {}", id);
        try {
            User updatedUser = userService.updateUser(id, user);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Failed to update user ID {}: {}", id, e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.debug("Deleting user ID: {}", id);
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            logger.error("Failed to delete user ID {}: {}", id, e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, Principal connectedUser) {
        logger.debug("Changing password for user: {}", connectedUser.getName());
        try {
            userService.changePassword(changePasswordRequest, connectedUser);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("Failed to change password for user {}: {}", connectedUser.getName(), e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            logger.warn("Unauthorized access attempt to /api/users/me");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String email = authentication.getName();
        logger.debug("Fetching current user with email: {}", email);
        return userService.getUserByEmail(email)
                .map(user -> {
                    logger.info("User found: {}", user.getEmail());
                    UserDTO userDTO = new UserDTO();
                    userDTO.setId(user.getId());
                    userDTO.setFirstName(user.getFirstName());
                    userDTO.setLastName(user.getLastName());
                    userDTO.setEmail(user.getEmail());
                    userDTO.setRole(user.getRole().toString());
                    userDTO.setGender(user.getGender() != null ? user.getGender().toString() : null);
                    return new ResponseEntity<>(userDTO, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("User not found for email: {}", email);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }
}