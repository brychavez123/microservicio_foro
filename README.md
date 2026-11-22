# microservicio-foro — Sistema de Foros en Línea

Microservicio para la **búsqueda, visualización, publicación y comentarios de temas** del foro.
La moderación (banear y desbanear) vive ahora en su propio microservicio, **ms-moderacion**.
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
| **MODERADOR** | Lo anterior + ver temas y comentarios baneados (banear y desbanear se hace en ms-moderacion) |
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

### Comentarios
| Método | Endpoint | Acceso |
|--------|----------|--------|
| GET | `/api/temas/{id}/comentarios` | Público |
| POST | `/api/temas/{id}/comentarios` | Autenticado |
| GET | `/api/comentarios/{id}` | Público |
| PUT / DELETE | `/api/comentarios/{id}` | Autor / ADMIN |

Ejemplos:
```json
POST /api/temas
{ "titulo": "¿Qué teclado mecánico recomiendan?", "contenido": "Busco uno para programar.", "categoriaId": 1 }
```

Códigos de respuesta: `200`, `201`, `400` (validación), `401`, `403` (rol sin permiso o no eres el autor), `404`, `409` (conflicto, por ejemplo eliminar una categoría que tiene temas).

## Docker
```bash
docker build -t ms-foro .
docker run -p 8082:8082 --env-file db.properties -e DB_WALLET_PATH=/app/wallet -v ./Wallet_NOMBRE:/app/wallet:ro ms-foro
```

## Despliegue en AWS
`.github/workflows/deploy-aws.yml` corre los tests, publica la imagen en **Amazon ECR** y la despliega en la **EC2** por Systems Manager en cada push a `main`. Conserva el contenedor anterior hasta que el nuevo responde (rollback) y termina con una prueba desde internet a través de Nginx. El nombre, el puerto y las rutas de prueba están en `aws/servicio.env`.

Paso a paso completo (CloudFormation, Secrets Manager, S3 y GitHub): [MANUAL-DESPLIEGUE.md](https://github.com/brychavez123/frontend_foro/blob/main/MANUAL-DESPLIEGUE.md).

## Pruebas con Postman

Colección: [`postman/ms-foro.postman_collection.json`](postman/ms-foro.postman_collection.json) (23 peticiones).

1. En Postman: **Import** y arrastra el archivo.
2. Cada petición ya trae la URL pública (`http://100.27.17.133`), los parámetros, el body JSON y la autenticación Basic del usuario de prueba que corresponde.
3. Las peticiones que modifican o eliminan algo recién creado usan un id fijo: cámbialo por el `id` que devolvió el POST.
4. Si la EC2 se reinicia cambia la IP: reemplázala en todo el archivo.

Incluye los casos de error esperados (400 validaciones, 401 sin login, 403 rol sin permiso).
