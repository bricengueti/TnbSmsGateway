package TNB.SmsGateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 jours par défaut
    private long refreshExpiration;

    /**
     * Récupérer la clé de signature
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== ACCESS TOKEN ====================

    /**
     * Générer un access token
     */
    public String generateAccessToken(UUID userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("userId", userId.toString())
                .claim("type", "ACCESS")
                .claim("generatedAt", now.getTime())
                .claim("expiresAt", expiry.getTime())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Générer un access token avec claims personnalisés
     */
    public String generateAccessToken(UUID userId, String email, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        var builder = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("userId", userId.toString())
                .claim("type", "ACCESS")
                .claim("generatedAt", now.getTime())
                .claim("expiresAt", expiry.getTime())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey());

        if (extraClaims != null) {
            extraClaims.forEach(builder::claim);
        }

        return builder.compact();
    }

    // ==================== REFRESH TOKEN ====================

    /**
     * Générer un refresh token (durée plus longue)
     */
    public String generateRefreshToken(UUID userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("userId", userId.toString())
                .claim("type", "REFRESH")
                .claim("generatedAt", now.getTime())
                .claim("expiresAt", expiry.getTime())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Générer les deux tokens
     */
    public TokenPair generateTokenPair(UUID userId, String email) {
        String accessToken = generateAccessToken(userId, email);
        String refreshToken = generateRefreshToken(userId, email);

        return new TokenPair(accessToken, refreshToken);
    }

    /**
     * Générer les deux tokens avec claims personnalisés
     */
    public TokenPair generateTokenPair(UUID userId, String email, Map<String, Object> extraClaims) {
        String accessToken = generateAccessToken(userId, email, extraClaims);
        String refreshToken = generateRefreshToken(userId, email);

        return new TokenPair(accessToken, refreshToken);
    }

    // ==================== VALIDATION ====================

    /**
     * Valider un token (access ou refresh)
     */
    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Vérifier si le token est un access token
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = validateToken(token);
            return "ACCESS".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifier si le token est un refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = validateToken(token);
            return "REFRESH".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== EXTRACTION ====================

    public UUID getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("email", String.class);
    }

    public String getTokenType(String token) {
        Claims claims = validateToken(token);
        return claims.get("type", String.class);
    }

    public Date getGeneratedAtFromToken(String token) {
        Claims claims = validateToken(token);
        Long generatedAt = claims.get("generatedAt", Long.class);
        return generatedAt != null ? new Date(generatedAt) : claims.getIssuedAt();
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getExpiration();
    }

    public <T> T getClaimFromToken(String token, String claimKey, Class<T> clazz) {
        Claims claims = validateToken(token);
        return claims.get(claimKey, clazz);
    }

    public Map<String, Object> getAllClaims(String token) {
        Claims claims = validateToken(token);
        Map<String, Object> allClaims = new HashMap<>();

        allClaims.put("subject", claims.getSubject());
        allClaims.put("email", claims.get("email", String.class));
        allClaims.put("userId", claims.get("userId", String.class));
        allClaims.put("type", claims.get("type", String.class));
        allClaims.put("generatedAt", claims.get("generatedAt", Long.class));
        allClaims.put("expiresAt", claims.get("expiresAt", Long.class));
        allClaims.put("issuedAt", claims.getIssuedAt());
        allClaims.put("expiration", claims.getExpiration());

        return allClaims;
    }

    // ==================== VERIFICATION ====================

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isValidToken(String token) {
        try {
            validateToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidAccessToken(String token) {
        return isValidToken(token) && isAccessToken(token);
    }

    public boolean isValidRefreshToken(String token) {
        return isValidToken(token) && isRefreshToken(token);
    }

    public boolean isTokenForUser(String token, UUID userId) {
        try {
            UUID tokenUserId = getUserIdFromToken(token);
            return tokenUserId.equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== TEMPS RESTANT ====================

    public long getRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }

    public long getRemainingTimeInSeconds(String token) {
        return getRemainingTime(token) / 1000;
    }

    public boolean willExpireInLessThan(String token, long minutes) {
        long remainingSeconds = getRemainingTimeInSeconds(token);
        return remainingSeconds < minutes * 60;
    }

    // ==================== TOKEN PAIR DTO ====================

    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }
}