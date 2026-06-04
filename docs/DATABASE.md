[← Volver al índice](INDEX.md)

# 🗄️ Database Reference - autenticacionWeb

Este documento detalla el esquema de base de datos, funciones almacenadas y la interacción de la capa de persistencia utilizada por el microservicio de autenticación.

---

## ⚙️ Información del Gestor de Base de Datos

* **Motor de Base de Datos:** PostgreSQL
* **Nombre de la Base de Datos:** `edu_llm`
* **Esquema:** `comun`
* **Estrategia de Inicialización:** `spring.jpa.hibernate.ddl-auto: none` (El microservicio no modifica ni crea tablas automáticamente, asume que el esquema ya existe y está administrado por el proyecto principal).

---

## 📋 Listado de Tablas y Colecciones

### Tabla: `comun.admin_usuario`

* **Propósito:** Almacena los registros de usuarios del sistema, sus correos electrónicos para recuperación, contraseñas encriptadas y los datos temporales del flujo de restablecimiento de contraseña.
* **Columnas:**

| Columna | Tipo | Restricciones | Default | Descripción |
|---|---|---|---|---|
| `username` | `VARCHAR(100)` | PRIMARY KEY, NOT NULL | - | Nombre de usuario único en el sistema. |
| `correo` | `VARCHAR(255)` | NOT NULL, UNIQUE | - | Correo electrónico utilizado para notificaciones y restablecimiento. |
| `password_hash` | `VARCHAR(255)` | NOT NULL | - | Hash BCrypt de la contraseña del usuario. |
| `reset_token` | `VARCHAR(255)` | NULL | - | Token temporal (UUID v4) para restablecer la contraseña. |
| `reset_token_expiry` | `TIMESTAMP` | NULL | - | Fecha y hora en la que el token de restablecimiento expira. |

* **Índices:**
  - `admin_usuario_pkey` en la columna `username` (B-tree, único, implícito por PK).
  - `idx_admin_usuario_reset_token` en `reset_token` (B-tree, para búsquedas veloces en la recuperación).
* **Ejemplo de Registro:**

```sql
INSERT INTO comun.admin_usuario (username, correo, password_hash, reset_token, reset_token_expiry)
VALUES (
  'profesor1', 
  'profesor1@edullm.edu', 
  '$2a$10$Y5n/g2LpM89K596T2R3UvO7D/nZt.aH4xS/3dF4eR5g6h7i8j9k0l', 
  'd748f32c-29b1-4c7c-87d5-8664b4198cc7', 
  '2026-05-25 10:15:00'
);
```

---

## 🛠️ Funciones Almacenadas (Stored Procedures)

### Función: `comun.fn_login`

* **Propósito:** Verifica la existencia de un usuario durante el flujo de autenticación y extrae sus datos básicos junto con su rol asignado.
* **Firma SQL:**
  ```sql
  SELECT * FROM comun.fn_login(p_username TEXT);
  ```
* **Columnas que retorna:**

| Nombre Columna | Tipo SQL | Descripción | Mapeo en DTO (`UsuarioLogin`) |
|---|---|---|---|
| `id_usuario` | `INTEGER` | ID único numérico del usuario. | `idUsuario` (Integer) |
| `password_hash` | `TEXT` | Hash BCrypt de la contraseña para verificación. | `passwordHash` (String) |
| `id_rol` | `INTEGER` | ID numérico del rol del usuario (ej: 1 = Admin, 2 = Profesor). | `idRol` (Integer) |
| `primer_nombre` | `VARCHAR` | Primer nombre del usuario. | `primerNombre` (String) |
| `apellido_paterno` | `VARCHAR` | Apellido paterno del usuario. | `apellidoPaterno` (String) |
| `apellido_materno` | `VARCHAR` | Apellido materno del usuario. | `apellidoMaterno` (String) |

* **Ejemplo de Ejecución en código:**
  ```sql
  SELECT * FROM comun.fn_login('admin');
  ```

---

## 📐 Convenciones de Nombres

* **Base de Datos:** Se utiliza `snake_case` para el esquema, nombres de tablas, columnas e índices.
* **Modelo en Código Java:** Se utiliza `camelCase` en los atributos de `UsuarioLogin`. El mapeo entre `snake_case` de PostgreSQL y `camelCase` se realiza manualmente dentro de `UsuarioRepository.java` usando un `RowMapper` personalizado.

---

> **Nota para IA:** El microservicio asume que no maneja migraciones de base de datos directas (`ddl-auto: none`). Si se añade alguna tabla nueva administrada directamente por este microservicio, se deberá crear una carpeta `migrations/` y documentar aquí las convenciones de migración.

---

## Última revisión
- **Fecha:** 2026-05-25
- **Commit:** `c646311c83eae3bf4759c7ea39bfde2726ff11c9`

---

## Instrucciones para actualizar este doc
- Si cambias el esquema de BD (nombres de tablas, columnas, restricciones o tipos de datos) → actualiza `DATABASE.md`.
- Si se modifica la firma o columnas retornadas por la función almacenada `comun.fn_login` → actualiza `DATABASE.md`.

[← Volver al índice](INDEX.md)
