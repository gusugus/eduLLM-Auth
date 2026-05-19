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
        log.warn("Login correcto: {} con idRol", usuario.getUsername(), usuario.getIdRol());
        return User.builder()
        	    .username(usuario.getUsername())
        	    .password(usuario.getPasswordHash())
        	    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getIdRol())))
        	    .build();
    }
}