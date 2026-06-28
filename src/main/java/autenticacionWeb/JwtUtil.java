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

    /**
     * Obtener la clave de firma (SecretKey para HS256)
     * @return key secret
     */
    private SecretKey getSignKey() {
        // Si el secret está en Base64, decodifícalo; si es texto plano, usa getBytes()
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extraer el username (subject) del token
     * @param token
     * @return Username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extraer la fecha de expiración
     * @param token Token
     * @return Fecha de expiracion
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 
    /**
     * Extraer un claim específico
     * @param <T>
     * @param token
     * @param claimsResolver
     * @return
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Validar token contra UserDetails
     * @param token
     * @param userDetails
     * @return
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Verificar si el token ha expirado
     * @param token
     * @return true si el token ha exxpirado
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }



    /**
     * Funcion para construir y firmar el token (API moderna)
     * @param claims Datos Verificados
     * @param subject Username
     * @return token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)                     // Claims adicionales
                .subject(subject)                   // Username como subject
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())             // Firma con la SecretKey
                .compact();
    }
    
    /**
     * Extraer todos los claims del token (API moderna)
     * @param token
     * @return Datos Validados
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())           // Verificar con la misma clave
                .build()
                .parseSignedClaims(token)           // parseSignedClaims en lugar de parseClaimsJws
                .getPayload();                      // getPayload en lugar de getBody
    }

    /**
     * Genera token
     * @param username Usuario 
     * @param idUsuario idUsuario
     * @param rol NomreRol
     * @return
     */
    public String generateToken(String username, int idUsuario, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("idUsuario", idUsuario);
        claims.put("rol", rol);
        return createToken(claims, username);
    }

    public Long extractIdUsuario(String token) {
        return extractAllClaims(token).get("idUsuario", Long.class);
    }

    
}