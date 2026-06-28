# Índice de Documentación - autenticacionWeb

Este documento sirve como el mapa principal y única fuente de verdad para la navegación de la documentación técnica del microservicio de autenticación de la plataforma **EduLLM**.

---

## 🗂️ Archivos de Documentación

A continuación se listan todos los documentos técnicos estructurados por área temática:

| Documento | Descripción |
|---|---|
| 🏁 [README.md](README.md) | Vista general del proyecto, propósito del microservicio y guía de inicio rápido. |
| 🏗️ [ARCHITECTURE.md](ARCHITECTURE.md) | Diseño de arquitectura del sistema, estructura de paquetes y flujos detallados. |
| 🔌 [API.md](API.md) | Catálogo detallado de endpoints expuestos, especificación de payloads y respuestas. |
| 🗄️ [DATABASE.md](DATABASE.md) | Modelo de datos físico en PostgreSQL, esquemas de tablas, funciones y consultas. |
| ⚙️ [SERVICES.md](SERVICES.md) | Detalle de los servicios internos de la aplicación y herramientas de observabilidad. |
| 🌐 [INTEGRATIONS.md](INTEGRATIONS.md) | Relación de este componente con el ecosistema EduLLM y servicios de terceros. |
| 🔒 [SECURITY.md](SECURITY.md) | Filtros de seguridad, flujo del filtro JWT, encriptación y análisis de riesgos. |
| 🖥️ [VIEWS.md](VIEWS.md) | Vistas web implementadas mediante plantillas Thymeleaf. |
| 🚀 [IMPROVEMENTS.md](IMPROVEMENTS.md) | Mejoras técnicas sugeridas, roadmap y mitigación de deuda técnica. |
| 👥 [CONTRIBUTING.md](CONTRIBUTING.md) | Guía de contribución para desarrolladores y estándares de código. |
| 📖 [GLOSSARY.md](GLOSSARY.md) | Definiciones y glosario de términos del dominio del proyecto. |
| 📦 [DEPENDENCIES.md](DEPENDENCIES.md) | Catálogo de librerías y dependencias externas declaradas en Maven (`pom.xml`). |
| 📜 [CHANGELOG.md](CHANGELOG.md) | Bitácora histórica de versiones y cambios del microservicio. |

---

> **Nota para IA:** Este índice es el punto de entrada para comprender el microservicio. Si se agregan o eliminan archivos de documentación, este índice debe actualizarse de inmediato para conservar la integridad referencial.

---

## Última revisión
- **Fecha:** 2026-05-25
- **Commit:** `c646311c83eae3bf4759c7ea39bfde2726ff11c9`

---

## Instrucciones para actualizar este doc
- Si cambia la estructura de archivos en la carpeta `/docs` (se añade, elimina o renombra un archivo) → actualiza `INDEX.md`.
- Si se actualiza el estado general del repositorio → actualiza el hash de commit y la fecha en la sección de última revisión de este archivo.
