package vector.HospitalManagementApplication.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LogoutHandler logoutHandler;

    private static final String[] WHITE_LIST_URL = {
            "/api/auth/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        req -> req
                                .requestMatchers(WHITE_LIST_URL).permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("TEAM_LEAD")
                                .requestMatchers(HttpMethod.GET, "/api/teams/**", "/api/projects/**", "/api/tasks/**", "/api/comments/**").hasAnyRole("USER", "TEAM_LEAD")
                                .requestMatchers(HttpMethod.POST, "/api/comments/**").hasAnyRole("USER", "TEAM_LEAD")
                                .requestMatchers(HttpMethod.POST, "/api/teams/**", "/api/projects/**", "/api/tasks/**").hasRole("TEAM_LEAD")
                                .requestMatchers(HttpMethod.PUT, "/api/teams/**", "/api/projects/**", "/api/tasks/**", "/api/comments/**").hasRole("TEAM_LEAD")
                                .requestMatchers(HttpMethod.DELETE, "/api/teams/**", "/api/projects/**", "/api/tasks/**", "/api/comments/**").hasRole("TEAM_LEAD")
                                .requestMatchers(HttpMethod.POST, "/api/tasks/assign/**").hasRole("TEAM_LEAD")
                                .requestMatchers("/api/management/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                                .requestMatchers(HttpMethod.GET, "/api/management/**").hasAnyRole("ADMIN_READ", "MANAGER_READ")
                                .requestMatchers(HttpMethod.POST, "/api/management/**").hasAnyRole("ADMIN_CREATE", "MANAGER_CREATE")
                                .requestMatchers(HttpMethod.PUT, "/api/management/**").hasAnyRole("ADMIN_UPDATE", "MANAGER_UPDATE")
                                .requestMatchers(HttpMethod.DELETE, "/api/management/**").hasAnyRole("ADMIN_DELETE", "MANAGER_DELETE")
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}