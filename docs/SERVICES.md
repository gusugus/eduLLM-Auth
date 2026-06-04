[← Volver al índice](INDEX.md)

# ⚙️ Internal Services - autenticacionWeb

Este documento describe la lógica de los servicios internos de la aplicación, su configuración y la pila de instrumentación y observabilidad del sistema.

---

## 📋 Catálogo de Servicios Internos

El microservicio no contiene tareas programadas (cron jobs), colas de mensajería asíncronas ni workers en segundo plano. Toda la lógica se ejecuta de forma síncrona mediante los siguientes servicios:

### 1. `CustomUserDetailsService`
* **Clase:** `services.CustomUserDetailsService`
* **Interfaz:** `org.springframework.security.core.userdetails.UserDetailsService`
* **Propósito:** Recuperar la información de identidad del usuario durante el flujo de inicio de sesión.
* **Detalle de Lógica:**
  - Llama a `UsuarioRepository.obtenerUsuarioPorUsername(username)`.
  - Mapea el rol numérico de la base de datos como una autoridad de Spring Security con el prefijo `"ROLE_"`.
  - Retorna un objeto `CustomUserDetails` que extiende de `User` de Spring Security, incorporando el ID de usuario (`idUsuario`) y el rol formateado (`rol`).

### 2. `PasswordResetService`
* **Clase:** `services.PasswordResetService`
* **Propósito:** Orquestar el flujo de recuperación y actualización de contraseñas.
* **Detalle de Lógica:**
  - **Generación de Token (`createPasswordResetToken`):**
    1. Verifica si el usuario existe en `comun.admin_usuario`.
    2. Genera un UUID v4 único y calcula el tiempo de expiración (por defecto 10 minutos).
    3. Registra el token y la fecha de expiración en base de datos.
    4. Invoca a `EmailService` para el envío del correo electrónico.
  - **Reinicio de Contraseña (`resetPassword`):**
    1. Busca al usuario asociado al token en base de datos.
    2. Valida que la fecha de expiración no haya pasado.
    3. Codifica la nueva contraseña utilizando `BCryptPasswordEncoder`.
    4. Actualiza la tabla limpiando el token e ingresando la nueva clave.

### 3. `EmailServiceImpl`
* **Clase:** `services.EmailServiceImpl`
* **Interfaz:** `services.EmailService`
* **Propósito:** Envío físico de correos de restablecimiento de contraseña mediante protocolo SMTP.
* **Detalle de Lógica:**
  - Construye el cuerpo del mensaje de correo en base a plantillas de texto plano.
  - Genera el enlace de restablecimiento dinámicamente usando las propiedades inyectadas de host, puerto y protocolo.
  - Envía el correo de manera síncrona utilizando `JavaMailSender`.

---

## ⚙️ Parámetros de Configuración de Servicios

Los servicios se configuran a través del archivo `application.yml` mediante las siguientes propiedades clave:

| Propiedad | Valor por Defecto | Descripción |
|---|---|---|
| `app.reset-token-expiration-minutes` | `10` | Tiempo de vida límite del token de recuperación en minutos. |
| `app.base-url` | `localhost` | Host para construir el enlace de restablecimiento de contraseña. |
| `app.protocol` | `http:///` | Protocolo HTTP o HTTPS del enlace de restablecimiento. |
| `app.port` | `8081` | Puerto utilizado para el enlace de restablecimiento. |
| `spring.mail.host` | `smtp.gmail.com` | Host del servidor SMTP para el envío de correos. |
| `spring.mail.port` | `587` | Puerto del servidor SMTP (por defecto TLS/STARTTLS). |

---

## 📊 Observabilidad y Monitoreo

El microservicio incluye soporte nativo para telemetría y recolección de métricas a través del paquete **Actuator**, **Micrometer** y **OpenTelemetry**:

* **Métricas Prometheus:** Expone métricas de rendimiento de la JVM, peticiones HTTP y pool de conexiones Hikari en el endpoint público `/actuator/prometheus`.
* **Exportador OTel:** Envía trazas y métricas distribuidas hacia un recolector OpenTelemetry (como Jaeger o Zipkin) configurado en las variables:
  - Trazas: `http://localhost:4318/v1/traces`
  - Métricas OTLP: `http://localhost:4318/v1/metrics`
* **Muestreo de Trazas:** Configurado con probabilidad `1.0` (captura el 100% de las solicitudes para análisis).

---

> **Nota para IA:** Este servicio depende de `JavaMailSender` y de la disponibilidad de un servidor SMTP. Si se agregan colas de mensajería (como RabbitMQ o Kafka) para asincronía del correo en el futuro, deberán documentarse aquí las colas y consumidores.

---

## Última revisión
- **Fecha:** 2026-05-25
- **Commit:** `c646311c83eae3bf4759c7ea39bfde2726ff11c9`

---

## Instrucciones para actualizar este doc
- Si se agregan nuevos servicios internos de lógica de negocio o de infraestructura → actualiza `SERVICES.md`.
- Si se integra una cola de mensajería, worker asíncrono o tarea programada → actualiza `SERVICES.md`.

[← Volver al índice](INDEX.md)
