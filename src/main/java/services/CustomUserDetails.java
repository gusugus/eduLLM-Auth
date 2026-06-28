package services;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;

public class CustomUserDetails extends User {
    private static final long serialVersionUID = 1L;
	private final int idUsuario;
    private final String rol;

    public CustomUserDetails(int idUsuario, String username, String password,
                             Collection<? extends GrantedAuthority> authorities, String rol) {
        super(username, password, authorities);
        this.idUsuario = idUsuario;
        this.rol = rol;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getRol() {
        return rol;
    }
}