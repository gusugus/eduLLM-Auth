package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import autenticacionWeb.JwtUtil;
import dto.AuthenticationRequest;
import dto.AuthenticationResponse;
import dto.ForgotPasswordRequest;
import dto.ResetPasswordRequest;
import lombok.extern.slf4j.Slf4j;
import services.PasswordResetService;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordResetService passwordResetService;

    @GetMapping("/test")
    public String test() { return "OK"; }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
    	String username = authenticationRequest.getUsername();
        // 1. Autenticar y obtener el objeto Authentication
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                username, 
                authenticationRequest.getPassword()
            )
        );

        // 2. Extraer UserDetails del Authentication (sin llamar de nuevo al servicio)
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3. Generar el token
        final String jwt = jwtUtil.generateToken(userDetails);
        log.info("Se creo el token al user {}", username);
        // 4. Devolver respuesta
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
    
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        // Se procesa la solicitud. Por seguridad, siempre se devuelve el mismo mensaje.
        passwordResetService.createPasswordResetToken(request.getUsername());
        return ResponseEntity.ok("Si el usuario existe, recibirás un correo con las instrucciones.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        
        boolean isReset = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        if (isReset) {
            return ResponseEntity.ok("Contraseña restablecida exitosamente.");
        } else {
            return ResponseEntity.badRequest().body("Token inválido o expirado.");
        }
    }
    
    
}