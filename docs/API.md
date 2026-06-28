[← Volver al índice](INDEX.md)

# 🔌 API Reference - autenticacionWeb

Este documento detalla los endpoints REST expuestos por el microservicio de autenticación, incluyendo formatos de payload, códigos de respuesta y ejemplos prácticos.

El servidor corre por defecto en el puerto **`8081`** (o **`8080`** si se usa la configuración embebida por defecto). Los ejemplos siguientes asumen que corre en el puerto `8081`.

---

## 🗺️ Resumen de Endpoints

| Método | Endpoint | Autenticación | Descripción |
|---|---|---|---|---|
| `GET` | `/api/auth/test` | ❌ Pública | Healthcheck para comprobar el estado del servicio. |
| `POST` | `/api/auth/login` | ❌ Pública | Valida credenciales. Si tiene `reset_token` activo, devuelve `mustChangePassword: true`. |
| `POST` | `/api/auth/forgot-password` | ❌ Pública | Solicita token temporal de recuperación de contraseña. |
| `POST` | `/api/auth/reset-password` | ❌ Pública | Restablece la contraseña usando el token temporal (valida política de contraseña). |
| `GET` | `/api/auth/verify-reset-token` | ❌ Pública | Verifica si un token de reset es válido o expiró. |
| `POST` | `/api/auth/recreate-credentials` | ✅ Solo ADMIN | Genera nueva contraseña, la hashea, crea reset token de 7 días y envía credenciales por email. |
| `*` | Cualquier otra ruta (ej: `/api/protegido`) | ✅ JWT Requerido | Endpoint protegido por filtro de seguridad JWT. |

---

## 🔍 Detalle de Endpoints

### 1. Healthcheck

* **URL:** `/api/auth/test`
* **Método:** `GET`
* **Headers requeridos:** Ninguno.
* **Payload de entrada:** Ninguno.
* **Respuestas:**
  - `200 OK`: Retorna `"OK"` en texto plano.

#### Ejemplo de Petición
```bash
curl -i http://localhost:8081/api/auth/test
```

---

### 2. Login de Usuario

* **URL:** `/api/auth/login`
* **Método:** `POST`
* **Headers requeridos:** `Content-Type: application/json`
* **Payload de entrada:**
  ```json
  {
    "username": "admin",
    "password": "mi_password"
  }
  ```
* **Respuestas:**
  - `200 OK` (login normal): Retorna un JSON con el token JWT generado.
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiJ9.eyJpZFVzdWFyaW8iOjEsInJvbCI6IlJPTEVfMSIsInN1YiI6ImFkbWluIiwiaWF0IjoxNzg0ODg3OTc0LCJleHAiOjE3ODQ5NzQzNzR9.signature...",
      "idUsuario": 1,
      "rol": "ROLE_1",
      "redirectUrl": "http://localhost:8085/login-success"
    }
    ```
  - `200 OK` (primer ingreso, debe cambiar contraseña):
    ```json
    {
      "mustChangePassword": true,
      "resetToken": "uuid-del-reset-token",
      "message": "Debes cambiar tu contraseña antes de continuar."
    }
    ```
  - `401 Unauthorized`: Ocurre si las credenciales son incorrectas.
    ```json
    {
      "message": "Credenciales inválidas"
    }
    ```

#### Ejemplo de Petición
```bash
curl -X POST http://localhost:8081/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "admin", "password": "mi_password"}'
```

---

### 3. Solicitar Recuperación de Contraseña (Forgot Password)

* **URL:** `/api/auth/forgot-password`
* **Método:** `POST`
* **Headers requeridos:** `Content-Type: application/json`
* **Payload de entrada:**
  ```json
  {
    "username": "admin"
  }
  ```
* **Respuestas:**
  - `200 OK`: Usuario encontrado, correo enviado.
    ```text
    Correo de recuperación enviado. Revisa tu bandeja de entrada.
    ```
  - `404 Not Found`: Usuario no existe.
    ```text
    Usuario no encontrado.
    ```

#### Ejemplo de Petición
```bash
curl -X POST http://localhost:8081/api/auth/forgot-password \
     -H "Content-Type: application/json" \
     -d '{"username": "admin"}'
```

---

### 4. Restablecer Contraseña (Reset Password)

* **URL:** `/api/auth/reset-password`
* **Método:** `POST`
* **Headers requeridos:** `Content-Type: application/json`
* **Payload de entrada:**
  ```json
  {
    "token": "d748f32c-29b1-4c7c-87d5-8664b4198cc7",
    "newPassword": "mi_nueva_contraseña"
  }
  ```
* **Respuestas:**
  - `200 OK`: La contraseña se actualizó correctamente en la BD.
    ```text
    Contraseña restablecida exitosamente.
    ```
  - `400 Bad Request`: Si la contraseña no cumple la política (6-10 chars, mayúscula, minúscula, número) o el token expiró.
    ```text
    La contraseña debe tener entre 6 y 10 caracteres. Debe incluir al menos una mayúscula. ...
    ```
    o
    ```text
    Token inválido o expirado.
    ```

#### Ejemplo de Petición
```bash
curl -X POST http://localhost:8081/api/auth/reset-password \
     -H "Content-Type: application/json" \
     -d '{"token": "d748f32c-29b1-4c7c-87d5-8664b4198cc7", "newPassword": "mi_nueva_contraseña"}'
```

---

### 5. Verificar Token de Reset (Verify Reset Token)

* **URL:** `/api/auth/verify-reset-token`
* **Método:** `GET`
* **Headers requeridos:** Ninguno.
* **Parámetros query:** `token` — el token UUID recibido por correo.
* **Respuestas:**
  - `200 OK`: Token válido.
    ```json
    {
      "valid": true
    }
    ```
  - `200 OK`: Token expirado o inválido.
    ```json
    {
      "valid": false,
      "message": "El enlace ha expirado. Solicita un nuevo restablecimiento."
    }
    ```

#### Ejemplo de Petición
```bash
curl -i "http://localhost:8081/api/auth/verify-reset-token?token=d748f32c-29b1-4c7c-87d5-8664b4198cc7"
```

---

### 6. Recrear Credenciales (Admin)

* **URL:** `/api/auth/recreate-credentials`
* **Método:** `POST`
* **Headers requeridos:** `Content-Type: application/json`. Requiere autenticación de administrador (validada por gateway).
* **Payload de entrada:**
  ```json
  {
    "username": "juan.perez"
  }
  ```
* **Respuestas:**
  - `200 OK`: Credenciales generadas y enviadas.
    ```json
    {
      "message": "Credenciales actualizadas. Revisa tu correo electrónico."
    }
    ```
  - `404 Not Found`: Usuario no existe.
    ```json
    {
      "message": "Usuario no encontrado"
    }
    ```

#### Ejemplo de Petición
```bash
curl -X POST http://localhost:8081/api/auth/recreate-credentials \
     -H "Content-Type: application/json" \
     -d '{"username": "juan.perez"}'
```

---

### 7. Acceder a Rutas Protegidas

* **URL:** `/cualquier/ruta/protegida`
* **Headers requeridos:** `Authorization: Bearer <token_jwt>`
* **Respuestas:**
  - `200 OK` (o código del endpoint correspondiente).
  - `401 Unauthorized`: Retorna error si el token expiró, no está presente o es inválido.
    - Token expirado: HTTP 401 con mensaje `"Token expirado"`.
    - Token inválido: HTTP 401 con mensaje `"Token inválido"`.
    - Token ausente: HTTP 401 con mensaje `"Token requerido"`.

#### Ejemplo de Petición
```bash
curl -i http://localhost:8081/api/recurso-protegido \
     -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

> **Nota para IA:** Si se añaden nuevos endpoints en `AuthController` o se modifica el payload de entrada de los DTOs, este archivo debe ser sincronizado inmediatamente para reflejar la especificación exacta.

---

## Última revisión
- **Fecha:** 2026-06-25

---

## Instrucciones para actualizar este doc
- Si agregas un nuevo endpoint HTTP → actualiza la tabla de endpoints y añade su sección de detalle en `API.md`.
- Si se alteran los códigos de estado HTTP o el payload JSON de entrada/salida → actualiza `API.md`.

[← Volver al índice](INDEX.md)
