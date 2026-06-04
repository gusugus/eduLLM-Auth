package repositorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import dto.UsuarioLogin;

@Repository
public class UsuarioRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Llama a la función comun.fn_login
    @SuppressWarnings("deprecation")
	public UsuarioLogin obtenerUsuarioPorUsername(String username) {
        String sql = "SELECT * FROM comun.fn_login(?)";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{username}, (rs, rowNum) -> {
                UsuarioLogin u = new UsuarioLogin();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setUsername(username);   // <-- Asignar el username pasado
                u.setPasswordHash(rs.getString("password_hash"));
                u.setIdRol(rs.getInt("id_rol"));
                u.setNombreRol(rs.getString("nombre_rol"));
                u.setPrimerNombre(rs.getString("primer_nombre"));
                u.setApellidoPaterno(rs.getString("apellido_paterno"));
                u.setApellidoMaterno(rs.getString("apellido_materno"));
                return u;
            });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
