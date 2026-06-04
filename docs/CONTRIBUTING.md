[← Volver al índice](INDEX.md)

# 👥 Developer Contribution Guide - autenticacionWeb

Este documento sirve como guía para cualquier desarrollador que desee añadir funcionalidades, corregir errores o realizar modificaciones en la base de código de este microservicio.

---

## 🛠️ Entorno de Desarrollo y Requisitos

1. **IDE Recomendado:** Eclipse IDE (con plugins de Maven), IntelliJ IDEA o VS Code.
2. **Kit de Desarrollo:** Java SE 17.
3. **Gestor de Dependencias:** Maven 3.8 o superior.
4. **Base de Datos Local:** PostgreSQL con la base de datos `edu_llm` accesible.

---

## 🏗️ Cómo Añadir Código o Funcionalidades

### 1. Crear un Nuevo Endpoint REST

Para crear un nuevo controlador o ruta HTTP:
1. Navega al paquete `controller/` e inicializa una nueva clase.
2. Añade las anotaciones `@RestController` y `@RequestMapping` correspondientes:
   ```java
   package controller;

   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.RequestMapping;
   import org.springframework.web.bind.annotation.RestController;

   @RestController
   @RequestMapping("/api/recurso")
   public class MiControlador {
       @GetMapping
       public String obtenerRecurso() {
           return "Datos";
       }
   }
   ```
3. Si el endpoint debe ser de acceso libre, edita `SecurityConfig.java` y añade el patrón de ruta en `authorizeHttpRequests` (`.requestMatchers("/api/recurso/**").permitAll()`). Además, añade la ruta en el método `shouldNotFilter` de `JwtRequestFilter.java`.
4. Si debe ser protegido, no agregues excepciones; el filtro `JwtRequestFilter` validará el JWT automáticamente.

### 2. Modificar el Modelo de Datos del Usuario

Si la base de datos añade información al usuario:
1. Agrega el nuevo atributo a `dto/UsuarioLogin.java`.
2. Como se utiliza Lombok, los getters y setters se generan de manera automática mediante la anotación `@Data`.
3. Modifica la consulta y el mapeador en `repositorio/UsuarioRepository.java` para recuperar y setear la nueva columna de base de datos en el DTO:
   ```java
   u.setNuevoCampo(rs.getString("nuevo_campo"));
   ```

### 3. Implementar un Nuevo Servicio

Si se requiere lógica de negocio adicional:
1. Crea la interfaz en `services/` y su clase de implementación asociada.
2. Utiliza la anotación `@Service` para el registro en el contenedor de inversión de control de Spring.
3. Utiliza la anotación `@Autowired` para inyectar dependencias y `@Slf4j` de Lombok para registro de logs.

---

## 📦 Flujo de Trabajo y Compilación

Antes de enviar tus cambios al repositorio remoto, asegúrate de:

* **Compilar localmente** para verificar errores de sintaxis o dependencias faltantes:
  ```bash
  mvn clean compile
  ```
* **Empaquetar la aplicación** y comprobar que no se rompan pruebas:
  ```bash
  mvn clean package -DskipTests
  ```
* **Formatear el código** y eliminar imports sin utilizar.

---

> **Nota para IA:** El proyecto utiliza Lombok. Asegúrate de tener instalado el agente de Lombok en tu Eclipse/IntelliJ local o las clases DTO mostrarán errores de compilación por falta de getters y setters.

---

## Última revisión
- **Fecha:** 2026-05-25
- **Commit:** `c646311c83eae3bf4759c7ea39bfde2726ff11c9`

---

## Instrucciones para actualizar este doc
- Si se modifica la estructura del flujo de contribución o la forma de inyectar dependencias y capas en el framework → actualiza `CONTRIBUTING.md`.
- Si se añaden requisitos del compilador o directivas de análisis estático de código → actualiza `CONTRIBUTING.md`.

[← Volver al índice](INDEX.md)
