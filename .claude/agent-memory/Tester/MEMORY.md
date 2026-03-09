# QA Tester Agent Memory - Blog Backend

## Project Structure
- **Backend directory:** `blog-backend/` (NOT `backend/`)
- **Stack:** Spring Boot 4.0.1, Java 21, MongoDB, JWT (jjwt 0.11.5), OAuth2, Supabase Storage, Spring Mail
- **Build:** Maven (mvn available at /opt/homebrew/bin/mvn)
- **Java runtime:** OpenJDK 23.0.2 (project targets Java 21)
- **Base package:** `com.fonsi13.blogbackend`
- **No ANALISIS_PROYECTO.md file exists** - must explore project structure manually

## Test Configuration
- Test dependencies come via `spring-boot-starter-data-mongodb-test` (transitive: JUnit 6, Mockito 5.20, AssertJ 3.27)
- `BlogBackendApplicationTests` requires full Spring context (MongoDB, SMTP) - must be @Disabled for unit test runs
- Tests use `@ExtendWith(MockitoExtension.class)` (no Spring context needed for unit tests)
- No `application-test.properties` exists yet

## Known Issues in Production Code
1. StorageController `@RequestMapping` missing leading `/` ("api/v1/storage" vs "/api/v1/storage")
2. RuntimeException used instead of custom exception for "Usuario autenticado no encontrado"
3. PostResponseDTO missing `updatedAt` field mapping
4. UserRegistrationRequest email field lacks @NotBlank validation
5. UserController.getCurrentUser() uses SecurityContextHolder directly instead of Authentication parameter

## Test Files Created (2026-03-09)
- `services/UserServiceImplTest.java` - 9 tests
- `services/PostServiceImplTest.java` - 10 tests
- `services/CommentServiceImplTest.java` - 8 tests
- `services/PasswordResetServiceImplTest.java` - 9 tests
- `config/security/JwtServiceTest.java` - 5 tests (uses reflection for @Value fields)
- `exceptions/GlobalExceptionHandlerTest.java` - 4 tests
- `models/PasswordResetTokenTest.java` - 5 tests
- **Total: 51 tests, 50 passed, 1 skipped, 0 failures**

## Notion Documentation
- Plan de Pruebas page ID: `31ea6879-7e89-80f9-9726-d43408fd1de5`
- URL: https://www.notion.so/31ea68797e8980f99726d43408fd1de5

## Patterns and Lessons
- JwtService test: use reflection to set @Value fields since no Spring context
- Notion update_content requires EXACT text match from fetch output (including table tags)
- Notion renders markdown tables as `<table>` tags - use those in old_str for updates
