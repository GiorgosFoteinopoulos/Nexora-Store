# Nexora Store

![CI](https://github.com/GiorgosFoteinopoulos/Nexora-Store/actions/workflows/ci.yml/badge.svg)

Backend for Nexora Technologies, a fictional consumer-electronics company:
product catalog, user accounts, reviews and **Nexa**, an AI shopping advisor.

Work in progress, built step by step with Java 25, Spring Boot 4, PostgreSQL 18 and Docker.

## Run the whole stack with Docker
1. Copy `.env.example` to `.env` and set `DB_PASSWORD`, `JWT_SECRET` (32+ characters) and the admin account.
2. `docker compose up -d --build`
3. 3. Open the store at http://localhost:8081 (API docs: http://localhost:8081/swagger-ui.html)

Nexa runs offline by default. For the real AI advisor set `NEXORA_PROFILES=docker,openai`,
`OPENAI_API_KEY` and `OPENAI_MODEL` in `.env`.

## Develop in IntelliJ
1. `docker compose up -d db` (only the database)
2. Run `NexoraStoreApplication` with the `dev` profile (it activates `openai` too).

## Tests
`./mvnw verify` — Docker must be running; the tests start their own PostgreSQL with Testcontainers.