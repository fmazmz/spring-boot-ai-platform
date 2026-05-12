# AI integration platform

Spring Boot service that sits between authenticated users and OpenRouter: GitHub OAuth for identity, Postgres for agents and model catalog, RestClient for outbound calls, in-memory chat history keyed by user and session, and OpenAPI at `/swagger-ui.html`.

## Prerequisites

- JDK and Maven as in the project (`pom.xml`).
- PostgreSQL (local or Docker). `compose.yaml` defines a Postgres 16 instance; align credentials with your `SPRING_DATASOURCE_*` values.

## Configuration

Set environment variables (or a project-root `.env` file; the app loads it on startup if present):

Use the provided `.env.example` : `cp .env.example .env`

| Variable | Purpose |
|----------|---------|
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | JDBC to Postgres |
| `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET` | OAuth2 login with GitHub |
| `OPENROUTER_API_KEY` | Server-side OpenRouter key (see below) |

Optional: override `gateway.providers.openrouter.default-model` (default `openai/gpt-4o-mini` in `application.yaml`).

## OpenRouter: two API key roles

The design separates **who pays for catalog ingestion** from **who pays for inference**.

1. **Server key (`OPENROUTER_API_KEY`)**  
   Mapped to `gateway.providers.openrouter.api-key`. Used only for OpenRouter **`GET /models`** when you run **`POST /api/v1/chat/models/sync`**. That call pulls the provider catalog and persists it for listing and selection. It does not go on chat requests. Use a key you control (organization or deployment account).

2. **Per-request user key (`X-Provider-Api-Key`)**  
   Required on **`POST /api/v1/chat`** and **`POST /api/v1/chat/completions`**. Sent as the Bearer token to OpenRouter **`POST /chat/completions`**. Each authenticated user supplies their own key so usage and billing accrue to that key’s OpenRouter account, not necessarily the server’s.

`GET /api/v1/chat/models` reads the **local database** (optionally filtered and paged); it does not call OpenRouter unless you trigger sync separately.

## Run

```bash
docker compose up -d   # if using bundled Postgres
mvn spring-boot:run
```

API base path is under `/api`. Browser docs: `/swagger-ui.html` (publicly reachable; API routes still require GitHub login except documented public paths).

## Notable API surface

- **`POST /api/v1/chat`** — Agent-backed chat: body includes `agent` (`AgentRole` enum name, e.g. `BACKEND_DEVELOPER`), `message`, optional `sessionId`. System prompt comes from the seeded `Agent` row. Header `X-Provider-Api-Key` required.
- **`POST /api/v1/chat/completions`** — Lower-level chat with explicit `model` and `messages` list; same header.
- **`POST /api/v1/chat/models/sync`** — Refresh model catalog from OpenRouter using the **server** key.
- **`GET /api/v1/chat/models`** — Paged models from DB (no user OpenRouter key).

Agents are seeded at startup from `AgentRole` into the `agents` table when missing; prompts are editable via your agent HTTP API if you extend usage that way.

## Tests

```bash
mvn test
```

Uses in-memory H2 and test `application.yaml`; no live OpenRouter or Postgres required for the default suite.
