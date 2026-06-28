package services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.reset-token-expiration-minutes:10}")
    private int expirationMinutes;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.protocol}")
    private String protocol;

    @Value("${app.port:8081}")
    private int port;

    @Value("${app.email-templates-path}")
    private String templatesPath;

    @Override
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        String resetLink = "%s%s:%d/reset-password?token=%s".formatted(protocol, baseUrl, port, resetToken);
        String html = loadTemplate("reset-password-email.html", Map.of(
            "username", username,
            "resetLink", resetLink,
            "expirationMinutes", String.valueOf(expirationMinutes)
        ));
        sendHtml(toEmail, "Recuperación de Contraseña - EduLLM", html);
    }

    @Override
    public void sendNewCredentialsEmail(String toEmail, String username, String newPassword) {
        String html = loadTemplate("new-credentials-email.html", Map.of(
            "username", username,
            "newPassword", newPassword
        ));
        sendHtml(toEmail, "Credenciales de Acceso - EduLLM", html);
    }

    private void sendHtml(String toEmail, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Correo enviado a: {}", toEmail);
        } catch (Exception e) {
            log.error("Error al enviar correo a: {}", toEmail, e);
        }
    }

    private String loadTemplate(String templateName, Map<String, String> placeholders) {
        try {
            Path path = Path.of(templatesPath, templateName);
            String content = Files.readString(path, StandardCharsets.UTF_8);
            for (var entry : placeholders.entrySet()) {
                content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
            return content;
        } catch (IOException e) {
            log.error("Error al cargar template de email: {}", templateName, e);
            throw new RuntimeException("No se pudo cargar el template: " + templateName, e);
        }
    }
}
