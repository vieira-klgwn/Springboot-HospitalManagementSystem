package vector.HospitalManagementApplication.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vector.HospitalManagementApplication.models.Token;
import vector.HospitalManagementApplication.repositories.TokenRepository;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    private final AuthenticationService authenticationService;
    private final TokenRepository tokenRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest registerRequest) {
        logger.debug("Register request for email: {}", registerRequest.getEmail());
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        logger.debug("Login request for email: {}", authenticationRequest.getEmail());
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletResponse response, HttpServletRequest request) throws IOException {
        logger.debug("Refresh token request received");
        try {
            authenticationService.refreshToken(request, response);
        } catch (Exception e) {
            logger.error("Failed to refresh token: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid refresh token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        logger.debug("Logout request with Authorization header: {}", authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Optional<Token> tokenEntity = tokenRepository.findByToken(token);
            if (tokenEntity.isPresent()) {
                Token dbToken = tokenEntity.get();
                dbToken.setExpired(true);
                dbToken.setRevoked(true);
                tokenRepository.save(dbToken);
                logger.info("Token invalidated for user: {}", dbToken.getUser().getEmail());
            } else {
                logger.warn("Token not found in database: {}", token);
            }
        } else {
            logger.warn("No valid Bearer token provided for logout");
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping({"/forgot-password", "/request-password-reset"})
    public ResponseEntity<String> requestPasswordReset(@RequestBody ForgotPasswordRequest request) {
        logger.debug("Password reset request for email: {}", request.getEmail());
        try {
            String resetToken = authenticationService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok("Password reset email sent successfully");
        } catch (Exception e) {
            logger.error("Failed to process password reset: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to send password reset email: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        logger.debug("Password reset attempt with token: {}", request.getResetToken());
        try {
            authenticationService.resetPassword(request.getResetToken(), request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully");
        } catch (Exception e) {
            logger.error("Failed to reset password: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to reset password: " + e.getMessage());
        }
    }
}