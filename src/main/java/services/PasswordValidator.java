package services;

import java.util.ArrayList;
import java.util.List;

public class PasswordValidator {

    public static List<String> validate(String password) {
        List<String> errors = new ArrayList<>();
        if (password == null || password.length() < 6 || password.length() > 10) {
            errors.add("La contraseña debe tener entre 6 y 10 caracteres.");
        }
        if (!password.matches(".*[A-Z].*")) {
            errors.add("Debe incluir al menos una mayúscula.");
        }
        if (!password.matches(".*[a-z].*")) {
            errors.add("Debe incluir al menos una minúscula.");
        }
        if (!password.matches(".*\\d.*")) {
            errors.add("Debe incluir al menos un número.");
        }
        return errors;
    }
}
