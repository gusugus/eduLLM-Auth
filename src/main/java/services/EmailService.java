package services;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String username, String resetToken);
}