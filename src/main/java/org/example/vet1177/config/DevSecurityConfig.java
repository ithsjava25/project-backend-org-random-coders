package org.example.vet1177.config;

import org.example.vet1177.entities.User;
import org.example.vet1177.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@Profile("dev")  // ← bara aktiv i dev-profil
public class DevSecurityConfig {

    private final UserRepository userRepository;

    public DevSecurityConfig(@Lazy UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public OncePerRequestFilter devAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain)
                    throws ServletException, IOException {

                // Läs vilken användare från header
                // X-Dev-User: anna@test.se
                String email = request.getHeader("X-Dev-User");

                if (email != null) {
                    userRepository.findByEmail(email).ifPresent(user -> {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        user, null, user.getAuthorities()
                                );
                        SecurityContextHolder.getContext()
                                .setAuthentication(auth);
                    });
                }

                filterChain.doFilter(request, response);
            }
        };
    }
}