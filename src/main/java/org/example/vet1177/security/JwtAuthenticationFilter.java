package org.example.vet1177.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter som körs EN gång per HTTP-request (OncePerRequestFilter).
 *
 * Vad händer steg för steg:
 *
 *   [Klient] → HTTP Request med "Authorization: Bearer eyJhbG..."
 *       ↓
 *   [JwtAuthenticationFilter]
 *       1. Läser Authorization-headern
 *       2. Extraherar token-strängen (allt efter "Bearer ")
 *       3. Avkodar token via JwtService → får email + roll
 *       4. Laddar User från DB via CustomUserDetailsService
 *       5. Skapar ett Authentication-objekt och sätter det i SecurityContext
 *       ↓
 *   [Spring Security]
 *       Kollar SecurityContext: finns en autentiserad användare?
 *       Om ja → kontrollerar att användaren har rätt roll för endpointen
 *       Om nej → returnerar 401 Unauthorized
 *       ↓
 *   [Controller]
 *       @AuthenticationPrincipal User currentUser ← hämtas från SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Läs Authorization-headern
        // Formatet är: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        String authHeader = request.getHeader("Authorization");

        // Om headern saknas eller inte börjar med "Bearer " → skippa filtret.
        // Requesten släpps vidare i kedjan. Om endpointen kräver auth
        // kommer Spring Security att returnera 401 automatiskt.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extrahera token-strängen (ta bort "Bearer " som är 7 tecken)
        String token = authHeader.substring(7);

        try {
            // 3. Avkoda och validera token.
            // Om signaturen inte stämmer eller token har gått ut kastar decode() JwtException.
            Jwt jwt = jwtService.decode(token);

            // 4. Hämta email från token (subject-claim)
            String email = jwt.getSubject();

            // Kolla att vi inte redan har autentiserat denna request
            // (kan hända om flera filter kör i kedjan)
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Ladda det fulla User-objektet från databasen.
                // Vi behöver hela entiteten (med id, klinik, roll) för @AuthenticationPrincipal.
                var userDetails = userDetailsService.loadUserByUsername(email);

                // 6. Skapa ett Authentication-objekt.
                // UsernamePasswordAuthenticationToken är Springs standardklass för "en autentiserad användare".
                // - Första argumentet (principal) → User-objektet (det som @AuthenticationPrincipal ger)
                // - Andra argumentet (credentials) → null (vi behöver inte lösenordet, token räcker)
                // - Tredje argumentet (authorities) → rollerna (ROLE_VET, ROLE_ADMIN, etc.)
                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                // Kopplar request-detaljer (IP-adress, session-id) till autentiseringen — för loggning.
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 7. Sätt autentiseringen i SecurityContext.
                // Från och med nu "vet" Spring Security att det finns en inloggad användare.
                // Alla controllers kan hämta den via @AuthenticationPrincipal.
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated user email={} via JWT", email);
            }

        } catch (JwtException e) {
            // Token är ogiltig (felaktig signatur, utgången, korrupt).
            // Vi loggar och släpper vidare — Spring Security returnerar 401 automatiskt
            // eftersom SecurityContext förblir tom.
            log.warn("Invalid JWT token: {}", e.getMessage());
        }

        // Släpp vidare requesten till nästa filter i kedjan (och sedan till controllern)
        filterChain.doFilter(request, response);
    }
}
