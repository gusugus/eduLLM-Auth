package autenticacionWeb;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // Obtener la clave de firma (SecretKey para HS256)
    private SecretKey getSignKey() {
        // Si el secret está en Base64, decodifícalo; si es texto plano, usa getBytes()
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extraer el username (subject) del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extraer la fecha de expiración
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extraer un claim específico
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Validar token contra UserDetails
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Verificar si el token ha expirado
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Generar token solo con username
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    // Generar token con claims adicionales (userId, rol, etc.)
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    // Construir y firmar el token (API moderna)
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)                     // Claims adicionales
                .subject(subject)                   // Username como subject
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())             // Firma con la SecretKey
                .compact();
    }
    
    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    // Extraer todos los claims del token (API moderna)
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())           // Verificar con la misma clave
                .build()
                .parseSignedClaims(token)           // parseSignedClaims en lugar de parseClaimsJws
                .getPayload();                      // getPayload en lugar de getBody
    }
}