[← Volver al índice](INDEX.md)

# 📜 Changelog - autenticacionWeb

Este documento registra cronológicamente todos los cambios significativos, nuevas funcionalidades y correcciones de errores realizados en el microservicio `autenticacionWeb`.

---

## [0.0.1-SNAPSHOT] - 2026-05-25

### Añadido
* **Seguridad y JWT:**
  - Implementación del filtro `JwtRequestFilter` y configuración de seguridad en `SecurityConfig` para autenticación sin estado.
  - Firma y verificación de tokens con el algoritmo HMAC-SHA256 (`JJWT` v0.12.6).
* **Persistencia:**
  - Repositorio `UsuarioRepository` con llamadas JDBC directas a la función de base de datos `comun.fn_login(?)`.
* **Restablecimiento de Contraseñas:**
  - Servicio `PasswordResetService` para la generación de tokens UUID temporales de 10 minutos y validación de los mismos en la tabla `comun.admin_usuario`.
  - Servicio de correos `EmailServiceImpl` interactuando con servidores SMTP de Gmail.
* **Interfaz de Usuario (Thymeleaf):**
  - Vistas HTML para `/login`, `/forgot-password`, `/reset-password` y `/dashboard` integradas con peticiones fetch asíncronas y guardado en `localStorage`.
* **CI/CD y Webhooks:**
  - Pipeline `.github/workflows/telegram-notify.yml` para notificar actualizaciones y solicitudes de revisiones de Pull Requests directamente en Telegram.
* **Observabilidad:**
  - Métricas de Prometheus expuestas en `/actuator/prometheus` e instrumentación básica con OpenTelemetry.

---

> **Nota para IA:** El registro de cambios se mantiene de forma manual. Cada vez que realices una actualización en la estructura o resuelvas elementos de deuda técnica, añade una línea aquí detallando la modificación.

---

## Última revisión
- **Fecha:** 2026-05-25
- **Commit:** `c646311c83eae3bf4759c7ea39bfde2726ff11c9`

---

## Instrucciones para actualizar este doc
- Cuando completes un cambio relevante en la base de código → añade una línea o subsección detallada en `CHANGELOG.md`.

[← Volver al índice](INDEX.md)
