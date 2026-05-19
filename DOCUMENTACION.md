# autenticacionWeb — Microservicio de Autenticación JWT

## 1. ¿Qué es este proyecto?

**`autenticacionWeb`** es un **microservicio Spring Boot** cuya única responsabilidad es **autenticar usuarios contra una base de datos PostgreSQL y emitir JWT (JSON Web Tokens)**. Es el punto de entrada de identidad de la plataforma MindBuzz/EduLLM.

- No gestiona sesiones HTTP (es completamente **stateless**).
- No maneja lógica de negocio (materias, quizzes, partidas, etc.).
- Cualquier otro microservicio que necesite verificar identidad consume el JWT que este MS emite.

---

## 2. Stack tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 17 |
| Framework | Spring Boot | 3.4.4 |
| Seguridad | Spring Security | (heredada del parent) |
| JWT | JJWT (io.jsonwebtoken) | 0.12.6 |
| Base de datos | PostgreSQL (via Spring JDBC) | — |
| Boilerplate | Lombok | — |
| Dev tools | Spring Boot DevTools | — |
| Empaquetado | Maven (spring-boot-maven-plugin) | — |
| Runtime Docker | Amazon Corretto 17 Alpine | — |

---

## 3. Estructura de archivos

```
autenticacionWeb/
├── config/
│   └── application.yml              ← Config externa (usada por Docker)
├── src/
│   └── main/
│       ├── java/
│       │   ├── main/
│       │   │   └── Application.java         ← Entry point Spring Boot
│       │   ├── autenticacionWeb/
│       │   │   └── JwtUtil.java             ← Generación/validación de tokens
│       │   ├── config/
│       │   │   ├── SecurityConfig.java      ← Reglas de seguridad HTTP
│       │   │   └── JwtRequestFilter.java    ← Filtro JWT en cada request
│       │   ├── controller/
│       │   │   └── AuthController.java      ← Endpoints REST públicos
│       │   ├── services/
│       │   │   └── CustomUserDetailsService.java ← Carga usuario desde DB
│       │   ├── repositorio/
│       │   │   └── UsuarioRepository.java   ← JDBC → fn_login PostgreSQL
│       │   └── dto/
│       │       ├── AuthenticationRequest.java  ← Body del POST /login
│       │       ├── AuthenticationResponse.java ← Respuesta con token
│       │       └── UsuarioLogin.java           ← Modelo interno del usuario
│       └── resources/
│           └── application.yml              ← Config embebida (desarrollo)
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

---

## 4. APIs expuestas

El servidor corre en el **puerto 8081** (configurable en `application.yml`).

### 4.1 `GET /api/auth/test`
**Propósito:** Healthcheck — verificar que el servicio está levantado.  
**Auth requerida:** ❌ Ninguna (permitAll)  
**Response:** `200 OK` → body `"OK"` (texto plano)

---

### 4.2 `POST /api/auth/login`
**Propósito:** Autenticar credenciales y obtener un JWT.  
**Auth requerida:** ❌ Ninguna (permitAll)  
**Content-Type:** `application/json`

**Request body:**
```json
{
  "username": "string",
  "password": "string (texto plano, se compara contra BCrypt)"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response `401 Unauthorized`:** Credenciales incorrectas (lanzado automáticamente por Spring Security).

**Flujo interno:**
1. `AuthController` recibe el body y llama a `AuthenticationManager.authenticate()`
2. `AuthenticationManager` delega a `DaoAuthenticationProvider`
3. `DaoAuthenticationProvider` llama a `CustomUserDetailsService.loadUserByUsername(username)`
4. `CustomUserDetailsService` invoca `UsuarioRepository.obtenerUsuarioPorUsername(username)`
5. El repositorio ejecuta `SELECT * FROM comun.fn_login(?)` en PostgreSQL
6. El `DaoAuthenticationProvider` valida el `password` en texto plano contra el `passwordHash` con **BCrypt**
7. Si válido → `JwtUtil.generateToken(userDetails)` genera el JWT firmado con HS256
8. Se retorna `{ "token": "..." }`

---

### 4.3 Endpoints protegidos (cualquier otra ruta)
**Auth requerida:** ✅ Header `Authorization: Bearer <token>`  
Actualmente no hay más endpoints implementados, pero cualquier ruta fuera de `/api/auth/**` requiere JWT válido por configuración de Spring Security.

---

## 5. Seguridad — Flujo del JWT Filter

Para **cada request** que NO sea `/api/auth/**`, el filtro `JwtRequestFilter` hace:

```
Request → JwtRequestFilter.doFilterInternal()
  1. Lee header "Authorization"
  2. Extrae el token (substring después de "Bearer ")
  3. Extrae username del token via JwtUtil.extractUsername()
     - Si token expirado → HTTP 401 "Token expirado" (corta la cadena)
     - Si token malformado → HTTP 401 "Token inválido" (corta la cadena)
  4. Si username extraído y SecurityContext vacío:
     a. Carga UserDetails desde CustomUserDetailsService
     b. Valida token (username coincide y no expirado)
     c. Registra autenticación en SecurityContextHolder
  5. chain.doFilter() → continúa con el resto de la cadena
```

---

## 6. Capa de datos — Base de datos PostgreSQL

### Función invocada

```sql
SELECT * FROM comun.fn_login(username TEXT)
```

**Columnas que retorna (mapeadas a `UsuarioLogin`):**

| Columna DB | Campo Java | Tipo |
|---|---|---|
| `id_usuario` | `idUsuario` | `Integer` |
| — (param) | `username` | `String` |
| `password_hash` | `passwordHash` | `String` (BCrypt) |
| `id_rol` | `idRol` | `Integer` |
| `primer_nombre` | `primerNombre` | `String` |
| `apellido_paterno` | `apellidoPaterno` | `String` |
| `apellido_materno` | `apellidoMaterno` | `String` |

> ⚠️ Si `fn_login` retorna 0 filas (usuario no existe), `UsuarioRepository` retorna `null` y `CustomUserDetailsService` lanza `UsernameNotFoundException`.

### Rol asignado
El rol del JWT se construye como `"ROLE_" + idRol` (entero). Ejemplo: si `idRol = 2`, la authority es `ROLE_2`.

---

## 7. Configuración

### Prioridad de configuración
Docker monta `./config/application.yml` en `/app/config/application.yml`, que sobreescribe el `src/main/resources/application.yml` embebido en el JAR.

| Propiedad | Valor por defecto | Descripción |
|---|---|---|
| `server.port` | `8081` | Puerto HTTP del servicio |
| `jwt.secret` | `"404E635266..."` (64 chars hex) | Clave HMAC-SHA256. **Cambiar en producción.** |
| `jwt.expiration` | `86400000` | Vida del token en ms (24 horas) |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/edu_llm` | URL PostgreSQL |
| `spring.datasource.username` | `admin` | Usuario DB |
| `spring.datasource.password` | `admin` | Password DB. **Cambiar en producción.** |
| `spring.jpa.hibernate.ddl-auto` | `none` | No modifica el esquema DB |
| `logging.file.name` | `logs/auth.log` | Archivo de log rotativo |

---

## 8. Docker

### Dockerfile
```dockerfile
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY ./target/autenticacionWeb-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080         # ← Nota: expone 8080 pero la app corre en 8081 por config
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> ⚠️ **Inconsistencia conocida:** El `Dockerfile` declara `EXPOSE 8080` pero `application.yml` configura `server.port: 8081`. Docker solo usa `EXPOSE` como documentación, la app efectivamente escucha en **8081**.

### docker-compose.yml
```yaml
services:
  autenticacion-web:
    build: .
    container_name: auth-ms
    network_mode: host          # Usa la red del host (acceso directo a localhost:5432)
    mem_limit: 512m
    volumes:
      - ./config/application.yml:/app/config/application.yml
      - ./logs:/app/logs
    restart: unless-stopped
```

`network_mode: host` permite que el contenedor acceda a PostgreSQL corriendo en `localhost:5432` del host sin necesidad de configurar redes Docker adicionales.

---

## 9. Guía de modificación

### ¿Dónde cambio el puerto?
→ `config/application.yml` → `server.port`

### ¿Dónde cambio la clave secreta JWT?
→ `config/application.yml` → `jwt.secret` (mínimo 32 caracteres / 256 bits)

### ¿Dónde cambio la expiración del token?
→ `config/application.yml` → `jwt.expiration` (milisegundos)

### ¿Dónde cambio la conexión a la base de datos?
→ `config/application.yml` → `spring.datasource.*`

### ¿Dónde agrego claims extras al JWT (e.g. idRol, nombre)?
→ `controller/AuthController.java` — en lugar de `jwtUtil.generateToken(userDetails)`, usar:
```java
Map<String, Object> claims = new HashMap<>();
claims.put("idRol", /* obtener del userDetails o del Authentication */);
final String jwt = jwtUtil.generateToken(userDetails, claims);
```
→ `autenticacionWeb/JwtUtil.java` → el método `generateToken(UserDetails, Map)` ya existe.

### ¿Dónde agrego un nuevo endpoint REST?
→ Crear clase en `controller/` con `@RestController`. Recordar agregar el nuevo package a `@SpringBootApplication(scanBasePackages = {...})` en `main/Application.java` si se crea un nuevo package.

### ¿Dónde agrego lógica de validación de usuarios adicional?
→ `services/CustomUserDetailsService.java` → método `loadUserByUsername()`

### ¿Dónde cambio la función PostgreSQL que valida credenciales?
→ `repositorio/UsuarioRepository.java` → línea con `SELECT * FROM comun.fn_login(?)`

### ¿Dónde agrego nuevos campos del usuario al token o a la respuesta?
1. Agregar campo a `dto/UsuarioLogin.java`
2. Mapear columna en `repositorio/UsuarioRepository.java` (bloque `rs.getString(...)`)
3. Incluir en claims del token en `controller/AuthController.java`

---

## 10. Dependencias Maven (pom.xml)

| Artefacto | Scope | Propósito |
|---|---|---|
| `spring-boot-starter-web` | compile | Servidor HTTP / REST (Tomcat embebido) |
| `spring-boot-starter-security` | compile | Filtros de seguridad, AuthenticationManager |
| `spring-boot-starter-jdbc` | compile | JdbcTemplate para queries JDBC |
| `postgresql` | runtime | Driver JDBC PostgreSQL |
| `jjwt-api` | compile | API de JJWT para firmar/parsear tokens |
| `jjwt-impl` | runtime | Implementación JJWT |
| `jjwt-jackson` | runtime | Serialización JSON de claims JWT |
| `lombok` | optional | Generación de getters/setters via `@Data` |
| `spring-boot-devtools` | compile | Hot reload en desarrollo |

---

## 11. Diagrama de flujo — Login exitoso

```
Cliente
  │
  ├─ POST /api/auth/login  { username, password }
  │
  ▼
AuthController.createAuthenticationToken()
  │
  ├─ AuthenticationManager.authenticate()
  │     │
  │     └─ DaoAuthenticationProvider
  │           │
  │           ├─ CustomUserDetailsService.loadUserByUsername(username)
  │           │     │
  │           │     └─ UsuarioRepository.obtenerUsuarioPorUsername(username)
  │           │           │
  │           │           └─ PostgreSQL: SELECT * FROM comun.fn_login(?)
  │           │
  │           └─ BCryptPasswordEncoder.matches(rawPassword, hash)
  │
  ├─ JwtUtil.generateToken(userDetails)
  │     └─ HMAC-SHA256 firmado, expira en 24h
  │
  └─ ResponseEntity.ok({ "token": "eyJ..." })
```

---

## 12. Notas de producción / issues conocidos

> [!WARNING]
> El `jwt.secret` y las credenciales de DB están hardcodeadas en `application.yml`. En producción, reemplazar con variables de entorno:
> ```yaml
> jwt:
>   secret: ${JWT_SECRET}
> spring:
>   datasource:
>     password: ${DB_PASSWORD}
> ```

> [!WARNING]
> `EXPOSE 8080` en el Dockerfile no coincide con `server.port: 8081`. Actualizar el Dockerfile a `EXPOSE 8081` para coherencia.

> [!NOTE]
> `spring-boot-devtools` está en scope `compile` (debería ser `runtime` o excluirse en producción para evitar overhead de recarga automática).

> [!NOTE]
> `@SuppressWarnings("deprecation")` en `UsuarioRepository` indica uso de la API deprecated `queryForObject(sql, Object[], RowMapper)`. Migrar a `queryForObject(sql, RowMapper, Object...)` para limpiar warnings.

> [!NOTE]
> El log de login exitoso en `CustomUserDetailsService` tiene un bug: `log.warn("Login correcto: {} con idRol", usuario.getUsername(), usuario.getIdRol())` — solo imprime el username, le falta el segundo placeholder `{}` para el rol.
