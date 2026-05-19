# 🔐 autenticacionWeb — Auth Microservice

Microservicio **Spring Boot 3 + JWT** que centraliza la autenticación de usuarios para la plataforma **EduLLM / MindBuzz**. Valida credenciales contra PostgreSQL y emite tokens firmados con HMAC-SHA256.

> Para documentación técnica detallada ver [`DOCUMENTACION.md`](./DOCUMENTACION.md)

---

## Stack

| | |
|---|---|
| **Lenguaje** | Java 17 |
| **Framework** | Spring Boot 3.4.4 |
| **Seguridad** | Spring Security + JJWT 0.12.6 |
| **Base de datos** | PostgreSQL (via JdbcTemplate) |
| **Puerto** | `8081` |

---

## APIs expuestas

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| `GET` | `/api/auth/test` | ❌ | Healthcheck |
| `POST` | `/api/auth/login` | ❌ | Login → retorna JWT |
| `*` | cualquier otra ruta | ✅ Bearer | Requiere token válido |

### Login — ejemplo

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "mi_password"}'
```

**Respuesta:**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

### Consumir un endpoint protegido

```bash
curl http://localhost:8081/alguna/ruta \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## Levantar localmente

### Prerrequisitos
- Java 17+
- Maven 3.8+
- PostgreSQL corriendo en `localhost:5432` con la DB `edu_llm`

```bash
# Compilar
mvn clean package -DskipTests

# Ejecutar
java -jar target/autenticacionWeb-0.0.1-SNAPSHOT.jar \
     --spring.config.location=config/application.yml
```

### Con Docker

```bash
# Build + run
docker compose up --build

# Solo correr (si el JAR ya existe)
docker compose up
```

---

## Configuración rápida

Editar `config/application.yml` (este archivo es montado en Docker y sobreescribe el embebido):

```yaml
server:
  port: 8081

jwt:
  secret: "CAMBIAR_EN_PRODUCCION_min32chars"   # ← ⚠️ cambiar
  expiration: 86400000                          # 24h en ms

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/edu_llm
    username: admin
    password: admin                             # ← ⚠️ cambiar
```

---

## Estructura del proyecto

```
src/main/java/
├── main/           → Application.java (entry point)
├── autenticacionWeb/ → JwtUtil.java (firmar/validar tokens)
├── config/         → SecurityConfig.java, JwtRequestFilter.java
├── controller/     → AuthController.java (endpoints REST)
├── services/       → CustomUserDetailsService.java
├── repositorio/    → UsuarioRepository.java → comun.fn_login()
└── dto/            → AuthenticationRequest/Response, UsuarioLogin
```

---

## Base de datos

El login consulta la función PostgreSQL:

```sql
SELECT * FROM comun.fn_login(:username)
-- Retorna: id_usuario, password_hash, id_rol, primer_nombre, apellido_paterno, apellido_materno
```

Las contraseñas se validan con **BCrypt**.

---

## Notas importantes

- ⚠️ El `Dockerfile` declara `EXPOSE 8080` pero la app corre en **8081** — inconsistencia cosmética.
- ⚠️ Credenciales y secret JWT hardcodeados en `application.yml` → usar variables de entorno en producción.
- El token JWT expira en **24 horas** por defecto.
- Logs en `logs/auth.log` (rotación: 10MB, 30 días).
