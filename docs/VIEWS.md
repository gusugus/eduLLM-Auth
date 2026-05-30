[← Volver al índice](INDEX.md)

# 🖥️ User Interface Views - autenticacionWeb

Este documento describe las vistas web interactivas implementadas en el microservicio utilizando plantillas **Thymeleaf**, sus rutas y el comportamiento de la lógica frontend en Javascript.

Todas las plantillas HTML se localizan en la ruta `src/main/resources/templates/`.

---

## 🗺️ Mapa de Rutas de Vistas

| Ruta HTTP | Plantilla HTML | Controlador | Acceso | Descripción |
|---|---|---|---|---|
| `/login` | `login.html` | `ViewController.loginPage` | Público | Pantalla de inicio de sesión. |
| `/forgot-password` | `forgot-password.html` | `ViewController.showForgotPasswordForm` | Público | Formulario para solicitar recuperación de contraseña. |
| `/reset-password` | `reset-password.html` | `ViewController.showResetPasswordForm` | Público | Formulario para ingresar la nueva contraseña. |
| `/dashboard` | `dashboard.html` | `ViewController.dashboard` | Semi-protegido (JS) | Panel de prueba que lee los claims del JWT. |

---

## 🔍 Detalle de Pantallas y Comportamiento Frontend

### 1. Pantalla de Login (`login.html`)
* **Propósito:** Capturar las credenciales (`username` y `password`) del usuario.
* **Flujo y Comportamiento JavaScript:**
  1. El formulario intercepta el evento de submit.
  2. Envía una petición `POST` en formato JSON a `/api/auth/login`.
  3. **Éxito (200 OK):** Guarda el token retornado en el almacén del navegador:
     ```javascript
     localStorage.setItem('jwtToken', data.token);
     ```
     Muestra un mensaje de éxito en color verde y redirige tras 1.5 segundos a `/dashboard`.
  4. **Fallo (401 / Conexión):** Muestra el mensaje de error correspondiente (o "Credenciales inválidas") en color rojo.

---

### 2. Pantalla de Recuperación (`forgot-password.html`)
* **Propósito:** Permitir al usuario solicitar el enlace de restauración.
* **Flujo y Comportamiento JavaScript:**
  1. Captura el `username` del usuario.
  2. Envía un `POST` JSON a `/api/auth/forgot-password`.
  3. Muestra en pantalla el texto de respuesta del backend (en color verde).

---

### 3. Pantalla de Restablecimiento (`reset-password.html`)
* **Propósito:** Ingresar la nueva contraseña habiendo validado el token temporal.
* **Paso de Parámetros (Thymeleaf):**
  - El controlador MVC inyecta el parámetro `token` de la URL en el modelo.
  - La plantilla captura el token mediante un input oculto:
    ```html
    <input type="hidden" id="token" th:value="${token}" />
    ```
* **Flujo y Comportamiento JavaScript:**
  1. Valida en el cliente que las contraseñas escritas coincidan (`newPassword === confirmPassword`).
  2. Envía un `POST` JSON a `/api/auth/reset-password` con `{ token, newPassword }`.
  3. **Éxito:** Muestra mensaje de éxito verde y redirige tras 2 segundos a `/login`.
  4. **Fallo:** Muestra mensaje de error en rojo.

---

### 4. Dashboard de Prueba (`dashboard.html`)
* **Propósito:** Validar que el token funciona y visualizar su contenido.
* **Flujo y Comportamiento JavaScript:**
  1. Al cargar la página, lee `jwtToken` de `localStorage`. Si está vacío, redirige inmediatamente a `/login`.
  2. Decodifica el payload del JWT utilizando una función local en base64:
     ```javascript
     const payload = parseJwt(token);
     ```
  3. Renderiza en el DOM la información de claims (`idUsuario`, `rol`, `sub`).
  4. **Cerrar Sesión:** Al pulsar el botón, ejecuta:
     ```javascript
     localStorage.removeItem('jwtToken');
     window.location.href = '/login';
     ```

---

> **Nota para IA:** Estas vistas utilizan estilos CSS embebidos en el bloque `<style>` de cada archivo. No se utilizan archivos CSS externos o frameworks como Bootstrap/Tailwind. Si se migra la interfaz a un sistema CSS externo, se debe actualizar esta sección.

---

## Última revisión
- **Fecha:** 2026-05-25
- **Commit:** `c646311c83eae3bf4759c7ea39bfde2726ff11c9`

---

## Instrucciones para actualizar este doc
- Si se agregan nuevas vistas HTML o se modifican los métodos de enrutamiento web del controlador → actualiza `VIEWS.md`.
- Si cambia el almacenamiento del token (ej: migrar de `localStorage` a cookies seguras) o la lógica JavaScript frontend → actualiza `VIEWS.md`.

[← Volver al índice](INDEX.md)
