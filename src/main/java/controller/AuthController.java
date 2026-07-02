// AuthController.java - Versión que soporta ambos métodos
package controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import autenticacionWeb.JwtUtil;
import dto.AuthenticationRequest;
import dto.AuthenticationResponse;
import dto.ForgotPasswordRequest;
import dto.RecreateCredentialsRequest;
import dto.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import services.CustomUserDetails;
import services.PasswordResetService;
import services.PasswordValidator;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Value("${jwt.expiration}")
    private int expirationMillis;
    
    @Autowired
    private PasswordResetService passwordResetService;

    @GetMapping("/test")
    public String test() { return "OK"; }

    @Value("${app.gateway-url}")
    String gatewayUrl;

    @Value("${app.redirect-delay}")
    private int redirectDelay;
    
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody AuthenticationRequest authRequest,
            HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Verificar si debe cambiar la contraseña (primer ingreso)
            String activeToken = passwordResetService.getActiveResetToken(userDetails.getUsername());
            if (activeToken != null) {
                Map<String, Object> mustChange = new HashMap<>();
                mustChange.put("mustChangePassword", true);
                mustChange.put("resetToken", activeToken);
                mustChange.put("message", "Debes cambiar tu contraseña antes de continuar.");
                return ResponseEntity.ok(mustChange);
            }

            String jwt = jwtUtil.generateToken(
                userDetails.getUsername(),
                userDetails.getIdUsuario(),
                userDetails.getRol()
            );

            // Crear cookie HttpOnly con el JWT
            ResponseCookie cookie = ResponseCookie.from("jwtToken", jwt)
                .httpOnly(true)           // No accesible desde JavaScript
                .secure(false)            // true en producción con HTTPS
                .path("/")                // Disponible en toda la aplicación
                .maxAge(expirationMillis/100)     // Tiempo  de expiracion
                .sameSite("Lax")          // Protección CSRF
                .build();
            
            response.addHeader("Set-Cookie", cookie.toString());

            // 🔥 IMPORTANTE: Redirigir al gateway, no al frontend directamente
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("token", jwt);
            responseBody.put("idUsuario", userDetails.getIdUsuario());
            responseBody.put("rol", userDetails.getRol());
            responseBody.put("redirectUrl", "/login-success");
            responseBody.put("redirectDelay", redirectDelay);
            
            log.info("Login exitoso para usuario: {}", userDetails.getUsername());
            return ResponseEntity.ok(responseBody);
            
        } catch (BadCredentialsException e) {
            log.warn("Intento de login fallido para usuario: {}", authRequest.getUsername());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Credenciales inválidas");
            return ResponseEntity.status(401).body(error);
        }
    }
    
    
    @GetMapping("/verify")
    public ResponseEntity<?> verify(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body(Map.of("authenticated", false));
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        boolean found = passwordResetService.createPasswordResetToken(request.getUsername());
        if (found) {
            return ResponseEntity.ok("Correo de recuperación enviado. Revisa tu bandeja de entrada.");
        } else {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }
    }


    @PostMapping("/recreate-credentials")
    public ResponseEntity<?> recreateCredentials(@RequestBody RecreateCredentialsRequest request) {
        String result = passwordResetService.recreateCredentials(request.getUsername());
        if (result != null) {
            return ResponseEntity.ok(Map.of("message", "Credenciales actualizadas. Revisa tu correo electrónico."));
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Usuario no encontrado"));
        }
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<?> verifyResetToken(@RequestParam("token") String token) {
        boolean valid = passwordResetService.verifyToken(token);
        Map<String, Object> response = new HashMap<>();
        response.put("valid", valid);
        if (!valid) {
            response.put("message", "El enlace ha expirado. Solicita un nuevo restablecimiento.");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        List<String> errors = PasswordValidator.validate(request.getNewPassword());
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(String.join(" ", errors));
        }
        boolean isReset = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        if (isReset) {
            return ResponseEntity.ok("Contraseña restablecida exitosamente.");
        } else {
            return ResponseEntity.badRequest().body("Token inválido o expirado.");
        }
    }
    
    
}