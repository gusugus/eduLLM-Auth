[← Volver al índice](INDEX.md)

# 📦 Dependencies - autenticacionWeb

Este documento reúne todas las librerías y dependencias externas del proyecto declaradas en `pom.xml`, agrupadas por categoría y explicando su propósito en la arquitectura.

---

## 📋 Listado de Dependencias (Maven)

El proyecto utiliza **Java 17** y hereda la configuración del padre `spring-boot-starter-parent` versión **`3.4.4`**.

### 1. Núcleo Web y Framework

| Dependencia | Scope | Versión | Propósito |
|---|---|---|---|
| `spring-boot-starter-web` | compile | *Heredada* | Proporciona el servidor Tomcat embebido y soporte para APIs REST. |
| `spring-boot-starter-thymeleaf` | compile | *Heredada* | Motor de vistas HTML en el servidor para páginas web (/login, /dashboard). |
| `spring-boot-devtools` | compile | *Heredada* | Recarga en caliente (Hot Reload) y herramientas de desarrollo local. |

### 2. Seguridad y JWT

| Dependencia | Scope | Versión | Propósito |
|---|---|---|---|
| `spring-boot-starter-security` | compile | *Heredada* | Filtros de control de acceso, manejo de contextos y BCrypt. |
| `spring-boot-starter-oauth2-client` | compile | *Heredada* | Soporte para inicio de sesión a través de proveedores OAuth2 externos. |
| `jjwt-api` | compile | `0.12.6` | Interfaz (API) para construcción y firma de tokens JWT. |
| `jjwt-impl` | runtime | `0.12.6` | Implementación en tiempo de ejecución del generador de tokens JJWT. |
| `jjwt-jackson` | runtime | `0.12.6` | Adaptador para serializar claims JWT a JSON utilizando Jackson. |

### 3. Persistencia y PostgreSQL

| Dependencia | Scope | Versión | Propósito |
|---|---|---|---|
| `spring-boot-starter-jdbc` | compile | *Heredada* | Soporte para ejecutar consultas SQL directas con `JdbcTemplate`. |
| `postgresql` | runtime | *Heredada* | Driver JDBC para comunicación directa con PostgreSQL. |

### 4. Servicios y Utilidades

| Dependencia | Scope | Versión | Propósito |
|---|---|---|---|
| `spring-boot-starter-mail` | compile | *Heredada* | Proporciona `JavaMailSender` para envío de correos SMTP. |
| `spring-boot-starter-validation` | compile | *Heredada* | Validación automática de campos y anotaciones en DTOs (ej: `@NotNull`). |
| `lombok` | optional | *Heredada* | Generación automática de boilerplate (getters, setters, constructores y loggers) en tiempo de compilación. |

### 5. Monitoreo y Observabilidad

| Dependencia | Scope | Versión | Propósito |
|---|---|---|---|
| `micrometer-tracing-bridge-otel` | compile | *Heredada* | Puente que conecta Micrometer con OpenTelemetry para trazas. |
| `opentelemetry-exporter-otlp` | compile | *Heredada* | Exportador de telemetría mediante protocolo estándar OTLP. |
| `micrometer-registry-prometheus` | compile | *Heredada* | Formatea métricas internas en formato entendible por Prometheus. |

---

> **Nota para IA:** Al actualizar de versión el parent de Spring Boot, comprueba si las versiones externas como `jjwt-api` siguen siendo compatibles o si requieren actualización manual en `pom.xml`.

---

## Última revisión
- **Fecha:** 2026-05-25
- **Commit:** `c646311c83eae3bf4759c7ea39bfde2726ff11c9`

---

## Instrucciones para actualizar este doc
- Si añades, actualizas o eliminas dependencias en el archivo `pom.xml` → actualiza `DEPENDENCIES.md`.

[← Volver al índice](INDEX.md)
