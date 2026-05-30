[← Volver al índice](INDEX.md)

# 🔒 Security Architecture - autenticacionWeb

Este documento detalla la infraestructura de seguridad del microservicio, incluyendo la configuración de filtros, algoritmos de hashing, generación de tokens y vulnerabilidades conocidas.

---

## 1. Configuración de Seguridad (Spring Security)

El microservicio utiliza una arquitectura de seguridad completamente **stateless** (sin estado) administrada por Spring Security.

* **CSRF (Cross-Site Request Forgery):** Deshabilitado, debido a que el servicio no utiliza cookies de sesión y confía únicamente en tokens JWT enviados en las cabeceras.
* **Política de Sesión:** `SessionCreationPolicy.STATELESS` (el servidor no mantiene registros de sesión en memoria).
* **Endpoints Públicos (Excluidos de Autenticación):**
  - `/api/auth/login`, `/api/auth/forgot-password`, `/api/auth/reset-password` (REST APIs)
  - `/login`, `/forgot-password`, `/reset-password`, `/dashboard` (Vistas Thymeleaf)
  - `/css/**`, `/js/**` (Recursos estáticos)
* **Endpoints Protegidos:** Cualquier ruta no listada arriba requiere un token JWT válido.

---

## 2. Flujo del Filtro de Seguridad JWT (`JwtRequestFilter`)

Cada petición que requiere autenticación pasa a través de la cadena de filtros en el siguiente orden:

```
[Cliente] 
   │
   ▼
[SimpleCorsFilter] (Añade cabeceras CORS)
   │
   ▼
[JwtRequestFilter] 
   ├─ 1. Lee cabecera 'Authorization'
   ├─ 2. ¿No empieza por 'Bearer '? → Corta y retorna HTTP 401 "Token requerido"
   ├─ 3. Extrae JWT y parsea con JwtUtil
   │     ├─ ¿Token Expirado? → Corta y retorna HTTP 401 "Token expirado"
   │     └─ ¿Token Malformado? → Corta y retorna HTTP 401 "Token inválido"
   ├─ 4. Carga UserDetails desde BD
   ├─ 5. Valida coincidencia de usuario y expiración
   └─ 6. Establece el contexto de seguridad en SecurityContextHolder
   │
   ▼
[AuthController / API Endpoint]
```

---

## 3. Cifrado y Firmas Digitales

### Encriptación de Contraseñas (BCrypt)
Las contraseñas de los usuarios nunca se guardan en texto plano. Se utiliza el algoritmo **BCrypt** (a través de `BCryptPasswordEncoder`) tanto para:
1. Validar el inicio de sesión (`DaoAuthenticationProvider` compara el password ingresado contra el hash).
2. Generar el hash de la nueva contraseña al realizar el restablecimiento.

### Firmas del Token JWT
* **Algoritmo:** HMAC-SHA256 (`HS256`).
* **Firma:** Se genera a partir de la propiedad `jwt.secret` (mínimo 32 caracteres / 256 bits).
* **Claims Estándar y Personalizados en el Token:**
  - `sub` (Subject): Nombre de usuario.
  - `idUsuario`: ID único del usuario en PostgreSQL.
  - `rol`: Rol del usuario (ejemplo: `ROLE_1`).
  - `iat` (Issued At): Fecha de emisión.
  - `exp` (Expiration): Fecha de expiración (24 horas por defecto).

---

## 4. Riesgos de Seguridad y Mitigaciones

| Riesgo / Vulnerabilidad | Impacto | Estado | Mitigación |
|---|---|---|---|
| **Secretos Hardcodeados** | Alto. Si el código se expone públicamente, la clave de firma JWT y las claves de acceso SMTP / Base de datos quedan expuestas. | ⚠️ Pendiente | Cambiar `application.yml` para leer los secretos desde variables de entorno de producción (ej: `${JWT_SECRET}`). |
| **CORS muy permisivo (`*`)** | Medio. Permite peticiones desde cualquier origen del navegador. | ⚠️ Pendiente | Restringir `Access-Control-Allow-Origin` a los dominios específicos de la aplicación EduLLM. |
| **Exposición del Token en localStorage** | Bajo/Medio. La vista web guarda el token en `localStorage`, que es vulnerable a ataques XSS si se inyecta JS de terceros. | ℹ️ Informativo | Utilizar mecanismos de protección de contenido (CSP) en las páginas web y asegurar cookies HttpOnly para mayor seguridad si el cliente lo permite. |

---

> **Nota para IA:** Si se detectan dependencias obsoletas en el archivo `pom.xml` con vulnerabilidades reportadas (CVEs), este archivo debe alertar sobre la necesidad de actualizar las versiones (por ejemplo, actualizando `jjwt-api`).

---

## Última revisión
- **Fecha:** 2026-05-25
- **Commit:** `c646311c83eae3bf4759c7ea39bfde2726ff11c9`

---

## Instrucciones para actualizar este doc
- Si cambias los filtros de seguridad, agregas OAuth2 o cambias a SSL/HTTPS → actualiza `SECURITY.md`.
- Si se modifican los algoritmos de hash (BCrypt por Argon2) o el firmado del JWT → actualiza `SECURITY.md`.

[← Volver al índice](INDEX.md)
