# Bhumi Vikas Sahayak Backend

The backend is the public API and deterministic legal-processing boundary. It uses Java 25, Spring Boot, Spring Data JPA, Flyway and PostgreSQL. Provider prompts and credentials remain in the separate Python `ai-service/`.

## Structure

```text
src/main/java/org/springboot/backend/
├── BackendApplication.java
├── config/          AI HTTP client, CORS and database configuration
├── controller/      assistant, rule and source-document APIs
├── dto/             validated transport contracts
├── entity/          JPA rule, passage, source and amendment models
├── repository/      Spring Data repositories
├── service/         retrieval, import, calculation, citation and amendment logic
├── service/impl/    assistant orchestration and Python AI-service adapter
├── exception/       API problem responses
└── util/            JSONL parsing and language detection

src/main/resources/
├── application.properties
├── db/migration/V1__initial_schema.sql
└── data/bhumi_vikas_rules.jsonl
```

Despite its requested historical name, `ClaudeAiService` does not call Claude directly. It is an HTTP adapter to the Python AI service and contains no provider key or prompt.

## Current behavior

- Flyway creates the PostgreSQL schema.
- The bundled 106-record JSONL corpus is imported once when the rule table is empty.
- Questions use the Python intent service when available and a deterministic parser otherwise.
- Rule passages are retrieved from PostgreSQL.
- Citation excerpts are checked against stored source text.
- Every answer includes the March 2025 corpus version, the official-document endpoint and page-linked citations.
- Responses honor an explicit `en` or `hi` request and otherwise use detected question language.
- Rule 56 residential front-open-space calculation is deterministic when road width and building height are present.
- Questions without matching evidence return `NOT_FOUND_IN_RULES`.
- Rule 2(5)(c) queries after 27 August 2026 receive a review warning until an authoritative amendment is loaded.

## Prerequisites

- Java Development Kit 25.
- PostgreSQL 15 or newer.
- Maven is not required globally; Maven Wrapper is included.

Create a local database and user, or configure an existing PostgreSQL database:

```sql
create user bhumi_vikas with password 'bhumi_vikas';
create database bhumi_vikas owner bhumi_vikas;
```

The shown password is a local-development default only. Use a secret manager or environment variables outside local development.

## Run on macOS or Linux

Without AI:

```bash
cd backend
export DB_URL="jdbc:postgresql://localhost:5432/bhumi_vikas"
export DB_USERNAME="bhumi_vikas"
export DB_PASSWORD="bhumi_vikas"
export BVS_AI_ENABLED="false"
./mvnw spring-boot:run
```

With the Python AI service already running:

```bash
cd backend
export DB_URL="jdbc:postgresql://localhost:5432/bhumi_vikas"
export DB_USERNAME="bhumi_vikas"
export DB_PASSWORD="bhumi_vikas"
export BVS_AI_ENABLED="true"
export BVS_AI_SERVICE_URL="http://localhost:8081"
export BVS_AI_SERVICE_TOKEN="replace-with-the-same-random-secret"
./mvnw spring-boot:run
```

## Run on Windows PowerShell

```powershell
Set-Location backend
$env:DB_URL = "jdbc:postgresql://localhost:5432/bhumi_vikas"
$env:DB_USERNAME = "bhumi_vikas"
$env:DB_PASSWORD = "bhumi_vikas"
$env:BVS_AI_ENABLED = "false"
.\mvnw.cmd spring-boot:run
```

To use the Python service, set `BVS_AI_ENABLED=true`, `BVS_AI_SERVICE_URL`, and the matching `BVS_AI_SERVICE_TOKEN` before the final command.

## Run on Windows Command Prompt

```bat
cd backend
set DB_URL=jdbc:postgresql://localhost:5432/bhumi_vikas
set DB_USERNAME=bhumi_vikas
set DB_PASSWORD=bhumi_vikas
set BVS_AI_ENABLED=false
mvnw.cmd spring-boot:run
```

## API endpoints

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/questions:answer` | Retrieve evidence and return a safe cited/calculated response. |
| `GET` | `/api/v1/rules` | List imported rules. |
| `GET` | `/api/v1/rules/{ruleNumber}` | Return one rule and its passages. |
| `POST` | `/api/v1/rules/imports:run` | Idempotently import bundled rules when the database is empty. |
| `GET` | `/api/v1/documents/rules.pdf` | Stream the official English rules PDF. |

Example:

```bash
curl -X POST http://localhost:8080/api/v1/questions:answer \
  -H 'Content-Type: application/json' \
  -d '{"question":"My residential building is 9 m high on a 10 m road. What front open space applies?","language":"en","asOfDate":"2026-10-11"}'
```

Windows PowerShell:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/v1/questions:answer `
  -ContentType "application/json" `
  -Body '{"question":"My residential building is 9 m high on a 10 m road. What front open space applies?","language":"en","asOfDate":"2026-10-11"}'
```

## Test

macOS/Linux:

```bash
cd backend
./mvnw test
```

Windows:

```powershell
Set-Location backend
.\mvnw.cmd test
```

Unit tests use mocks and do not require PostgreSQL or a live AI provider.

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | Public HTTP port. |
| `DB_URL` | configured Neon pooled URL | PostgreSQL JDBC URL; override for local development. |
| `DB_USERNAME` | `neondb_owner` | Database user. |
| `DB_PASSWORD` | empty | Required database password; keep it outside source control. |
| `BVS_PDF_FILE` | supplied English PDF path | Official PDF served by the document API. |
| `BVS_CORS_ORIGINS` | `http://localhost:5173` | Allowed frontend origins. |
| `BVS_AI_ENABLED` | `true` | Enable optional Python intent service. |
| `APP_AI_SERVICE_URL` | `http://localhost:8000` | Default internal Python service base URL. |
| `BVS_AI_SERVICE_URL` | value of `APP_AI_SERVICE_URL` | Optional backend-specific override. |
| `APP_JWT_SECRET` | empty | JWT signing key; keep it outside source control. |
| `BVS_AI_SERVICE_TOKEN` | empty | Shared internal token; required outside local development. |
| `BVS_AI_SERVICE_TIMEOUT` | `5s` | Internal HTTP timeout. |

Never place provider API keys in backend configuration.
