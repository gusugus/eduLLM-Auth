[← Volver al índice](INDEX.md)

# 📖 Glossary of Terms - autenticacionWeb

Este documento define la terminología clave y conceptos técnicos utilizados dentro de la base de código y arquitectura del microservicio de autenticación.

---

## 📖 Glosario de Términos

| Término | Definición |
|---|---|
| **JWT (JSON Web Token)** | Estándar abierto (RFC 7519) que define una forma compacta y autónoma para transmitir información de manera segura entre partes como un objeto JSON. |
| **HMAC-SHA256 (HS256)** | Algoritmo de firma digital simétrico de clave única que garantiza que el payload de un token JWT no sea alterado durante la transmisión. |
| **Claims** | Atributos de metadatos o declaraciones almacenadas dentro del payload de un token JWT (ej. `idUsuario`, `rol`, `sub`). |
| **Subject (`sub`)** | Identificador principal del usuario dentro de un JWT (en nuestro caso, el `username` del usuario autenticado). |
| **BCrypt** | Algoritmo de hashing adaptativo e irreversible diseñado específicamente para el almacenamiento seguro de contraseñas, que incorpora un factor de costo ("work factor") para ralentizar ataques de fuerza bruta. |
| **Thymeleaf** | Motor de plantillas Java del lado del servidor que permite renderizar y poblar dinámicamente archivos HTML en el navegador. |
| **Stateless (Sin Estado)** | Estilo de arquitectura de red donde el servidor no almacena información de sesión del cliente en memoria. Cada request debe contener toda la información necesaria para procesarse. |
| **Spring Security** | Framework de autenticación y control de acceso robusto y altamente personalizable para aplicaciones Spring. |
| **Filter Chain (Cadena de Filtros)** | Secuencia de filtros interceptores en Spring Security que examinan y modifican las peticiones HTTP antes de que lleguen a los controladores. |
| **CORS (Cross-Origin Resource Sharing)** | Mecanismo de seguridad implementado en navegadores web que permite o restringe el acceso de scripts de un origen web a recursos localizados en otro origen diferente. |
| **OpenTelemetry (OTel)** | Colección de herramientas, APIs y SDKs diseñados para generar, recolectar y exportar datos de telemetría (métricas, logs y trazas distribuidas). |
| **Actuator** | Subproyecto de Spring Boot que expone endpoints HTTP dedicados al monitoreo y la administración de la aplicación en producción. |
| **JdbcTemplate** | Clase del framework Spring que simplifica el uso de JDBC, encargándose del control de excepciones, apertura y cierre de conexiones a base de datos. |

---

> **Nota para IA:** El glosario ayuda a estandarizar el vocabulario de nuevos integrantes. Si se agregan conceptos avanzados (ej. tokens RSA o OAuth2), las definiciones pertinentes deben añadirse a esta lista.

---

## Última revisión
- **Fecha:** 2026-05-25
- **Commit:** `c646311c83eae3bf4759c7ea39bfde2726ff11c9`

---

## Instrucciones para actualizar este doc
- Si se introducen nuevos frameworks, librerías complejas o protocolos de seguridad → actualiza `GLOSSARY.md`.

[← Volver al índice](INDEX.md)
