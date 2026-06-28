package services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.EmptyResultDataAccessException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Slf4j
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
    public boolean createPasswordResetToken(String username) {
        String sqlFindUser = "SELECT username, correo FROM comun.tbl_m_usuario WHERE username = ?";
        
        try {
            var user = jdbcTemplate.queryForMap(sqlFindUser, username);
            String dbUsername = (String) user.get("username");
            String correo = (String) user.get("correo");

            String token = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(expirationMinutes);

            String sqlUpdateToken = "UPDATE comun.tbl_m_usuario SET reset_token = ?, reset_token_expiry = ? WHERE username = ?";
            jdbcTemplate.update(sqlUpdateToken, token, expiry, dbUsername);

            emailService.sendPasswordResetEmail(correo, dbUsername, token);
            
            log.info("Correo de reset enviado a: {}", correo);
            return true;

        } catch (EmptyResultDataAccessException e) {
            log.info("Solicitud de reset para usuario inexistente: {}", username);
            return false;
        } catch (Exception e) {
            log.error("Error en proceso de reset para usuario: {}", username, e);
            return false;
        }
    }

    public boolean verifyToken(String token) {
        String sql = "SELECT reset_token_expiry FROM comun.tbl_m_usuario WHERE reset_token = ?";
        try {
            var result = jdbcTemplate.queryForMap(sql, token);
            LocalDateTime expiry = ((java.sql.Timestamp) result.get("reset_token_expiry")).toLocalDateTime();
            return !expiry.isBefore(LocalDateTime.now());
        } catch (Exception e) {
            return false;
        }
    }

    public String getActiveResetToken(String username) {
        String sql = "SELECT reset_token, reset_token_expiry FROM comun.tbl_m_usuario WHERE username = ?";
        try {
            var result = jdbcTemplate.queryForMap(sql, username);
            String resetToken = (String) result.get("reset_token");
            if (resetToken == null) return null;
            LocalDateTime expiry = ((java.sql.Timestamp) result.get("reset_token_expiry")).toLocalDateTime();
            if (expiry.isBefore(LocalDateTime.now())) return null;
            return resetToken;
        } catch (Exception e) {
            return null;
        }
    }

    public String recreateCredentials(String username) {
        String sqlFind = "SELECT correo FROM comun.tbl_m_usuario WHERE username = ?";
        try {
            var user = jdbcTemplate.queryForMap(sqlFind, username);
            String correo = (String) user.get("correo");

            String newPassword = PasswordGenerator.generate(6, 10, true, true, true, true);
            String encodedPassword = passwordEncoder.encode(newPassword);
            String resetToken = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusDays(7);

            String sqlUpdate = "UPDATE comun.tbl_m_usuario SET password_hash = ?, reset_token = ?, reset_token_expiry = ? WHERE username = ?";
            jdbcTemplate.update(sqlUpdate, encodedPassword, resetToken, expiry, username);

            emailService.sendNewCredentialsEmail(correo, username, newPassword);

            log.info("Credenciales recreadas y enviadas a: {}", correo);
            return newPassword;

        } catch (Exception e) {
            log.error("Error al recrear credenciales para: {}", username, e);
            return null;
        }
    }

    // Valida el token y actualiza la contraseña
    public boolean resetPassword(String token, String newPassword) {
        List<String> errors = PasswordValidator.validate(newPassword);
        if (!errors.isEmpty()) {
            log.warn("La nueva contraseña no cumple la política para token: {}", token);
            return false;
        }

        String sqlFindToken = "SELECT username, reset_token_expiry FROM comun.tbl_m_usuario WHERE reset_token = ?";
        
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
            String sqlUpdatePassword = "UPDATE comun.tbl_m_usuario SET password_hash = ?, reset_token = NULL, reset_token_expiry = NULL WHERE reset_token = ?";
            int updated = jdbcTemplate.update(sqlUpdatePassword, encodedPassword, token);

            if (updated > 0) {
                log.info("Contraseña restablecida exitosamente para usuario: {}", username);
                return true;
            } else {
                log.warn("No se actualizó ningún registro para token: {}", token);
                return false;
            }
            
        } catch (Exception e) {
        	log.info("Error en reset password:", e);
            return false; // Token inválido o error
        }
    }
}