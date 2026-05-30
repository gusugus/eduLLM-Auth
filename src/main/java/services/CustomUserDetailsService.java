package services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import dto.UsuarioLogin;
import lombok.extern.slf4j.Slf4j;
import repositorio.UsuarioRepository;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsuarioLogin usuario = usuarioRepository.obtenerUsuarioPorUsername(username);
        if (usuario == null) {
            log.warn("Intento de login con usuario inexistente: {}", username);
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }
        log.debug("Usuario cargado desde BD: {}", usuario.getUsername());
        
        // Construir autoridades (rol)
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getNombreRol().toUpperCase());
        // El rol como string (sin prefijo "ROLE_") lo puedes guardar aparte
        String rolStr = "ROLE_" + usuario.getNombreRol().toUpperCase();

        return new CustomUserDetails(
            usuario.getIdUsuario(),      // asumiendo que UsuarioLogin tiene getIdUsuario()
            usuario.getUsername(),
            usuario.getPasswordHash(),
            Collections.singletonList(authority),
            rolStr
        );
        
    }
}