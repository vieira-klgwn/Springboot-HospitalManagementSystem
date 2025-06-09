package vector.HospitalManagementApplication.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // Skip filter for OPTIONS requests and whitelisted endpoints
        if (method.equals("OPTIONS") || requestURI.startsWith("/api/auth/") || requestURI.equals("/error")) {
            logger.debug("Skipping JWT filter for request: {} {}", method, requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.equals("Bearer undefined")) {
            logger.debug("No valid Bearer token found, proceeding without authentication.");
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        logger.debug("JWT Token: {}", jwt);

        try {
            String userEmail = jwtService.extractUsername(jwt);
            logger.debug("Extracted Email: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                logger.debug("UserDetails: {}", userDetails.getUsername());

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    List<String> authorities = jwtService.extractAuthorities(jwt);
                    List<SimpleGrantedAuthority> grantedAuthorities = authorities != null
                            ? authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                            : List.of();
                    logger.debug("Authorities: {}", grantedAuthorities);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, grantedAuthorities.isEmpty() ? userDetails.getAuthorities() : grantedAuthorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set in SecurityContext for user: {}", userEmail);
                } else {
                    logger.debug("JWT token is invalid for user: {}", userEmail);
                }
            } else {
                logger.debug("No email extracted or authentication already exists.");
            }
        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage());
            // Proceed without authentication instead of throwing an exception
        }

        filterChain.doFilter(request, response);
    }
}