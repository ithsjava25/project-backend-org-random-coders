package org.example.vet1177.config;

import org.example.vet1177.repository.UserRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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

    /**
     * Skapar dev-autentiseringsfiltret som läser X-Dev-User-headern.
     * Registreras INTE automatiskt som servlet-filter (se devAuthFilterRegistration).
     * Läggs istället till inuti Spring Security's filter-kedja via SecurityConfig.
     */
    @Bean("devAuthFilter")
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

    /**
     * Förhindrar att Spring Boot auto-registrerar devAuthFilter som servlet-filter.
     * Vi lägger till det manuellt i SecurityConfig istället, inuti säkerhetskedjan.
     * (Måste vara inuti kedjan — annars skriver SecurityContextHolderFilter över SecurityContext.)
     */
    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> devAuthFilterRegistration(
            @Lazy OncePerRequestFilter devAuthFilter) {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>(devAuthFilter);
        registration.setEnabled(false);
        return registration;
    }
}