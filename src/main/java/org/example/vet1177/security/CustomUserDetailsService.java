package org.example.vet1177.security;

import org.example.vet1177.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bryggan mellan Spring Security och vår databas.
 *
 * Spring Security behöver veta hur man hämtar en användare givet ett "användarnamn".
 * I vårt fall är "användarnamnet" email-adressen (det som står i JWT:ns subject-claim).
 *
 * Flödet:
 *   JWT-token innehåller sub="sara@vet.se"
 *       ↓
 *   JwtAuthenticationFilter anropar loadUserByUsername("sara@vet.se")
 *       ↓
 *   Vi söker i databasen: userRepository.findByEmail("sara@vet.se")
 *       ↓
 *   Returnerar User-objektet (som implementerar UserDetails)
 *       ↓
 *   Spring Security sätter det i SecurityContext → @AuthenticationPrincipal fungerar
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email={}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found email={}", email);
                    return new UsernameNotFoundException("Användare hittades inte: " + email);
                });
    }
}
