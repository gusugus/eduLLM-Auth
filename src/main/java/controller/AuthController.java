// AuthController.java - Versión que soporta ambos métodos
package controller;

import java.util.HashMap;
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
import dto.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import services.CustomUserDetails;
import services.PasswordResetService;

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
            //responseBody.put("token", jwt);
            // Devolver la URL de redirección al gateway
            responseBody.put("redirectUrl", gatewayUrl + "/login-success");
            
            log.info("Login exitoso para usuario: {}", userDetails.getUsername());
            return ResponseEntity.ok(responseBody);
            
        } catch (BadCredentialsException e) {
            log.warn("Intento de login fallido para usuario: {}", authRequest.getUsername());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Credenciales inválidas");
            return ResponseEntity.status(401).body(error);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Limpiar la cookie
        ResponseCookie cookie = ResponseCookie.from("jwtToken", "")
            .httpOnly(true)
            .path("/")
            .maxAge(0)  // Expira inmediatamente
            .sameSite("Lax")
            .build();
        
        response.addHeader("Set-Cookie", cookie.toString());
        log.info("Logout exitoso");
        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
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