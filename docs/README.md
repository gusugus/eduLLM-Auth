[← Volver al índice](INDEX.md)

# 🔐 README - autenticacionWeb

`autenticacionWeb` es un microservicio basado en **Spring Boot 3** que proporciona la funcionalidad de autenticación centralizada y emisión de tokens **JWT** (JSON Web Tokens) para el ecosistema educativo **EduLLM** (anteriormente conocido como MindBuzz).

El servicio es completamente **stateless** y se comunica con una base de datos central PostgreSQL para validar las credenciales del usuario y recuperar los detalles del perfil y roles asociados.

---

## 🚀 Inicio Rápido

### Prerrequisitos

Para ejecutar este microservicio de forma local o en un entorno de desarrollo, necesitas:

| Requisito | Versión Mínima |
|---|---|
| **Java** | JDK 17 |
| **Maven** | 3.8+ |
| **PostgreSQL** | 14+ |
| **Docker** (Opcional) | 20.10+ |

### Configuración de Base de Datos

El microservicio requiere que la base de datos `edu_llm` esté levantada en el puerto `5432` de tu sistema y que contenga la estructura del esquema `comun`. Asegúrate de que exista el usuario y la contraseña correspondientes (por defecto `admin` / `admin`).

---

## 🛠️ Cómo Compilar y Ejecutar

### 1. Compilación con Maven

Para limpiar y generar el paquete JAR ejecutable sin ejecutar pruebas unitarias:

```bash
mvn clean package -DskipTests
```

El archivo JAR resultante se generará en la ruta:  
`target/autenticacionWeb-0.0.1-SNAPSHOT.jar`

### 2. Ejecutar de Forma Local

Para ejecutar el JAR localmente utilizando el archivo de configuración externo:

```bash
java -jar target/autenticacionWeb-0.0.1-SNAPSHOT.jar \
     --spring.config.location=config/application.yml
```

### 3. Ejecutar con Docker Compose

El proyecto está preparado para correr en contenedores Docker mediante la red del host (`network_mode: host`):

```bash
# Construir la imagen y levantar el contenedor
docker compose up --build

# Detener los contenedores
docker compose down
```

---

## ⚙️ Configuración Rápida

La configuración del microservicio se define en `config/application.yml`:

```yaml
server:
  port: 8081 # Puerto en el cual escucha el microservicio

jwt:
  secret: "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970" # Mínimo 32 caracteres (256 bits)
  expiration: 86400000 # Duración del token en milisegundos (24 horas)

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/edu_llm
    username: admin
    password: admin
```

---

> **Nota para IA:** Este documento está diseñado para la incorporación rápida de nuevos desarrolladores. Si el proceso de compilación, ejecución o los prerrequisitos del sistema cambian, asegúrate de mantener este archivo actualizado.

---

## Última revisión
- **Fecha:** 2026-05-25
- **Commit:** `c646311c83eae3bf4759c7ea39bfde2726ff11c9`

---

## Instrucciones para actualizar este doc
- Si cambias los prerrequisitos de ejecución (versión de Java, herramientas) o comandos de levantamiento → actualiza `README.md`.
- Si se modifica la estructura del puerto por defecto o el archivo de configuración principal de arranque → actualiza `README.md`.

[← Volver al índice](INDEX.md)
