[← Volver al índice](INDEX.md)

# 🌐 Integrations Reference - autenticacionWeb

Este documento detalla la interacción y acoplamiento de `autenticacionWeb` con otros sistemas del ecosistema EduLLM y servicios de terceros.

---

## 1. Integración con el Ecosistema EduLLM

El microservicio funciona como el **Proveedor de Identidad Central (IdP)** para toda la plataforma EduLLM/MindBuzz. La comunicación se realiza de forma desacoplada:

```
[Cliente] ---> 1. POST /login ---> [autenticacionWeb] (Firma con jwt.secret)
[Cliente] <--- 2. Retorna JWT <--- [autenticacionWeb]
    │
    ├─ 3. Request + Header Bearer JWT
    ▼
[Otros Microservicios] (Decodifican localmente usando el mismo jwt.secret)
```

* **Validación Descentralizada:** Otros microservicios (por ejemplo, el generador de exámenes o el panel del profesor) no necesitan consultar a `autenticacionWeb` para validar si un token es real. Solo necesitan configurar la misma clave secreta (`jwt.secret`) en sus entornos para desencriptar el JWT y obtener el ID de usuario y su rol.

---

## 2. Integraciones de Terceros (APIs Externas)

| Integración | Canal/Protocolo | Propósito | Configuración |
|---|---|---|---|
| **Servidor SMTP de Gmail** | SMTP (Puerto `587` / STARTTLS) | Envío de correos electrónicos con enlaces para restablecer contraseñas. | `spring.mail.host: smtp.gmail.com` |
| **Telegram Bot API** | HTTPS Webhook (a través de GitHub Actions) | Notificación en tiempo real sobre el ciclo de vida de los Pull Requests. | Secretos `TELEGRAM_CHAT_ID` y `TELEGRAM_BOT_TOKEN` |

---

## 3. Webhooks de GitHub / CI/CD: Notificación a Telegram

El proyecto integra un flujo automatizado de CI/CD en `.github/workflows/telegram-notify.yml` que interactúa con la API de bots de Telegram en los siguientes eventos de Pull Request (PR):

* **PR Abierto (`opened`):** Envía un mensaje grupal de color verde indicando el repositorio, título del PR, autor y enlace directo en GitHub.
* **PR Cerrado (`closed`):** Envía un mensaje grupal de color rojo indicando el cierre del Pull Request.
* **Revisión Solicitada (`review_requested`):** Consulta los revisores asignados a través de la API de GitHub y envía un mensaje grupal notificando a los encargados con mención explícita.

---

> **Nota para IA:** Si se migra el servidor de correo a otro proveedor (por ejemplo, Amazon SES o SendGrid) o si se migra de firmas simétricas de JWT a firmas asimétricas (RSA/ECDSA), se deberán actualizar este archivo y `SECURITY.md` para reflejar la nueva topología.

---

## Última revisión
- **Fecha:** 2026-05-25
- **Commit:** `c646311c83eae3bf4759c7ea39bfde2726ff11c9`

---

## Instrucciones para actualizar este doc
- Si se conecta el microservicio a un nuevo componente o API de terceros (ej: OAuth2 Login con Google/GitHub) → actualiza `INTEGRATIONS.md`.
- Si se modifica el canal de notificaciones de CI/CD (ej: migrar de Telegram a Slack o Discord) → actualiza `INTEGRATIONS.md`.

[← Volver al índice](INDEX.md)
