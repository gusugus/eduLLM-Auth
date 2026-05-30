# Plan de Implementación - Generación de Documentación Completa

Este plan detalla el proceso para estructurar y escribir la suite de documentación técnica para el microservicio `autenticacionWeb`, siguiendo estrictamente las reglas descritas en `SKILL_DOCUMENTACION.md`.

La documentación actuará como la **única fuente de verdad** del proyecto y estará contenida en la carpeta `/docs` en la raíz del repositorio.

---

## Cambios Propuestos

Crearemos una nueva carpeta `/docs` y generaremos los 14 archivos Markdown especificados por la plantilla, adaptándolos a la arquitectura de Spring Boot 3 + JWT, las bases de datos PostgreSQL y las vistas Thymeleaf que posee el proyecto.

### Módulos a crear en `/docs`

#### [NEW] [docs/INDEX.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/INDEX.md)
Índice principal que enlaza a todos los demás archivos. Seguirá el formato estandarizado.

#### [NEW] [docs/README.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/README.md)
Vista general, propósito del microservicio de autenticación, requisitos previos y cómo levantarlo en entornos locales y Docker.

#### [NEW] [docs/ARCHITECTURE.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/ARCHITECTURE.md)
Diagrama de arquitectura del microservicio, descripción de los paquetes Java (controllers, config, services, repositories, dtos), diagrama de flujo del proceso de Login y de Recuperación/Restablecimiento de contraseña, y decisiones técnicas clave (stateless, Spring Security 6+, JJWT, OpenTelemetry/Prometheus).

#### [NEW] [docs/API.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/API.md)
Documentación de los endpoints HTTP expuestos (públicos y protegidos):
- `GET /api/auth/test`
- `POST /api/auth/login`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
Incluirá ejemplos de peticiones (`curl` / JSON) y respuestas (200 OK, 401 Unauthorized, 400 Bad Request).

#### [NEW] [docs/DATABASE.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/DATABASE.md)
Detalles de la base de datos PostgreSQL (`edu_llm`):
- Tabla `comun.admin_usuario` (columnas: `username`, `correo`, `password_hash`, `reset_token`, `reset_token_expiry`, etc.).
- Función PostgreSQL `comun.fn_login(?)` (propósito, parámetros, columnas de salida).
- Estilo y convenciones de BD.

#### [NEW] [docs/SERVICES.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/SERVICES.md)
Servicios de aplicación del microservicio:
- `CustomUserDetailsService`: Validación de credenciales.
- `PasswordResetService`: Generación y verificación de tokens UUID de reinicio de clave.
- `EmailService` / `EmailServiceImpl`: Envío de correos de restablecimiento con SMTP (Gmail).
- Observabilidad y monitoreo (Prometheus, OpenTelemetry Collector via Jaeger/Zipkin).

#### [NEW] [docs/INTEGRATIONS.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/INTEGRATIONS.md)
Relación con otros microservicios de EduLLM (como emisor de identidad centralizado) y webhook/integraciones de CI/CD:
- Flujo de PR a través de Telegram (`.github/workflows/telegram-notify.yml`).
- Servidores SMTP de Gmail para envío de correos.

#### [NEW] [docs/SECURITY.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/SECURITY.md)
Mecanismos de seguridad implementados:
- Configuración de filtros (Spring Security, `JwtRequestFilter` y `SimpleCorsFilter`).
- Algoritmo de hash de contraseñas (BCrypt).
- Firmas JWT (HMAC-SHA256).
- Vulnerabilidades/Riesgos identificados (secretos hardcodeados en configuraciones).

#### [NEW] [docs/VIEWS.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/VIEWS.md)
Descripción de la UI web integrada con Thymeleaf:
- Pantalla de Login (`/login`)
- Pantalla de Recuperación (`/forgot-password`)
- Pantalla de Nueva Contraseña (`/reset-password`)
- Dashboard de prueba (`/dashboard`)
Se detallará el comportamiento interactivo (fetch al backend, localStorage de tokens).

#### [NEW] [docs/IMPROVEMENTS.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/IMPROVEMENTS.md)
Roadmap de mejoras sugeridas y deuda técnica:
- Desalineación del puerto expuesto en `Dockerfile` (8080) contra `application.yml` (8081).
- Externalización de secretos (contraseña de DB y SMTP, clave secreta JWT) a variables de entorno.
- Warn por uso de API obsoleta `queryForObject(..., Object[], RowMapper)` en JDBC.
- Error en el logger de login correcto (placeholder faltante `{}`).
- Pasar `spring-boot-devtools` a scope runtime.

#### [NEW] [docs/CONTRIBUTING.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/CONTRIBUTING.md)
Guía de contribución para desarrolladores: pasos para probar cambios locales, compilar usando Maven (`mvn clean package`) y levantar contenedores Docker.

#### [NEW] [docs/GLOSSARY.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/GLOSSARY.md)
Glosario con conceptos clave: JWT, Claims, Subject, Thymeleaf, BCrypt, OTel (OpenTelemetry), Spring Boot, etc.

#### [NEW] [docs/DEPENDENCIES.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/DEPENDENCIES.md)
Resumen estructurado de las dependencias definidas en `pom.xml`, agrupadas por propósito (seguridad, BD, mensajería, observabilidad, etc.) con sus versiones correspondientes.

#### [NEW] [docs/CHANGELOG.md](file:///home/gusgus/eclipse-workspace/autenticacionWeb/docs/CHANGELOG.md)
Historial manual de versiones y cambios del microservicio.

---

## Plan de Verificación

### Pruebas Automatizadas e Integridad
- Asegurar que todos los enlaces inter-documentación en los archivos Markdown funcionan correctamente (es decir, apuntan a las rutas relativas adecuadas).
- Validar que todos los archivos contienen el bloque de navegación `[← Volver al índice](INDEX.md)` al inicio y al final.
- Confirmar que todos los archivos finalizan con la sección obligatoria "Última revisión" (con la fecha de hoy y el hash del commit actual) y las "Instrucciones para actualizar este doc".
- Verificar la consistencia técnica de la documentación contrastando con los archivos reales del repositorio.
