package dto;

public class ForgotPasswordRequest {
    private String username;

    // Constructor por defecto (necesario para Jackson)
    public ForgotPasswordRequest() {}

    // Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}