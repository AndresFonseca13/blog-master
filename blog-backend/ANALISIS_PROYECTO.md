# Análisis Completo del Proyecto: Blog Backend

## 1. Descripción del Proyecto

**Blog Backend** es una API REST desarrollada con **Spring Boot 4.0.1** y **Java 21** que sirve como backend para una plataforma de blog personal. La aplicación permite a los usuarios registrarse, autenticarse (local y OAuth2), crear y gestionar posts con contenido multimedia, comentar en publicaciones y recuperar contraseñas por email. Utiliza **MongoDB** como base de datos y **Supabase Storage** para almacenamiento de imágenes.

---

## 2. Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|---|---|---|
| Java | 21 | Lenguaje de programación |
| Spring Boot | 4.0.1 | Framework principal |
| Spring Security | (incluido) | Autenticación y autorización |
| Spring Data MongoDB | (incluido) | Persistencia de datos |
| MongoDB | - | Base de datos NoSQL |
| JWT (jjwt) | 0.11.5 | Tokens de autenticación |
| OAuth2 Client | (incluido) | Login social (Google, Facebook) |
| Lombok | (incluido) | Reducción de boilerplate |
| SpringDoc OpenAPI | 2.8.0 | Documentación Swagger |
| Spring Mail | (incluido) | Envío de correos SMTP |
| Supabase Storage | - | Almacenamiento de imágenes en la nube |
| Maven | - | Gestión de dependencias y build |

---

## 3. Arquitectura del Proyecto

```
com.fonsi13.blogbackend/
├── config/
│   ├── ApplicationConfig.java          # UserDetailsService para Spring Security
│   ├── CorsConfig.java                 # Configuración CORS
│   ├── DataInitializer.java            # Seed del usuario admin inicial
│   ├── MongoConfig.java                # Configuración de MongoDB
│   ├── OpenApiConfig.java              # Configuración de Swagger/OpenAPI
│   └── security/
│       ├── JwtAuthenticationFilter.java # Filtro JWT en cada request
│       ├── JwtService.java              # Generación y validación de JWT
│       ├── SecurityConfig.java          # Cadena de filtros de seguridad
│       └── oauth2/
│           ├── CustomOAuth2UserService.java           # Procesamiento de usuarios OAuth2
│           ├── CustomUserPrincipal.java                # Principal personalizado
│           ├── FacebookOAuth2UserInfo.java             # Extractor de datos Facebook
│           ├── GoogleOAuth2UserInfo.java               # Extractor de datos Google
│           ├── OAuth2AuthenticationSuccessHandler.java # Handler post-login OAuth2
│           ├── OAuth2UserInfo.java                     # Clase abstracta de info OAuth2
│           └── OAuth2UserInfoFactory.java              # Factory de proveedores OAuth2
├── controllers/
│   ├── AuthController.java       # Recuperación de contraseña
│   ├── CommentController.java    # CRUD de comentarios
│   ├── PostControllers.java      # CRUD de posts
│   ├── StorageController.java    # Subida de imágenes
│   └── UserController.java       # Registro, login y perfil
├── dto/                          # Data Transfer Objects
├── exceptions/                   # Excepciones personalizadas + GlobalExceptionHandler
├── models/                       # Entidades MongoDB
├── repositories/                 # Interfaces MongoRepository
└── services/                     # Lógica de negocio (interfaz + implementación)
```

**Patrón arquitectónico**: MVC con separación en capas (Controller → Service → Repository).

---

## 4. Modelos de Datos

### User
| Campo | Tipo | Descripción |
|---|---|---|
| id | String | ID generado por MongoDB |
| username | String | Nombre de usuario (único, indexado) |
| email | String | Correo electrónico |
| password | String | Contraseña hasheada con BCrypt |
| role | String | Rol del usuario ("USER", "ADMIN") |
| provider | AuthProvider | Proveedor de autenticación (LOCAL, GOOGLE, FACEBOOK) |
| providerId | String | ID del proveedor OAuth2 |
| profilePicture | String | URL de foto de perfil |
| emailVerified | boolean | Si el email está verificado |
| createdAt | LocalDateTime | Fecha de creación |

### Post
| Campo | Tipo | Descripción |
|---|---|---|
| id | String | ID generado por MongoDB |
| title | String | Título del post |
| slug | String | URL amigable generada del título |
| content | String | Contenido del post |
| summary | String | Resumen del post |
| coverImage | String | URL de imagen de portada |
| images | List\<String\> | Galería de imágenes adicionales |
| videoUrls | List\<String\> | URLs de videos (YouTube/Vimeo) |
| authorId | String | ID del autor |
| topics | List\<String\> | Temas/etiquetas del post |
| status | PostStatus | Estado (DRAFT, PUBLISHED, ARCHIVED) |
| createdAt | LocalDateTime | Fecha de creación |
| updatedAt | LocalDateTime | Fecha de última modificación |

### Comment
| Campo | Tipo | Descripción |
|---|---|---|
| id | String | ID generado por MongoDB |
| content | String | Contenido del comentario |
| postId | String | ID del post asociado |
| authorId | String | ID del autor |
| createdAt | LocalDateTime | Fecha de creación |
| updatedAt | LocalDateTime | Fecha de última modificación |

### PasswordResetToken
| Campo | Tipo | Descripción |
|---|---|---|
| id | String | ID generado por MongoDB |
| token | String | Token UUID único (indexado) |
| userId | String | ID del usuario asociado |
| expiryDate | LocalDateTime | Fecha de expiración (1 hora) |
| used | boolean | Si ya fue utilizado |

---

## 5. Endpoints de la API

### Autenticación y Usuarios (`/api/v1/users`)

| Método | Ruta | Autenticación | Descripción |
|---|---|---|---|
| POST | `/api/v1/users/register` | No | Registro de nuevo usuario |
| POST | `/api/v1/users/login` | No | Login con username y password |
| GET | `/api/v1/users/me` | Sí (JWT) | Obtener perfil del usuario autenticado |

### Recuperación de Contraseña (`/api/v1/auth`)

| Método | Ruta | Autenticación | Descripción |
|---|---|---|---|
| POST | `/api/v1/auth/forgot-password` | No | Solicitar email de recuperación |
| POST | `/api/v1/auth/reset-password` | No | Restablecer contraseña con token |
| GET | `/api/v1/auth/validate-reset-token` | No | Validar si un token es válido |

### Posts (`/api/v1/posts`)

| Método | Ruta | Autenticación | Descripción |
|---|---|---|---|
| POST | `/api/v1/posts` | Sí (JWT) | Crear nuevo post |
| GET | `/api/v1/posts` | No | Listar posts paginados |
| GET | `/api/v1/posts/{slug}` | No | Obtener post por slug |
| PUT | `/api/v1/posts/{id}` | Sí (JWT) | Actualizar post (autor o ADMIN) |
| DELETE | `/api/v1/posts/{id}` | Sí (JWT) | Eliminar post (autor o ADMIN) |

### Comentarios (`/api/v1/posts/{postId}/comments`)

| Método | Ruta | Autenticación | Descripción |
|---|---|---|---|
| POST | `/api/v1/posts/{postId}/comments` | Sí (JWT) | Crear comentario en un post |
| GET | `/api/v1/posts/{postId}/comments` | Sí (JWT) | Listar comentarios paginados |
| PUT | `/api/v1/posts/{postId}/comments/{commentId}` | Sí (JWT) | Actualizar comentario (autor o ADMIN) |
| DELETE | `/api/v1/posts/{postId}/comments/{commentId}` | Sí (JWT) | Eliminar comentario (autor o ADMIN) |

### Storage (`/api/v1/storage`)

| Método | Ruta | Autenticación | Descripción |
|---|---|---|---|
| POST | `/api/v1/storage/upload` | Sí (JWT) | Subir imagen (JPG, PNG, GIF, WEBP, max 5MB) |

### OAuth2

| Ruta | Descripción |
|---|---|
| `/oauth2/authorization/google` | Iniciar flujo OAuth2 con Google |
| `/oauth2/authorization/facebook` | Iniciar flujo OAuth2 con Facebook |

---

## 6. Funcionalidades Clave

1. **Autenticación dual**: Login local con JWT + OAuth2 social (Google, Facebook)
2. **Autorización por roles**: Roles USER/ADMIN con control de permisos por recurso
3. **CRUD completo de Posts**: Con soporte para multimedia (imágenes, videos), slug automático, estados (DRAFT/PUBLISHED/ARCHIVED)
4. **Sistema de comentarios**: Anidado a posts con paginación
5. **Recuperación de contraseñas**: Flujo completo con token temporal, email HTML y validación
6. **Almacenamiento de imágenes**: Integración con Supabase Storage con validación de tipo y tamaño
7. **Limpieza de recursos**: Eliminación automática de imágenes en Supabase al borrar un post
8. **Documentación API**: Swagger/OpenAPI integrado
9. **Seed de datos**: Creación automática de usuario admin en primera ejecución
10. **Manejo global de errores**: `GlobalExceptionHandler` centralizado con respuestas consistentes

---

## 7. Bugs y Problemas Detectados

### 7.1 CRÍTICO: Falta `@Valid` en `PostControllers` y `UserController`

**Archivo**: `PostControllers.java:29`, `UserController.java:22`

Los DTOs `PostCreateRequest` y `UserRegistrationRequest` tienen anotaciones de validación (`@NotBlank`, `@Email`, `@Size`), pero los controllers no usan `@Valid` en los `@RequestBody`, por lo que las validaciones **nunca se ejecutan**.

```java
// ACTUAL (sin validación)
public ResponseEntity<...> createPost(@RequestBody PostCreateRequest request, ...)

// CORRECTO
public ResponseEntity<...> createPost(@Valid @RequestBody PostCreateRequest request, ...)
```

**Afecta**: `createPost`, `updatePost`, `register`, `login`.

---

### 7.2 CRÍTICO: Inconsistencia en tamaño mínimo de contraseña

**Archivos**: `UserRegistrationRequest.java:17` y `ResetPasswordRequest.java:14`

- Registro: `@Size(min = 6)` - mínimo 6 caracteres
- Reset de contraseña: `@Size(min = 8)` - mínimo 8 caracteres

Un usuario que se registra con 6 caracteres no podría poner la misma contraseña al hacer reset.

---

### 7.3 ALTO: `UserController.getCurrentUser()` accede directamente al repositorio

**Archivo**: `UserController.java:44-64`

El endpoint `/me` inyecta `UserRepository` directamente en el controller y construye el DTO manualmente, duplicando lógica que ya existe en `UserServiceImpl.mapToDTO()`. Esto viola la separación de capas Controller → Service.

---

### 7.4 ALTO: `PostControllers` tiene nombre inconsistente

**Archivo**: `PostControllers.java`

La clase se llama `PostControllers` (plural) mientras que todos los demás controllers usan singular: `CommentController`, `UserController`, `AuthController`, `StorageController`.

---

### 7.5 MEDIO: Falta `/` en ruta de registro

**Archivo**: `UserController.java:21`

```java
@PostMapping("register")  // Falta el slash inicial
```

Mientras que login sí lo tiene:

```java
@PostMapping("/login")    // Correcto
```

Funciona igual por Spring, pero es inconsistente.

---

### 7.6 MEDIO: `getAllPosts` retorna TODOS los posts incluyendo DRAFT y ARCHIVED

**Archivo**: `PostServiceImpl.java:70-78`

El endpoint público `GET /api/v1/posts` usa `postRepository.findAll(pageable)` que retorna posts en cualquier estado. Los visitantes no autenticados no deberían ver borradores ni archivados.

---

### 7.7 MEDIO: El login lanza excepción en vez de retornar error controlado

**Archivo**: `UserServiceImpl.java:55-56`

Si el username no existe, se lanza `ResourceNotFoundException`, que retorna 404 al cliente. Esto revela que el usuario no existe, lo cual es un riesgo de enumeración de usuarios. Debería retornar un mensaje genérico como "Credenciales incorrectas" con 401.

---

### 7.8 MEDIO: No se eliminan los comentarios al borrar un post

**Archivo**: `PostServiceImpl.java:143-164`

Al eliminar un post se eliminan las imágenes de Supabase, pero los comentarios asociados al post quedan huérfanos en la base de datos.

---

### 7.9 BAJO: Uso de `RuntimeException` genérica en lugar de excepciones específicas

**Archivos**: `PostServiceImpl.java:39`, `PostServiceImpl.java:98`, `PostServiceImpl.java:150`, `UserController.java:50`, `ApplicationConfig.java:25`

Se usa `new RuntimeException("Usuario autenticado no encontrado")` en múltiples lugares. Debería usarse `ResourceNotFoundException` o una excepción más específica.

---

### 7.10 BAJO: `PasswordResetServiceImpl.forgotPassword` loguea el email en warning

**Archivo**: `PasswordResetServiceImpl.java:41`

```java
log.warn("Intento de recuperación de contraseña para email no registrado: {}", email);
```

Aunque el endpoint no revela si el email existe, los logs sí lo hacen. En entornos compartidos esto podría ser un vector de información.

---

### 7.11 BAJO: `email` no tiene índice único en MongoDB

**Archivo**: `User.java:25`

Solo `username` tiene `@Indexed(unique = true)`. El campo `email` debería tener también un índice único para evitar duplicados a nivel de base de datos (la validación por código podría fallar en condiciones de concurrencia).

---

### 7.12 BAJO: `slug` no tiene índice único en MongoDB

**Archivo**: `Post.java`

El slug se usa para buscar posts (`findBySlug`) pero no tiene un índice definido, lo que degrada el rendimiento y no garantiza unicidad a nivel de BD.

---

## 8. Mejoras y Recomendaciones de Refactorización

### 8.1 Extraer la lógica de `/me` a `UserService`

Mover la lógica del endpoint `/me` al servicio y eliminar la inyección directa de `UserRepository` en `UserController`:

```java
// En UserService.java
ApiResponse<UserResponseDTO> getCurrentUser(String username);

// En UserController.java (simplificado)
@GetMapping("/me")
public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser(Authentication auth) {
    return ResponseEntity.ok(userService.getCurrentUser(auth.getName()));
}
```

---

### 8.2 Filtrar posts por estado en el listado público

Agregar un query method al repositorio para filtrar solo posts publicados:

```java
// PostRepository.java
Page<Post> findByStatus(PostStatus status, Pageable pageable);
```

---

### 8.3 Incluir información del autor en las respuestas de Posts y Comentarios

Actualmente `PostResponseDTO` y `CommentResponseDTO` solo devuelven `authorId`. Para evitar que el frontend necesite hacer N+1 consultas, incluir el nombre de usuario y foto de perfil del autor:

```java
// En PostResponseDTO
private String authorUsername;
private String authorProfilePicture;
```

---

### 8.4 Usar un Enum para los roles en vez de String

**Archivo**: `User.java:28`

El campo `role` es un `String` libre. Crear un enum `Role` (similar a `AuthProvider` y `PostStatus`) evita errores de typo y facilita validaciones:

```java
public enum Role {
    USER, ADMIN
}
```

---

### 8.5 Centralizar la lógica de verificación de permisos

La lógica `if (!resource.getAuthorId().equals(currentUser.getId()) && !"ADMIN".equals(currentUser.getRole()))` se repite en `PostServiceImpl` (update y delete) y en `CommentServiceImpl` (update y delete). Extraer a un método utilitario:

```java
private void verifyOwnershipOrAdmin(String resourceAuthorId, User currentUser, String resourceName, String action) {
    if (!resourceAuthorId.equals(currentUser.getId()) && !"ADMIN".equals(currentUser.getRole())) {
        throw new UnauthorizedAccessException(resourceName, action);
    }
}
```

---

### 8.6 Usar `@Indexed` en campos de búsqueda frecuente

Agregar índices en MongoDB para optimizar las consultas más comunes:

```java
// User.java
@Indexed(unique = true)
private String email;

// Post.java
@Indexed(unique = true)
private String slug;

@Indexed
private String authorId;

@Indexed
private PostStatus status;

// Comment.java
@Indexed
private String postId;
```

---

### 8.7 Usar `@CreatedDate` y `@LastModifiedDate` de Spring Data

En lugar de setear manualmente `createdAt` y `updatedAt` con `LocalDateTime.now()` en cada servicio, usar las auditorías automáticas de Spring Data MongoDB:

```java
@Document(collection = "posts")
public class Post {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

Requiere habilitar `@EnableMongoAuditing` en la configuración.

---

### 8.8 Configurar CORS desde `application.properties`

**Archivo**: `CorsConfig.java:25`

La URL de origen permitido está hardcodeada a `http://localhost:5173`. Debería externalizarse para que en producción se configure sin recompilar:

```properties
app.cors.allowed-origins=http://localhost:5173
```

---

### 8.9 Agregar paginación por defecto con orden descendente por fecha

**Archivo**: `PostControllers.java:41`

El sort por defecto es `createdAt` ascendente, lo que muestra los posts más antiguos primero. Cambiar a descendente:

```java
@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
```

---

### 8.10 Agregar tests unitarios y de integración

Actualmente solo existe `BlogBackendApplicationTests.java` (el test por defecto generado por Spring Initializr). Se recomienda agregar:

- Tests unitarios para los servicios (`UserServiceImpl`, `PostServiceImpl`, `CommentServiceImpl`)
- Tests de integración para los controllers
- Tests para `JwtService` (generación y validación de tokens)

---

### 8.11 Eliminar comentarios huérfanos al borrar un post

En `PostServiceImpl.deletePost`, agregar la eliminación de los comentarios asociados:

```java
commentRepository.deleteByPostId(postId); // Agregar este método al CommentRepository
```

---

### 8.12 Actualizar la versión de jjwt

La versión actual `0.11.5` usa APIs deprecadas (`setClaims`, `setSubject`, `signWith(Key, SignatureAlgorithm)`). La versión más reciente de jjwt (`0.12.x`) usa un builder API renovado y es más segura.

---

### 8.13 Agregar endpoint para obtener el perfil público de un usuario

Actualmente no hay forma de ver el perfil de otro usuario (para mostrar el autor de un post). Agregar:

```
GET /api/v1/users/{username}  →  Retorna perfil público (sin email ni datos sensibles)
```

---

### 8.14 Renombrar `PostControllers` a `PostController`

Para mantener consistencia con el resto del proyecto (`CommentController`, `UserController`, etc.).

---

## 9. Resumen de Seguridad

| Aspecto | Estado | Observación |
|---|---|---|
| Hashing de contraseñas | OK | BCrypt implementado correctamente |
| JWT | OK | Firma HS256, validación de expiración, secreto configurable |
| CSRF | OK | Deshabilitado (correcto para APIs REST con JWT) |
| CORS | Parcial | Hardcodeado a localhost:5173 |
| Validación de entrada | Parcial | DTOs tienen anotaciones pero falta `@Valid` en varios controllers |
| Enumeración de usuarios | Parcial | Forgot-password no revela info, pero login sí (404 vs 401) |
| Manejo de errores | OK | GlobalExceptionHandler no expone detalles internos |
| Subida de archivos | OK | Validación de tipo, extensión y tamaño |
| OAuth2 | OK | Implementación correcta con Google y Facebook |
| Índices de BD | Parcial | Faltan índices en `email`, `slug`, `postId` |

---

*Análisis generado el 2026-03-08*
