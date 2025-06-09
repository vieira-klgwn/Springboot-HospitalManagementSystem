package vector.HospitalManagementApplication.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vector.HospitalManagementApplication.config.JwtService;
import vector.HospitalManagementApplication.models.Token;
import vector.HospitalManagementApplication.models.TokenType;
import vector.HospitalManagementApplication.models.User;
import vector.HospitalManagementApplication.models.Gender;
import vector.HospitalManagementApplication.repositories.TokenRepository;
import vector.HospitalManagementApplication.repositories.UserRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;
    private static final Logger logger = Logger.getLogger(AuthenticationService.class.getName());

    @Value("${spring.mail.from}")
    private String fromEmail;

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken: " + request.getEmail());
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalStateException("Passwords do not match");
        }
        if (request.getGender() == null || request.getGender().isEmpty()) {
            throw new IllegalStateException("Gender is required");
        }
        Gender gender;
        try {
            gender = Gender.valueOf(request.getGender().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid gender value: " + request.getGender());
        }
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .gender(gender)
                .build();
        var savedUser = userRepository.save(user);
        var token = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, token);
        saveUserToken(savedUser, refreshToken);
        logger.info("accesstoken: " + token);
        logger.info("refreshtoken: " + refreshToken);
        return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword()));
        var user = userRepository.findByEmail(authenticationRequest.getEmail()).orElseThrow(() -> new IllegalStateException("User not found: " + authenticationRequest.getEmail()));
        var token = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, refreshToken);
        saveUserToken(user, token);
        return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }

    private void revokeAllUserTokens(User user) {
        var validTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validTokens.isEmpty()) {
            return;
        }
        validTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(validTokens);
    }

    private void saveUserToken(User savedUser, String jwtToken) {
        var token = Token.builder()
                .token(jwtToken)
                .user(savedUser)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .createdDate(LocalDateTime.now())
                .build();
        tokenRepository.save(token);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader("Authorization");
        final String refreshToken;
        final String email;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or Invalid Authorization header");
            return;
        }

        refreshToken = authHeader.substring(7);
        email = jwtService.extractUsername(refreshToken);

        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Refresh Token");
            return;
        }

        var user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalStateException("User not found: " + email));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or Expired Refresh Token");
            return;
        }
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        var authResponse = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
    }

    public String requestPasswordReset(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
        String resetToken = UUID.randomUUID().toString();
        var token = Token.builder()
                .token(resetToken)
                .user(user)
                .tokenType(TokenType.PASSWORD_RESET)
                .expired(false)
                .revoked(false)
                .createdDate(LocalDateTime.now())
                .build();
        tokenRepository.save(token);

        // Send email with reset link via JavaMailSender
        String resetUrl = "http://localhost:8081/reset-password?token=" + resetToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("Hello,\n\nTo reset your password, click the following link:\n" + resetUrl +
                "\n\nThis link will expire in 1 hour.\n\nIf you did not request a password reset, please ignore this email.\n\nBest regards,\nTaskSync Team");
        message.setFrom(fromEmail);

        try {
            mailSender.send(message);
            logger.info("Password reset email sent to: " + email + ", Token: " + resetToken);
        } catch (MailException e) {
            logger.severe("Failed to send password reset email to: " + email + ", Error: " + e.getMessage());
            throw new IllegalStateException("Failed to send password reset email: " + e.getMessage());
        }

        return resetToken;
    }

    public void resetPassword(String resetToken, String newPassword) {
        var token = tokenRepository.findByToken(resetToken)
                .orElseThrow(() -> new IllegalStateException("Invalid reset token"));
        if (token.isExpired() || token.isRevoked() || token.getTokenType() != TokenType.PASSWORD_RESET || token.getCreatedDate().isBefore(LocalDateTime.now().minusHours(1))) {
            throw new IllegalStateException("Invalid or expired reset token");
        }
        var user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        token.setExpired(true);
        token.setRevoked(true);
        tokenRepository.save(token);
        logger.info("Password reset successfully for user: " + user.getEmail());
    }
}