# FutbolDB Backend

API REST de un juego de **adivinar al futbolista**. El servidor elige un jugador al azar, te da dos pistas y tú vas acertando el resto de sus datos en tantos intentos como necesites.

Proyecto personal de práctica con Java 17, Spring Boot 3, Spring Security (JWT) y PostgreSQL.

## Cómo se juega

1. Te registras e inicias sesión (obtienes un token JWT).
2. Creas una partida: el servidor elige un jugador y te da **2 pistas** (dos campos al azar con su valor).
3. Envías tus respuestas para los campos que quieras. Los campos son: `edad`, `paisRegion`, `posicion`, `liga` y `equipoActual`.
4. Cada intento te dice qué campos acertaste, cuáles fallaste y cuáles faltan. Los aciertos se acumulan.
5. Cuando aciertas los 5 campos la partida termina y se revela el nombre del jugador, junto con el número de intentos.

Las respuestas no distinguen mayúsculas de minúsculas. La posición se escribe con su abreviatura (`POR`, `DFC`, `MC`, `ED`, `DEL`...) y los países, ligas y equipos en inglés, como vienen de TheSportsDB (p. ej. `Spanish La Liga`).

## Stack

- Java 17, Spring Boot 3.3.5, Maven (wrapper incluido)
- Spring Security + JWT (jjwt), contraseñas con BCrypt
- Spring Data JPA + PostgreSQL 16 (Docker Compose)
- [TheSportsDB](https://www.thesportsdb.com/) como fuente de jugadores
- springdoc-openapi (Swagger UI), JUnit 5 + Mockito + H2 para los tests

## Cómo ejecutarlo

Requisitos: JDK 17 o superior y Docker.

```bash
cp .env.example .env
```

Edita `.env`: pon una `DB_PASSWORD` y un `JWT_SECRET` de al menos 32 caracteres (por ejemplo `openssl rand -hex 32`). Si el puerto 5433 ya lo usas, cambia `DB_PORT`.

```bash
docker compose up -d
```

```bash
cd Backend
set -a && source ../.env && set +a
./mvnw spring-boot:run
```

La API queda en `http://localhost:8181` y Swagger UI en `http://localhost:8181/swagger-ui.html`.

Tests (no necesitan Postgres: usan H2 en memoria):

```bash
cd Backend && ./mvnw test
```

## Endpoints

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/api/users/register` | no | Registra un usuario (`name`, `email`, `password` de 8 a 72 caracteres) |
| POST | `/api/auth/login` | no | Devuelve `{"token": "..."}` |
| GET | `/api/users/me` | sí | Datos del usuario autenticado |
| POST | `/api/players/import/{nombre}` | sí | Importa de TheSportsDB los jugadores que coinciden con el nombre |
| POST | `/api/game/nueva` | sí | Crea una partida y devuelve `gameId` + 2 pistas |
| POST | `/api/game/{gameId}/responder` | sí | Evalúa las respuestas de la partida (solo el usuario que la creó) |

Los errores tienen siempre la forma `{"status", "error", "message", "timestamp"}` (400 validación, 401 sin credenciales, 404, 409, 502 si falla TheSportsDB).

## Ejemplo de uso

La base empieza sin jugadores: impórtalos antes de jugar.

```bash
curl -X POST localhost:8181/api/users/register -H 'Content-Type: application/json' \
  -d '{"name":"Ana","email":"ana@example.com","password":"clave-larga-123"}'

TOKEN=$(curl -s -X POST localhost:8181/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"ana@example.com","password":"clave-larga-123"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')

curl -X POST "localhost:8181/api/players/import/Kylian%20Mbappe" -H "Authorization: Bearer $TOKEN"

curl -X POST localhost:8181/api/game/nueva -H "Authorization: Bearer $TOKEN"
# {"gameId":1,"pistas":[{"campo":"liga","valor":"Spanish La Liga"},{"campo":"paisRegion","valor":"France"}], ...}

curl -X POST localhost:8181/api/game/1/responder -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"edad":27,"posicion":"SD"}'
# {"finalizada":false,"intentos":1,"acertadosEstaRonda":["edad","posicion"],"faltantes":["paisRegion","liga","equipoActual"], ...}
```

## Estructura

```
Backend/src/main/java/edu/uam/futboldb/
├── config/      SecurityConfig (BCrypt, rutas públicas), OpenApiConfig
├── security/    JwtService, JwtAuthenticationFilter
├── user/        registro, login, /me
├── player/      entidad Player, importación desde TheSportsDB
├── game/        Game, Campo, GameService (reglas del juego)
└── exception/   ApiException + GlobalExceptionHandler (errores HTTP uniformes)
```

## Limitaciones y mejoras futuras

- Las pistas de **premios** (individuales y colectivos) se quitaron: no había una fuente fiable de datos y el scraper de Wikipedia original era solo una prueba. Podrían volver con una fuente de datos fiable.
- Cualquier usuario autenticado puede importar jugadores; falta un rol de administrador.
- Los valores se comparan como texto exacto en inglés (sin alias ni tolerancia a erratas).
- Sin límite de intentos, puntuación ni ranking.
- Sin migraciones (Flyway): el esquema lo crea Hibernate con `ddl-auto=update`.
- La aplicación no está incluida en el `docker-compose.yml`; solo la base de datos.
