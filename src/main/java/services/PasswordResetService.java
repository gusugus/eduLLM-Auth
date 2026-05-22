package services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailService emailService;
    
    @Value("${app.reset-token-expiration-minutes:10}")
    private int expirationMinutes;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Genera un token, lo guarda en la BD y envía el correo
    public void createPasswordResetToken(String username) {
        String sqlFindUser = "SELECT username, correo FROM comun.admin_usuario WHERE username = ?";
        
        try {
            // 1. Verificar si el usuario existe
            var user = jdbcTemplate.queryForMap(sqlFindUser, username);
            String dbUsername = (String) user.get("username");
            String correo = (String) user.get("correo");

            // 2. Generar token único
            String token = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(expirationMinutes);

            // 3. Guardar token y expiración en la BD
            String sqlUpdateToken = "UPDATE comun.admin_usuario SET reset_token = ?, reset_token_expiry = ? WHERE username = ?";
            jdbcTemplate.update(sqlUpdateToken, token, expiry, dbUsername);

            // 4. Enviar correo con el enlace
            // Nota: Para enviar el correo, necesitamos el email del usuario.
            // Asumimos que el username ES el email. Ajusta el query si tienes un campo email separado.
            emailService.sendPasswordResetEmail(correo, dbUsername, token);

        } catch (Exception e) {
            // Por seguridad, no lanzamos una excepción específica. Simplemente registramos el error internamente.
            System.err.println("Error en el proceso de reset para el usuario: " + username+e.toString());
        }
    }

    // Valida el token y actualiza la contraseña
    public boolean resetPassword(String token, String newPassword) {
        String sqlFindToken = "SELECT username, reset_token_expiry FROM comun.admin_usuario WHERE reset_token = ?";
        
        try {
            var result = jdbcTemplate.queryForMap(sqlFindToken, token);
            String username = (String) result.get("username");
            LocalDateTime expiry = ((java.sql.Timestamp) result.get("reset_token_expiry")).toLocalDateTime();

            // Verificar si el token ha expirado
            if (expiry.isBefore(LocalDateTime.now())) {
                return false; // Token expirado
            }

            // Codificar la nueva contraseña y actualizar
            String encodedPassword = passwordEncoder.encode(newPassword);
            String sqlUpdatePassword = "UPDATE comun.admin_usuario SET password_hash = ?, reset_token = NULL, reset_token_expiry = NULL WHERE reset_token = ?";
            int updated = jdbcTemplate.update(sqlUpdatePassword, encodedPassword, token);

            return updated > 0;
        } catch (Exception e) {
            return false; // Token inválido o error
        }
    }
}