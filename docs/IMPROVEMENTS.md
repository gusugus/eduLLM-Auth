[← Volver al índice](INDEX.md)

# 🚀 Technical Debt & Roadmap - autenticacionWeb

Este documento reúne las sugerencias de mejora arquitectónica, la deuda técnica identificada y el roadmap de desarrollo para optimizar y asegurar el microservicio de autenticación.

---

## ⚠️ Deuda Técnica y Errores Identificados

A continuación se detallan los hallazgos en el código actual que requieren atención a corto o mediano plazo:

| Componente | Hallazgo / Problema | Impacto | Solución Sugerida |
|---|---|---|---|
| **Dockerfile** | Declara `EXPOSE 8080` pero la aplicación corre internamente en el puerto `8081`. Genera confusión a nivel de red y despliegue. | 🔴 Cosmético / Medio | Modificar la instrucción en el `Dockerfile` a `EXPOSE 8081` para mantener la coherencia. |
| **Configuraciones** | Credenciales de base de datos, contraseñas SMTP y la clave secreta `jwt.secret` están expuestas en texto plano en `application.yml`. | 🔴 Alto (Seguridad) | Reemplazar los valores fijos por variables de entorno en producción (ej. `password: ${DB_PASSWORD}`). |
| **UsuarioRepository** | Uso del método deprecado `queryForObject(sql, Object[], RowMapper)` silenciado mediante `@SuppressWarnings("deprecation")`. | 🟡 Bajo | Reemplazar por la firma moderna basada en varargs: `queryForObject(sql, RowMapper, Object...)`. |
| **CustomUserDetailsService** | Bug en la línea de log de éxito: `log.warn("Login correcto: {} con idRol", usuario.getUsername(), usuario.getIdRol())`. Falta un marcador `{}` para el rol y usa nivel `WARN` para eventos normales. | 🟡 Bajo | Corregir a: `log.info("Login correcto: {} con idRol: {}", ...)` |
| **pom.xml** | `spring-boot-devtools` está en scope de compilación por defecto, lo que puede empaquetarlo en el JAR final de producción. | 🟡 Bajo | Añadir la etiqueta `<optional>true</optional>` para evitar overhead y recargas innecesarias en entornos de producción. |
| **SimpleCorsFilter** | Configuración de CORS sumamente abierta (`Access-Control-Allow-Origin: *`). | 🔴 Alto (Seguridad) | Restringir el origen únicamente a los dominios autorizados de la aplicación EduLLM. |

---

## 🗺️ Roadmap de Mejoras Recomendadas

### Fase 1: Saneamiento y Robustez (Corto Plazo)
* **Externalización Total de Secretos:** Implementar la lectura de credenciales mediante variables del sistema o integrar un gestor de secretos como HashiCorp Vault.
* **Corrección de Warnings y Logs:** Resolver las APIs deprecadas en el Repositorio y corregir el formateador del log de inicio de sesión exitoso.

### Fase 2: Robustez en Seguridad (Mediano Plazo)
* **JWT Asimétrico (RS256 / ES256):** Migrar de una firma simétrica (HMAC-SHA256) a firmas de clave pública/privada. Esto permitirá que los demás microservicios verifiquen los tokens utilizando únicamente la clave pública, eliminando el riesgo de comprometer la clave de firma si un microservicio es vulnerado.
* **Token de Refresco (Refresh Token):** Implementar tokens de refresco con menor tiempo de vida para los tokens de acceso, reduciendo el impacto en caso de robo de credenciales.

### Fase 3: Escalabilidad (Largo Plazo)
* **Migración a Spring WebFlux (Reactivo):** Si el tráfico del microservicio aumenta considerablemente, migrar el servidor Tomcat síncrono a Netty reactivo para procesar miles de peticiones concurrentes con menor consumo de memoria.

---

> **Nota para IA:** Estas mejoras deben priorizarse según el impacto en la seguridad del sistema. Las vulnerabilidades de secretos expuestos y CORS permisivos deben resolverse antes de subir a cualquier entorno productivo.

---

## Última revisión
- **Fecha:** 2026-05-25
- **Commit:** `c646311c83eae3bf4759c7ea39bfde2726ff11c9`

---

## Instrucciones para actualizar este doc
- Conforme se vayan resolviendo los puntos de deuda técnica o se completen fases del roadmap → actualiza `IMPROVEMENTS.md`.
- Si se detectan nuevos problemas o bugs de diseño durante las pruebas de carga o auditorías de código → actualiza `IMPROVEMENTS.md`.

[← Volver al índice](INDEX.md)
