package services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

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
    
    @Override
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Recuperación de Contraseña - EduLLM");

        String resetLink = "%s%s:%d/reset-password?token=%s".formatted(protocol, baseUrl, port, resetToken);
        String emailBody = "Hola " + username + ",\n\n"
                + "Hemos recibido una solicitud para restablecer tu contraseña. "
                + "Haz clic en el siguiente enlace para continuar:\n\n"
                + resetLink + "\n\n"
                + "Si no solicitaste este cambio, puedes ignorar este correo.\n\n"
                + "Este enlace expirará en" + expirationMinutes + " minutos.\n\n"
                + "Saludos,\n"
                + "El equipo de EduLLM";
        message.setText(emailBody);
        log.info("Envio de recuperacion de contraseña al correo {}", toEmail);
        mailSender.send(message);
    }
}