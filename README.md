# FutbolDB Backend

REST API for a **guess-the-footballer** game. The server picks a random player, gives you two clues, and you guess the rest of his details in as many attempts as you need.

Personal practice project built with Java 17, Spring Boot 3, Spring Security (JWT) and PostgreSQL.

## How to play

1. Register and log in (you get a JWT token).
2. Create a game: the server picks a player and gives you **2 clues** (two random fields with their value).
3. Send your answers for any of the fields. The fields are: `edad` (age), `paisRegion` (country/region), `posicion` (position), `liga` (league) and `equipoActual` (current team).
4. Each attempt tells you which fields you got right, which you got wrong and which are still missing. Correct answers accumulate.
5. When you get all 5 fields right the game ends and the player's name is revealed, along with the number of attempts.

Answers are case-insensitive. The position is written with its Spanish abbreviation (`POR`, `DFC`, `MC`, `ED`, `DEL`...), and countries, leagues and teams are written in English, as they come from TheSportsDB (e.g. `Spanish La Liga`).

> Note: field names and routes of the API (`edad`, `posicion`, `/api/game/nueva`, `responder`...) are in Spanish.

## Stack

- Java 17, Spring Boot 3.3.5, Maven (wrapper included)
- Spring Security + JWT (jjwt), passwords hashed with BCrypt
- Spring Data JPA + PostgreSQL 16 (Docker Compose)
- [TheSportsDB](https://www.thesportsdb.com/) as the source of players
- springdoc-openapi (Swagger UI), JUnit 5 + Mockito + H2 for the tests

## How to run it

Requirements: JDK 17 or higher and Docker.

```bash
cp .env.example .env
```

Edit `.env`: set a `DB_PASSWORD` and a `JWT_SECRET` of at least 32 characters (for example `openssl rand -hex 32`). If port 5433 is already in use, change `DB_PORT`.

```bash
docker compose up -d
```

```bash
cd Backend
set -a && source ../.env && set +a
./mvnw spring-boot:run
```

The API runs at `http://localhost:8181` and Swagger UI at `http://localhost:8181/swagger-ui.html`.

Tests (they don't need Postgres: they use in-memory H2):

```bash
cd Backend && ./mvnw test
```

## Endpoints

| Method | Route | Auth | Description |
|---|---|---|---|
| POST | `/api/users/register` | no | Registers a user (`name`, `email`, `password` of 8 to 72 characters) |
| POST | `/api/auth/login` | no | Returns `{"token": "..."}` |
| GET | `/api/users/me` | yes | Data of the authenticated user |
| POST | `/api/players/import/{nombre}` | yes | Imports from TheSportsDB the players matching the name |
| POST | `/api/game/nueva` | yes | Creates a game and returns `gameId` + 2 clues |
| POST | `/api/game/{gameId}/responder` | yes | Evaluates the answers for the game (only the user who created it) |

Errors always have the shape `{"status", "error", "message", "timestamp"}` (400 validation, 401 missing credentials, 404, 409, 502 if TheSportsDB fails).

## Usage example

The database starts without players: import some before playing.

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

## Structure

```
Backend/src/main/java/edu/uam/futboldb/
├── config/      SecurityConfig (BCrypt, public routes), OpenApiConfig
├── security/    JwtService, JwtAuthenticationFilter
├── user/        registration, login, /me
├── player/      Player entity, import from TheSportsDB
├── game/        Game, Campo, GameService (game rules)
└── exception/   ApiException + GlobalExceptionHandler (uniform HTTP errors)
```

## Limitations and future improvements

- The **awards** clues (individual and team) were removed: there was no reliable data source and the original Wikipedia scraper was only a test. They could come back with a reliable data source.
- Any authenticated user can import players; an admin role is missing.
- Values are compared as exact English text (no aliases or typo tolerance).
- No attempt limit, scoring or ranking.
- No migrations (Flyway): the schema is created by Hibernate with `ddl-auto=update`.
- The application is not included in `docker-compose.yml`; only the database is.
