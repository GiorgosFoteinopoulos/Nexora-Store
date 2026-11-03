# Nexora Store

![CI](https://github.com/GiorgosFoteinopoulos/Nexora-Store/actions/workflows/ci.yml/badge.svg)

Backend for Nexora Technologies, a fictional consumer-electronics company:
product catalog, user accounts, reviews and **Nexa**, an AI shopping advisor.

Work in progress, built step by step with Java 25, Spring Boot 4, PostgreSQL 18 and Docker.

## Run locally
1. Copy `.env.example` to `.env` and set `DB_PASSWORD`.
2. `docker compose up -d`
3. Run `NexoraStoreApplication`, then open http://localhost:8080/actuator/health

## Tests
`./mvnw verify` — Docker must be running; the tests start their own PostgreSQL with Testcontainers.