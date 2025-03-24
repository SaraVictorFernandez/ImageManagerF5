# Prueba técnica F5

## Requisitos para instalar y probar la aplicación

### Backend
- Java 17 o superior
- Gradle 8.x
- H2 Database (incluida en las dependencias)

### Frontend
- Node.js y npm (versiones LTS recomendadas)

## Arquitectura y Proceso de Desarrollo

### Backend

El backend está desarrollado utilizando Spring Boot 3.2.3 y sigue una arquitectura en capas (Layered Architecture) con los siguientes componentes:

1. **Controllers**: Manejan las peticiones HTTP y la comunicación con el cliente
   - `ImageController`: Gestiona operaciones CRUD para imágenes
   - `UserController`: Maneja operaciones relacionadas con usuarios
   - `AuthController`: Gestiona la autenticación y autorización

2. **Services**: Contienen la lógica de negocio
   - Implementan la lógica específica de cada funcionalidad
   - Manejan las transacciones y la coordinación entre diferentes componentes

3. **Repositories**: Acceso a datos
   - Utilizan Spring Data JPA para la persistencia
   - Implementan las operaciones de base de datos

4. **Entities**: Modelos de datos
   - Representan las entidades del dominio
   - Mapeadas a tablas en la base de datos

5. **DTOs**: Objetos de transferencia de datos
   - Facilitan la transferencia de datos entre capas
   - Evitan la exposición directa de entidades

6. **Security**: Implementación de seguridad
   - Autenticación basada en JWT
   - Autorización basada en roles

### Técnicas y Buenas Prácticas Implementadas

1. **Arquitectura Limpia**
   - Separación clara de responsabilidades
   - Dependencias hacia el centro
   - Inversión de control mediante inyección de dependencias

2. **Seguridad**
   - Implementación de JWT para autenticación
   - Protección de endpoints
   - Manejo seguro de archivos

3. **Manejo de Errores**
   - Excepciones personalizadas
   - Manejadores globales de excepciones
   - Respuestas HTTP apropiadas

4. **Validación**
   - Validación de datos de entrada
   - Validación de archivos
   - Mensajes de error descriptivos

5. **Testing**
   - Tests unitarios
   - Tests de integración
   - Tests de seguridad

6. **Documentación**
   - Código documentado
   - README detallado
   - Comentarios explicativos en código complejo

### Tecnologías Principales

- **Framework**: Spring Boot 3.2.3
- **Base de Datos**: H2 (embebida)
- **ORM**: Spring Data JPA
- **Seguridad**: Spring Security + JWT
- **Gestión de Dependencias**: Gradle
- **Testing**: JUnit 5, Spring Test

## Instalación y Ejecución

1. Clonar el repositorio

### Backend
1. Navegar al directorio del backend
2. (Opcional) Ejecutar los tests con `./gradlew test`
3. Ejecutar `./gradlew bootRun`
4. La aplicación estará disponible en `http://localhost:8080`

### Frontend
1. Navegar al directorio del frontend
2. Ejecutar `npm install`
3. Ejecutar `npm run dev`
4. La aplicación estará disponible en `http://localhost:5173`


## Endpoints Principales del Backend

### Gestión de Imágenes
- `POST /api/images`: Subir una nueva imagen
- `GET /api/images`: Obtener todas las imágenes
- `GET /api/images/{id}`: Obtener una imagen específica
- `PATCH /api/images/{id}`: Actualizar una imagen
- `DELETE /api/images/{id}`: Eliminar una imagen

### Gestión de Usuarios y Autenticación
- `POST /api/auth/register`: Registrar un nuevo usuario
- `POST /api/auth/login`: Iniciar sesión y obtener token JWT
- `GET /api/users/me`: Obtener información del usuario actual
- `PATCH /api/users/{id}`: Actualizar información del usuario (sólo si es el actual)
- `DELETE /api/users/{id}`: Eliminar cuenta de usuario (sólo si es el actual)

## Consideraciones especiales de Frontend

Se han dejado algunos detalles de usabilidad fuera del alcance del proyecto, por lo que no se muestran algunos errores específicos para las siguientes casuísticas:

- Para registrar un usuario, el username tiene que tener mínimo 3 carácteres y el password 6

- Los formatos aceptados para las imágenes son:
    - JPEG/JPG
    - PNG
    - GIF

## Consideraciones de Seguridad

- Todas las operaciones requieren autenticación mediante JWT
- Los archivos se validan antes de ser procesados
- Las contraseñas se almacenan de forma segura