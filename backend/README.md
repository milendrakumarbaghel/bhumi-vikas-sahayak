# Bhumi Vikas Sahayak Backend

Java 25 / Spring Boot 4 API under `org.springboot.backend`, using PostgreSQL, Spring Security and Flyway. It owns authentication, source selection, amendment lifecycle and final citation/claim validation. LLM prompts and keys stay in the separate Python service.

## Implemented behavior

- Imports all 106 bundled rule records and rebuilds precise numbered passages idempotently.
- PostgreSQL GIN full-text ranking combined with explicit rule/clause priority, lexical matching and Hindi synonym expansion.
- Grounded answer generation through Python, followed by Java checks for exact quotes, rule references, numbers and complete sentence-level claim coverage.
- A separate evidence-only entailment call must approve every claim. Missing, contradictory, incomplete or unavailable verification falls back to source excerpts and deterministic calculations.
- Complete Rule 42 Table 4/5 calculations, along with setbacks, fees, FAR, electric clearances, validity and farm-house limits. Conditional premium FAR is not automatically granted.
- Effective-dated amendments with provenance, independent administrator review, activation, supersession, rejection and audit history. Historical dates resolve the previous applicable text.
- Database-backed USER/ADMIN accounts, BCrypt passwords, expiring opaque Bearer tokens stored only as hashes, logout, account disabling and login lockout.
- Public health/readiness endpoints; corpus import and amendment administration require ADMIN.

This is decision support, not legal approval. The August 2026 Rule 2(5)(c) notification must still be supplied and independently reviewed; no unverified amendment is activated.

## Structure

```text
src/main/java/org/springboot/backend/
├── config/          HTTP client, CORS, database and Spring Security
├── controller/      answers, rules, documents, health, auth and amendments
├── dto/             validated public contracts
├── entity/          corpus and source entities
├── repository/      JPA and PostgreSQL search
├── integration/ai/  grounded-answer and claim-verification HTTP adapter
├── service/         import, retrieval, auth, amendment and validation logic
├── service/impl/    assistant orchestration and Python intent adapter
├── exception/       API problem responses
└── util/            JSONL, passage chunking, language and Hindi query lexicon

src/main/resources/
├── application.properties
├── db/migration/V1__initial_schema.sql
├── db/migration/V2__authentication_amendments_and_search.sql
└── data/bhumi_vikas_rules.jsonl
```

`ClaudeAiService` is a historical name: it calls Python, not the provider directly.

## Prerequisites

- JDK 25 and PostgreSQL 15+.
- Maven Wrapper is included.
- Python 3.11+ for the official sample-answer tests, even if AI is disabled.
- Docker for the PostgreSQL integration suite. Without Docker, container tests skip; a skip is not acceptance evidence.

Create a local development database:

```sql
create user bhumi_vikas with password 'bhumi_vikas';
create database bhumi_vikas owner bhumi_vikas;
```

This password is only a local example. Use managed secrets elsewhere. Set all three DB variables to avoid accidentally using the configured hosted URL.

## Run on macOS / Linux

```bash
cd backend
export DB_URL="jdbc:postgresql://localhost:5432/bhumi_vikas"
export DB_USERNAME="bhumi_vikas"
export DB_PASSWORD="bhumi_vikas"
export BVS_BOOTSTRAP_ADMIN_USERNAME="local-admin"
export BVS_BOOTSTRAP_ADMIN_PASSWORD="replace-with-a-unique-long-password"
export BVS_AI_ENABLED="false"
./mvnw spring-boot:run
```

For Python calculators and grounded answers, start Python separately and set these before running Maven:

```bash
export BVS_AI_ENABLED="true"
export BVS_AI_SERVICE_URL="http://localhost:8081"
export BVS_AI_SERVICE_TOKEN="same-random-secret-as-python"
```

No provider key is needed for Python calculators. Generation/entailment requires the Python provider key.

## Run on Windows PowerShell

```powershell
Set-Location backend
$env:DB_URL = "jdbc:postgresql://localhost:5432/bhumi_vikas"
$env:DB_USERNAME = "bhumi_vikas"
$env:DB_PASSWORD = "bhumi_vikas"
$env:BVS_BOOTSTRAP_ADMIN_USERNAME = "local-admin"
$env:BVS_BOOTSTRAP_ADMIN_PASSWORD = "replace-with-a-unique-long-password"
$env:BVS_AI_ENABLED = "false"
.\mvnw.cmd spring-boot:run
```

Enable Python by setting `BVS_AI_ENABLED=true`, `BVS_AI_SERVICE_URL` and the matching `BVS_AI_SERVICE_TOKEN`.

## Run on Windows Command Prompt

```bat
cd backend
set DB_URL=jdbc:postgresql://localhost:5432/bhumi_vikas
set DB_USERNAME=bhumi_vikas
set DB_PASSWORD=bhumi_vikas
set BVS_BOOTSTRAP_ADMIN_USERNAME=local-admin
set BVS_BOOTSTRAP_ADMIN_PASSWORD=replace-with-a-unique-long-password
set BVS_AI_ENABLED=false
mvnw.cmd spring-boot:run
```

## Login and use the API

All application APIs require `Authorization: Bearer <accessToken>`, except login and health. The old `X-Admin-Token` header is no longer supported.

Bootstrap creates an ADMIN only if the username does not exist; it does not reset existing passwords. Remove bootstrap variables after first provisioning. Have an ADMIN create a second ADMIN for independent amendment review and USER accounts for clerks.

macOS/Linux login (copy the returned `accessToken`):

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"local-admin","password":"replace-with-a-unique-long-password"}'
export BVS_ACCESS_TOKEN="paste-returned-accessToken"
curl -X POST http://localhost:8080/api/v1/questions:answer \
  -H "Authorization: Bearer $BVS_ACCESS_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"question":"My residential building is 9 m high on a 10 m road. What front open space applies?","language":"en","asOfDate":"2026-10-11"}'
```

Windows PowerShell:

```powershell
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/auth/login -ContentType "application/json" -Body '{"username":"local-admin","password":"replace-with-a-unique-long-password"}'
$headers = @{ Authorization = "Bearer " + $login.accessToken }
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/questions:answer -Headers $headers -ContentType "application/json" -Body '{"question":"My residential building is 9 m high on a 10 m road. What front open space applies?","language":"en"}'
```

In Command Prompt use `curl.exe` with escaped JSON double quotes and `Authorization: Bearer YOUR_TOKEN`.

| Method | Path | Access / purpose |
|---|---|---|
| POST | `/api/v1/auth/login` | Public; username/password to accessToken, expiresAt and role. |
| GET | `/api/v1/auth/me` | Authenticated current identity. |
| POST | `/api/v1/auth/logout` | Revoke the current token. |
| POST | `/api/v1/admin/users` | ADMIN; `{username,password,role:"USER" or "ADMIN"}`. |
| POST | `/api/v1/admin/users/{id}/disable` | ADMIN; disable account and revoke its tokens. |
| POST | `/api/v1/questions:answer` | USER/ADMIN; cited answer, calculation, review or not-found. |
| GET | `/api/v1/rules`, `/api/v1/rules/{ruleNumber}` | USER/ADMIN; catalogue/passages. |
| GET | `/api/v1/documents/rules.pdf` | USER/ADMIN; official PDF. |
| POST | `/api/v1/rules/imports:run` | ADMIN; rebuild bundled corpus. |
| GET/POST | `/api/v1/admin/amendments` | ADMIN; list or submit draft. |
| POST | `/api/v1/admin/amendments/{id}/review` | A different ADMIN; `{note:"verified source and exact replacement..."}`. |
| POST | `/api/v1/admin/amendments/{id}/activate` | ADMIN; activate independently reviewed draft. |
| POST | `/api/v1/admin/amendments/{id}/reject` | ADMIN; reject an unactivated draft, `{note:"reason..."}`. |
| GET | `/api/v1/admin/amendments/{id}/history` | ADMIN; append-only lifecycle audit. |
| GET | `/api/v1/health/live`, `/api/v1/health/ready` | Public liveness/readiness. |

A draft body is:

```json
{
  "ruleNumber": "56(3)",
  "notificationNumber": "OFFICIAL-NOTIFICATION-ID",
  "effectiveDate": "2027-01-01",
  "replacementText": "Exact verified replacement clause, not an LLM summary.",
  "sourceUrl": "https://official-authority.example/notification.pdf",
  "sourceSha256": "64 hexadecimal characters from the downloaded authoritative document",
  "pageStart": 1,
  "pageEnd": 2
}
```

The example is a payload template, not a valid notification. Administrators must verify the official URL, checksum, pages, notification/effective date and replacement wording. The server records submitted provenance; it does not download/cryptographically authenticate the legal source. Drafts never enter answers. Same-scope successors close the previous exclusive end date; overlapping whole-rule/sub-clause scopes are rejected for explicit resolution. Calculations based on replaced base text are withheld pending updated reviewed calculator definitions.

Browser PDF links cannot attach Bearer headers directly: the future frontend must fetch an authenticated PDF Blob (and clean up its object URL) or use an authenticated viewer. Do not place tokens in URLs.

## Tests

```bash
cd backend
./mvnw test
```

Windows: `cd backend`, then `.\mvnw.cmd test`. Tests select `python` on Windows and `python3` elsewhere; override with `./mvnw -Dbvs.test.python=/path/to/python test` if needed.

Official tests execute the actual assistant with the real corpus and a temporary Python calculation server. They check expected statuses, rule citations and mandatory legal facts for all 17 questions. PostgreSQL tests verify Flyway/import, FTS/Hindi retrieval, USER/ADMIN access, token revocation/expiry, independent review, rejection and historical supersession. These are regression expectations, not expert legal certification or live-model accuracy measurements.

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | Public API port. |
| `DB_URL` | configured hosted URL | Always override for local/test runs. |
| `DB_USERNAME` | configured hosted user | Override with your database user. |
| `DB_PASSWORD` | empty | Supply externally; no password fallback. |
| `BVS_BOOTSTRAP_ADMIN_USERNAME/PASSWORD` | empty | First ADMIN provisioning; both required together. |
| `BVS_AUTH_TOKEN_TTL` | `8h` | Positive token lifetime, maximum 24h. |
| `BVS_CORS_ORIGINS` | `http://localhost:5173` | Allowed frontend origins. |
| `BVS_PDF_FILE` | supplied English PDF | Document file location. |
| `BVS_AI_ENABLED` | `true` | Enable Python calls. |
| `APP_AI_SERVICE_URL` | `http://localhost:8081` | Internal base URL. |
| `BVS_AI_SERVICE_URL` | APP_AI_SERVICE_URL | Backend-specific override. |
| `BVS_AI_SERVICE_TOKEN` | empty | Shared internal secret; set for deployment. |
| `BVS_AI_SERVICE_TIMEOUT` | `20s` | Per internal HTTP request timeout. |

Use HTTPS and private network isolation in deployment. Provider timeout (Python default 90s) should fit within the configured Java budget, and proxy limits must allow both generation and verification. Authentication is not a complete deployment security program: password recovery, MFA/SSO, distributed rate limiting, retention and operational monitoring remain deployment decisions. Rotate any credential previously committed/shared; removing a source fallback cannot revoke it at the provider.
