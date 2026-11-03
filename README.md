# microservicio-foro — Sistema de Foros en Línea

Microservicio para la **búsqueda, visualización, publicación y comentarios de temas** del foro. Incluye además la **moderación** (banear y desbanear temas o comentarios).
Desarrollo Full Stack III (DSY2205) — Experiencia 1, Sumativa "Programando nuestro BackEnd".

Trabaja junto a [ms-usuarios](https://github.com/brychavez123/microservicio_usuario.git): comparten la base de datos Oracle, así que los mismos usuarios y contraseñas sirven en ambos.

## Tecnologías
- Java 21 + **Spring Boot 3.5** (Maven)
- Spring Web (API REST), Spring Data JPA (consultas derivadas, JPQL y agregaciones), Bean Validation
- Spring Security (HTTP Basic + BCrypt, autorización por roles) y CORS
- **Oracle Cloud Autonomous Database** con Wallet (`ojdbc11` + `oraclepki`)
- Lombok (`@Slf4j` para logging) y Actuator

## Arquitectura (por capas)
```
src/main/java/com/duoc/foro
├── controller/   CategoriaController, TemaController, ComentarioController
├── service/      CategoriaService, TemaService, ComentarioService
├── repository/   Categoria-, Tema-, Comentario-, UsuarioRepository
├── model/        Categoria, Tema, Comentario, EstadoPublicacion (+ Usuario y Rol de solo lectura)
├── dto/          Request / Response con validaciones
├── exception/    GlobalExceptionHandler (@RestControllerAdvice)
└── security/     SecurityConfig, UsuarioDetailsService, UsuarioActual
```

## Modelo de datos
```
ROL 1───* USUARIO 1───* TEMA *───1 CATEGORIA
              │           │
              └──*  COMENTARIO *──┘
```
`TEMA` y `COMENTARIO` tienen un `ESTADO` (`ACTIVO` o `BANEADO`) y un `MOTIVO_BANEO`.

## Roles
| Rol | Privilegios |
|-----|-------------|
| Público (sin login) | Ver y buscar categorías, temas y comentarios activos |
| **USUARIO** | Publicar temas y comentar; editar o eliminar sus propias publicaciones |
| **MODERADOR** | Lo anterior + banear y desbanear temas y comentarios, y ver lo baneado |
| **ADMIN** | Todo lo anterior + gestionar categorías y editar o eliminar cualquier publicación |

## Base de datos
1. Ejecuta primero `db/01_usuarios.sql` del repositorio ms-usuarios.
2. Después ejecuta [`db/02_foro.sql`](db/02_foro.sql). Crea 6 categorías (Tecnología, Videojuegos, Deportes, Música, Cine y Series, Educación), 5 temas y 5 comentarios (en cada caso con uno baneado de ejemplo).

## Configuración y ejecución
1. Descomprime el Wallet en la raíz del proyecto (ej. `Wallet_foro/`).
2. Copia `db.properties.example` como `db.properties` y completa `DB_TNS_NAME`, `DB_WALLET_PATH`, `DB_USERNAME` y `DB_PASSWORD`.
3. Ejecuta:
   ```bash
   mvn spring-boot:run
   ```
   El servicio queda en `http://localhost:8082`.

## Endpoints
Autenticación: **Basic Auth** (usuario = email).

### Categorías
| Método | Endpoint | Acceso |
|--------|----------|--------|
| GET | `/api/categorias` | Público |
| GET | `/api/categorias/{id}` | Público |
| GET | `/api/categorias/{id}/temas` | Público |
| GET | `/api/categorias/resumen` | Público (cantidad de temas activos por categoría) |
| POST / PUT / DELETE | `/api/categorias[/{id}]` | ADMIN |

### Temas
| Método | Endpoint | Acceso |
|--------|----------|--------|
| GET | `/api/temas` | Público |
| GET | `/api/temas/{id}` | Público (un tema baneado solo lo ven MODERADOR y ADMIN) |
| GET | `/api/temas/buscar?q=texto` | Público (busca en el título y el contenido) |
| GET | `/api/temas/categoria/{id}` | Público |
| GET | `/api/temas/usuario/{id}` | Público |
| POST | `/api/temas` | Autenticado (el autor es el usuario logueado) |
| PUT / DELETE | `/api/temas/{id}` | Autor / ADMIN |
| GET | `/api/temas/baneados` | MODERADOR / ADMIN |
| PUT | `/api/temas/{id}/banear` · `/desbanear` | MODERADOR / ADMIN |

### Comentarios
| Método | Endpoint | Acceso |
|--------|----------|--------|
| GET | `/api/temas/{id}/comentarios` | Público |
| POST | `/api/temas/{id}/comentarios` | Autenticado |
| GET | `/api/comentarios/{id}` | Público |
| PUT / DELETE | `/api/comentarios/{id}` | Autor / ADMIN |
| GET | `/api/comentarios/baneados` | MODERADOR / ADMIN |
| PUT | `/api/comentarios/{id}/banear` · `/desbanear` | MODERADOR / ADMIN |

Ejemplos:
```json
POST /api/temas
{ "titulo": "¿Qué teclado mecánico recomiendan?", "contenido": "Busco uno para programar.", "categoriaId": 1 }

PUT /api/temas/3/banear
{ "motivo": "Incumple las normas del foro" }
```

Códigos de respuesta: `200`, `201`, `400` (validación), `401`, `403` (rol sin permiso o no eres el autor), `404`, `409` (conflicto, por ejemplo eliminar una categoría que tiene temas).
