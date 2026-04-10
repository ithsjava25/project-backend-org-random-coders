package org.example.vet1177.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Läser JWT-konfiguration från application.properties.
 *
 * jwt.secret-key  → den hemliga nyckeln som används för att signera/verifiera tokens (HMAC SHA-256)
 * jwt.expiration-ms → hur länge en token är giltig i millisekunder (86400000 = 24 timmar)
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secretKey;
    private long expirationMs;

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public long getExpirationMs() { return expirationMs; }
    public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
}
