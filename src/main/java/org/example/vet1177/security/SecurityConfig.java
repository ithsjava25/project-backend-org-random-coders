package org.example.vet1177.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Hela säkerhetskonfigurationen för applikationen.
 *
 * @EnableConfigurationProperties(JwtProperties.class) — talar om för Spring att
 * läsa jwt.secret-key och jwt.expiration-ms från application.properties och
 * skapa en JwtProperties-bean som kan injiceras i JwtService.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * SecurityFilterChain — definierar hela säkerhetskedjan.
     *
     * Varje rad i kedjan lägger till en regel:
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CORS — tillåter frontend (annan port/domän) att anropa vårt API.
                // Utan detta blockerar webbläsaren alla cross-origin requests.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF — avstängt. CSRF-skydd behövs för cookie-baserad auth
                // men är irrelevant för JWT (token skickas i header, inte cookie).
                .csrf(csrf -> csrf.disable())

                // Sessionhantering — STATELESS.
                // Servern sparar ingen session. All info finns i JWT-tokenen.
                // Varje request är oberoende — filtret avkodar token varje gång.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Endpoint-regler — vilka URLs kräver vad.
                // Ordningen spelar roll: första matchande regel vinner.
                .authorizeHttpRequests(auth -> auth
                        // Öppna endpoints — ingen token krävs
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/clinics", "/api/clinics/**").permitAll()

                        // Alla andra API-anrop kräver att man är inloggad
                        .anyRequest().authenticated()
                )

                // Registrera vårt JWT-filter FÖRE Springs inbyggda UsernamePasswordAuthenticationFilter.
                // Det betyder att JWT-filtret körs först och sätter SecurityContext
                // innan Spring kollar om användaren har rätt behörighet.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    /**
     * JwtDecoder-bean — Spring Security använder denna för att verifiera JWT-tokens.
     * Vi hämtar den från JwtService som redan har konfigurerat den med vår hemliga nyckel.
     */
    @Bean
    public JwtDecoder jwtDecoder(JwtService jwtService) {
        return jwtService.getJwtDecoder();
    }

    /**
     * PasswordEncoder — BCrypt är industristandard.
     * Används vid registrering (hasha lösenord) och login (verifiera lösenord).
     * BCrypt saltar automatiskt — samma lösenord ger olika hash varje gång.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager — hanterar login-flödet.
     *
     * När Person B:s AuthController anropar authManager.authenticate(email, password):
     * 1. AuthenticationManager delegerar till DaoAuthenticationProvider
     * 2. DaoAuthenticationProvider anropar CustomUserDetailsService.loadUserByUsername(email)
     * 3. DaoAuthenticationProvider jämför lösenord med PasswordEncoder.matches()
     * 4. Om match → returnerar Authentication med User-objektet
     * 5. Om mismatch → kastar BadCredentialsException
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * DaoAuthenticationProvider — kopplar ihop UserDetailsService med PasswordEncoder.
     * "Dao" = Data Access Object — den hämtar data från vår databas.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * CORS-konfiguration — vilka origins (domäner) som får anropa vårt API.
     *
     * I utveckling: localhost:5173 (Vite/React), localhost:3000 (Next.js)
     * I produktion: byt till er riktiga frontend-URL.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
