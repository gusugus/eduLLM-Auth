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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/test")
    public String test() { return "OK"; }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        // 1. Autenticar y obtener el objeto Authentication
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authenticationRequest.getUsername(), 
                authenticationRequest.getPassword()
            )
        );

        // 2. Extraer UserDetails del Authentication (sin llamar de nuevo al servicio)
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3. Generar el token
        final String jwt = jwtUtil.generateToken(userDetails);

        // 4. Devolver respuesta
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
}