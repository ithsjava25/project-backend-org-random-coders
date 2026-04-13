package org.example.vet1177.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.example.vet1177.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Hanterar skapande och validering av JWT-tokens.
 *
 * Flödet:
 * 1. Användaren loggar in med email + lösenord
 * 2. AuthService anropar generateToken(user) → får tillbaka en JWT-sträng
 * 3. Frontend skickar JWT:n i varje request: "Authorization: Bearer <token>"
 * 4. JwtAuthenticationFilter fångar headern, anropar decode(token) → får tillbaka claims
 * 5. Filtret laddar användaren från DB och sätter den i SecurityContext
 *
 * Vi använder HS256 (HMAC med SHA-256) — en symmetrisk algoritm där samma
 * hemliga nyckel används för att både signera och verifiera tokens.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long expirationMs;

    public JwtService(JwtProperties jwtProperties) {
        // Skapar en kryptografisk nyckel från vår hemliga sträng i application.properties.
        // SecretKeySpec tar emot byte-arrayen + algoritmen och ger oss ett SecretKey-objekt
        // som Java:s krypto-API kan använda.
        SecretKey key = new SecretKeySpec(
                jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        // JwtEncoder — skapar nya tokens.
        // ImmutableSecret wrapprar vår nyckel så att Nimbus-biblioteket kan använda den.
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        // JwtDecoder — avkodar och validerar befintliga tokens.
        // Den kontrollerar automatiskt: signatur (är tokenen äkta?) och utgångstid (har den gått ut?).
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        this.expirationMs = jwtProperties.getExpirationMs();
    }

    /**
     * Skapar en ny JWT för en inloggad användare.
     *
     * Tokenen innehåller "claims" — nyckel-värde-par med information:
     * - sub (subject): användarens email — det unika identifieringsvärdet
     * - userId: UUID:t — behövs för att hämta användaren från DB
     * - role: ROLE_VET / ROLE_OWNER / ROLE_ADMIN — för rollbaserad åtkomstkontroll
     * - iat (issued at): när tokenen skapades
     * - exp (expires at): när tokenen slutar gälla
     */
    public String generateToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("role", "ROLE_" + user.getRole().name())
                .issuedAt(now)
                .expiresAt(now.plusMillis(expirationMs))
                .build();

        // Talar om att vi vill signera med HS256-algoritmen
        JwtEncoderParameters params = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        );

        String token = jwtEncoder.encode(params).getTokenValue();
        log.info("Generated JWT for user email={}", user.getEmail());
        return token;
    }

    /**
     * Avkodar och validerar en JWT-sträng.
     *
     * Nimbus-biblioteket gör automatiskt:
     * 1. Kontrollerar att signaturen stämmer (ingen har ändrat innehållet)
     * 2. Kontrollerar att tokenen inte har gått ut (exp > nu)
     *
     * Om något är fel kastas JwtException och anroparen vet att tokenen är ogiltig.
     *
     * @return Jwt-objekt med alla claims om tokenen är giltig
     * @throws JwtException om tokenen är ogiltig, manipulerad eller utgången
     */
    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }

    /**
     * Exponerar JwtDecoder som en bean som Spring Security kan använda.
     * SecurityConfig behöver en JwtDecoder-bean för att konfigurera oauth2ResourceServer().
     */
    public JwtDecoder getJwtDecoder() {
        return jwtDecoder;
    }
}
