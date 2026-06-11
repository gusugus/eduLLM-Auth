# Cambios de Diseño — Refactorización de Templates y Ajustes en el Gateway

## Contexto

Como parte de la mejora de buenas prácticas en los templates HTML del microservicio `auth-ms`, se realizó una refactorización completa de la capa de presentación. El objetivo fue separar el CSS y el JavaScript del HTML, e introducir TailwindCSS como framework de estilos. Esta refactorización expuso limitaciones en la configuración del API Gateway que requirieron cambios adicionales.

---

## Cambios en `auth-ms` (eduLLM-Auth)

### 1. Separación de CSS y JavaScript

**Antes:** Cada template HTML (`login.html`, `forgot-password.html`, `reset-password.html`, `dashboard.html`) tenía estilos CSS embebidos en etiquetas `<style>` y lógica JavaScript dentro de etiquetas `<script>`, todo dentro del mismo archivo HTML.

**Después:** Se crearon archivos estáticos separados:

```
src/main/resources/static/
  css/
    auth.css          ← Clases .error y .success usadas dinámicamente por JS
  js/
    login.js
    forgot-password.js
    reset-password.js
    dashboard.js
```

Cada template referencia sus archivos externos mediante atributos Thymeleaf:

```html
<link th:href="@{/css/auth.css}" rel="stylesheet">
<script th:src="@{/js/login.js}"></script>
```

> **Nota sobre `reset-password.html`:** El bloque `<script th:inline="javascript">` que contiene `/*[[${token}]]*/` no puede moverse a un archivo externo. Thymeleaf solo procesa expresiones `${...}` en archivos servidos por el motor de plantillas del servidor, no en archivos `.js` estáticos. Por esto, ese bloque permanece inline. El event listener del formulario sí se separó a `reset-password.js`.

### 2. Introducción de TailwindCSS

Se utilizó TailwindCSS v3 mediante CDN oficial:

```html
<script src="https://cdn.tailwindcss.com"></script>
```

Los estilos visuales se expresan como clases de utilidad Tailwind directamente en el HTML, que es el enfoque estándar de este framework. El archivo `auth.css` contiene únicamente las clases personalizadas que el JavaScript manipula dinámicamente (`.error`, `.success`), ya que estas no pueden ser clases de Tailwind porque se agregan y quitan con `classList.add/remove`.

### 3. Degradado azul en login y forgot-password

Se aplicó la clase de Tailwind `bg-gradient-to-br from-blue-500 via-blue-700 to-blue-950` en el `<body>` de `login.html` y `forgot-password.html` para un fondo con degradado en tonos azules, manteniendo consistencia visual entre ambas páginas.

---

## Cambios en `gateway-ms` (eduLLM-Gateway)

Una vez aplicados los cambios en `auth-ms`, los archivos estáticos (`/css/auth.css`, `/js/login.js`, etc.) no cargaban en el navegador. Se diagnosticaron tres problemas en el gateway.

---

### Problema 1 — El gateway no tenía ruta para los archivos estáticos

**Archivo:** `config/application.yml`

**Causa:** Las rutas configuradas en Spring Cloud Gateway solo cubrían:

```yaml
- Path=/api/auth/**, /login, /forgot-password, /reset-password
```

Cuando el navegador cargaba la página de login e intentaba obtener `/css/auth.css` o `/js/login.js`, el gateway no encontraba ninguna ruta que coincidiera y devolvía `404 Not Found`.

**Solución:** Se agregó una ruta específica para los archivos estáticos del `auth-ms`:

```yaml
# Estáticos del auth-ms (CSS y JS)
- id: auth-ms-static
  uri: http://auth-ms:8080
  predicates:
    - Path=/css/**, /js/**
  filters:
    - StripPrefix=0
```

`StripPrefix=0` indica que la ruta se reenvía al microservicio sin modificar el path, de modo que `auth-ms` recibe `/css/auth.css` tal cual y Spring Boot lo resuelve desde su carpeta `static/`.

---

### Problema 2 — El filtro JWT del gateway bloqueaba los archivos estáticos

**Archivo:** `src/main/java/com/edullm/gateway/filter/JwtAuthenticationFilter.java`

**Causa:** El gateway tiene un filtro global (`JwtAuthenticationFilter`) con orden `-100` que intercepta todas las peticiones antes de que lleguen a cualquier ruta. Este filtro tiene una lista `PUBLIC_PATHS` con las rutas que se permiten sin token JWT:

```java
// Estado anterior
private static final List<String> PUBLIC_PATHS = List.of(
    "/api/auth/login", "/api/auth/forgot-password", "/api/auth/reset-password",
    "/login", "/forgot-password", "/reset-password"
);
```

Cualquier ruta que no estuviera en esta lista pasaba a `handleProtectedRoute`, que requiere un token JWT válido. Al no existir `/css/` ni `/js/` en la lista, el gateway respondía `401 Token requerido` antes de llegar al `auth-ms`, incluso habiendo agregado la ruta en el punto anterior.

**Solución:** Se agregaron los prefijos de los estáticos a `PUBLIC_PATHS`:

```java
// Estado actualizado
private static final List<String> PUBLIC_PATHS = List.of(
    "/api/auth/login", "/api/auth/forgot-password", "/api/auth/reset-password",
    "/login", "/forgot-password", "/reset-password",
    "/css/", "/js/"
);
```

La lógica del filtro usa `path::startsWith`, por lo que `/css/` cubre cualquier archivo bajo ese prefijo (`/css/auth.css`, `/css/otro.css`, etc.).

---

### Problema 3 — La política de seguridad de contenido (CSP) bloqueaba el CDN de Tailwind

**Archivo:** `config/application.yml`

**Causa:** El gateway aplica el header `Content-Security-Policy` a todas las respuestas mediante `default-filters`:

```yaml
# Estado anterior
- AddResponseHeader=Content-Security-Policy, "default-src 'self'; script-src 'self' 'unsafe-inline'; ..."
```

La directiva `script-src 'self' 'unsafe-inline'` solo permite scripts del mismo origen o inline. El CDN de Tailwind (`https://cdn.tailwindcss.com`) es un origen externo, por lo que el navegador lo bloqueaba silenciosamente y la página se mostraba sin estilos.

**Solución:** Se agregó el dominio del CDN a la directiva `script-src`:

```yaml
# Estado actualizado
- AddResponseHeader=Content-Security-Policy, "default-src 'self'; script-src 'self' 'unsafe-inline' https://cdn.tailwindcss.com; ..."
```

---

## Resumen de archivos modificados

| Proyecto | Archivo | Tipo de cambio |
|---|---|---|
| `eduLLM-Auth` | `src/main/resources/templates/login.html` | Reescritura con Tailwind, sin CSS/JS inline |
| `eduLLM-Auth` | `src/main/resources/templates/forgot-password.html` | Reescritura con Tailwind, sin CSS/JS inline |
| `eduLLM-Auth` | `src/main/resources/templates/reset-password.html` | Reescritura con Tailwind, bloque th:inline conservado |
| `eduLLM-Auth` | `src/main/resources/templates/dashboard.html` | Reescritura con Tailwind, sin CSS/JS inline |
| `eduLLM-Auth` | `src/main/resources/static/css/auth.css` | Nuevo — clases `.error` y `.success` |
| `eduLLM-Auth` | `src/main/resources/static/js/login.js` | Nuevo — lógica extraída del template |
| `eduLLM-Auth` | `src/main/resources/static/js/forgot-password.js` | Nuevo — lógica extraída del template |
| `eduLLM-Auth` | `src/main/resources/static/js/reset-password.js` | Nuevo — lógica extraída del template |
| `eduLLM-Auth` | `src/main/resources/static/js/dashboard.js` | Nuevo — lógica extraída del template |
| `eduLLM-Gateway` | `config/application.yml` | Nueva ruta `auth-ms-static` + CSP actualizado |
| `eduLLM-Gateway` | `src/.../filter/JwtAuthenticationFilter.java` | `/css/` y `/js/` agregados a `PUBLIC_PATHS` |
