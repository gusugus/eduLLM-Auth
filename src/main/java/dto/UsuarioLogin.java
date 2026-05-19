package dto;

import lombok.Data;

@Data
public class UsuarioLogin {
 private Integer idUsuario;
 private String username;        // <-- Agregar esto
 private String passwordHash;
 private Integer idRol;
 private String primerNombre;
 private String apellidoPaterno;
 private String apellidoMaterno;
}