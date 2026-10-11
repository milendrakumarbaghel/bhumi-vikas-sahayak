# Bhumi Vikas Sahayak

Bhumi Vikas Sahayak is a bilingual building-rules assistant for building-permission clerks at Bhopal Municipal Corporation. It is being built for Track 1 of Bhopal Impact Lab 2026.

The intended workflow is simple: a clerk asks a question in English or Hindi, the system finds the relevant Madhya Pradesh Bhumi Vikas Rules, performs deterministic calculations when required, and returns a concise answer with rule numbers, exact source text and PDF page references.

This software is an informational aid. It does not grant permission, make a legal decision or replace verification by the competent authority.

## Project status

Implemented foundations:

- Loading and cleaning all 106 supplied rule records.
- Detection of inserted rules such as 12-A, 17-a and 26-A.
- PostgreSQL-backed rule and passage persistence with Flyway migrations.
- PostgreSQL full-text/lexical hybrid ranking, precise rule/clause priority and Hindi synonym expansion.
- English/Hindi query normalization and measurement extraction.
- Optional Python AI service for bilingual intent extraction.
- Automatic deterministic fallback when the AI service is disabled or unavailable.
- Authenticated rule, document and cited assistant APIs; USER/ADMIN accounts and administrator-only import.
- Deterministic setback, fee, FAR, complete Rule 42 height/high-rise tables, electric-clearance, sanction-duration and farm-house calculations.
- Sentence-level claim/citation checks plus a separate evidence-only entailment pass; unverifiable prose fails closed.
- Effective-dated amendment drafts, independent administrator review, activation, rejection, historical resolution and audited supersession.
- Actual answer regression checks for all 17 official sample questions and isolated PostgreSQL integration tests.

Still under development:

- Clerk-focused frontend (the current frontend is still the Vite starter screen).
- Live-provider evaluation, domain-expert legal review and production hardening. Semantic checks reduce risk, but cannot mathematically prove every paraphrase.
- Additional specialist calculators and human review of every activated rule table.
- Ingestion/review of the authoritative August 2026 notification (not included in the supplied consolidated corpus).

Detailed requirements and implementation prompts are in [`brain/`](brain/README.md).

## Architecture

```text
Browser
  │
  ▼
React + TypeScript frontend (:5173)
  │ public HTTP
  ▼
Java Spring Boot backend (:8080)
  ├─ authenticated APIs and administrator lifecycle
  ├─ PostgreSQL/Flyway corpus, full-text search and effective-dated amendments
  ├─ deterministic validation, citation and calculation services
  └─ optional internal HTTP call
             │
             ▼
Python AI service (:8081)
  └─ provider prompts, intent, grounded wording, claim verification and calculators
```

The frontend calls only the Java backend. The Python service is internal: it proposes grounded wording and runs deterministic calculators. Java checks claims, citations, named rules and numerical values and requires a separate semantic verification response before returning generated prose.

## Repository layout

```text
frontend/                       React 19, TypeScript and Vite
backend/                        Java 25, Spring Boot, JPA, Flyway and PostgreSQL
ai-service/                     Python 3.11+ internal AI service
track-1-bhumi-vikas-sahayak/    supplied data, official PDFs and sample questions
brain/                          requirements, architecture, API and testing documents
```

Component guides:

- [Frontend setup](frontend/README.md)
- [Backend setup](backend/README.md)
- [Python AI-service setup](ai-service/README.md)

## Prerequisites

- Git.
- Node.js 20.19+ or 22.12+ and npm (matching Vite 8 requirements).
- Java Development Kit 25.
- Python 3.11 or newer.
- PostgreSQL 15 or newer with a database/user matching the backend configuration.
- An Anthropic API key and supported model ID for AI-assisted intent, generated prose and semantic verification; deterministic Python calculators need no provider key.
- Docker for PostgreSQL integration tests and Python for the backend official-answer tests.

Maven does not need to be installed globally; the repository includes Maven Wrapper scripts.

Before starting the backend, create the local PostgreSQL database described in [the backend guide](backend/README.md), or set `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` for an existing database.

## Quick start without AI

This is the simplest way to run the current project. Open two terminals from the repository root.

### macOS or Linux

Terminal 1 - backend:

```bash
cd backend
export DB_URL="jdbc:postgresql://localhost:5432/bhumi_vikas"
export DB_USERNAME="bhumi_vikas"
export DB_PASSWORD="bhumi_vikas"
export BVS_BOOTSTRAP_ADMIN_USERNAME="local-admin"
export BVS_BOOTSTRAP_ADMIN_PASSWORD="replace-with-a-unique-long-password"
BVS_AI_ENABLED=false ./mvnw spring-boot:run
```

Terminal 2 - frontend:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` (currently a starter UI). Verify liveness at `http://localhost:8080/api/v1/health/live`, then [log in and call authenticated APIs](backend/README.md#login-and-use-the-api). Python calculators require the separate Python process and `BVS_AI_ENABLED=true`.

### Windows PowerShell

Terminal 1 - backend:

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

Terminal 2 - frontend:

```powershell
Set-Location frontend
npm install
npm run dev
```

Open `http://localhost:5173`. Verify `/api/v1/health/live`, then follow the [PowerShell login example](backend/README.md#login-and-use-the-api). The starter frontend does not yet implement login/chat.

## Run the complete three-service setup

Use the same non-empty internal token for the backend and AI service. Configure the DB and bootstrap ADMIN variables from the quick start in the backend terminal too. Never commit passwords, tokens or provider keys. Remove bootstrap variables after first provisioning.

### macOS or Linux

Terminal 1 - AI service:

```bash
cd ai-service
export ANTHROPIC_API_KEY="your-api-key"
export ANTHROPIC_MODEL="your-supported-model-id"
export BVS_AI_SERVICE_TOKEN="replace-with-a-random-secret"
python3 -m ai_service
```

Terminal 2 - backend:

```bash
cd backend
export BVS_AI_ENABLED="true"
export BVS_AI_SERVICE_URL="http://localhost:8081"
export BVS_AI_SERVICE_TOKEN="replace-with-the-same-random-secret"
./mvnw spring-boot:run
```

Terminal 3 - frontend:

```bash
cd frontend
npm install
npm run dev
```

### Windows PowerShell

Terminal 1 - AI service:

```powershell
Set-Location ai-service
$env:ANTHROPIC_API_KEY = "your-api-key"
$env:ANTHROPIC_MODEL = "your-supported-model-id"
$env:BVS_AI_SERVICE_TOKEN = "replace-with-a-random-secret"
py -3 -m ai_service
```

If the Python Launcher is unavailable, use `python -m ai_service`.

Terminal 2 - backend:

```powershell
Set-Location backend
$env:BVS_AI_ENABLED = "true"
$env:BVS_AI_SERVICE_URL = "http://localhost:8081"
$env:BVS_AI_SERVICE_TOKEN = "replace-with-the-same-random-secret"
.\mvnw.cmd spring-boot:run
```

Terminal 3 - frontend:

```powershell
Set-Location frontend
npm install
npm run dev
```

## Verify the services

macOS/Linux:

```bash
curl http://localhost:8081/internal/v1/health
curl http://localhost:8080/api/v1/health/ready
```

Windows PowerShell:

```powershell
Invoke-RestMethod http://localhost:8081/internal/v1/health
Invoke-RestMethod http://localhost:8080/api/v1/health/ready
```

## Run tests and checks

macOS/Linux:

```bash
cd backend && ./mvnw test
cd ../ai-service && python3 -m unittest discover -s tests -v
cd ../frontend && npm install && npm run lint && npm run build
```

Windows PowerShell (run each line from the repository root):

```powershell
Set-Location backend; .\mvnw.cmd test; Set-Location ..
Set-Location ai-service; py -3 -m unittest discover -s tests -v; Set-Location ..
Set-Location frontend; npm install; npm run lint; npm run build; Set-Location ..
```

## Configuration

| Variable | Component | Default | Purpose |
|---|---|---|---|
| `PORT` | Backend | `8080` | Public backend port. |
| `DB_URL` | Backend | configured Neon pooled URL | PostgreSQL JDBC URL; override for local development. |
| `DB_USERNAME` | Backend | `neondb_owner` | PostgreSQL username. |
| `DB_PASSWORD` | Backend | empty | Required database password; keep it outside source control. |
| `BVS_PDF_FILE` | Backend | supplied English PDF | Official source PDF path. |
| `BVS_CORS_ORIGINS` | Backend | `http://localhost:5173` | Allowed frontend origin(s). |
| `BVS_AI_ENABLED` | Backend | `true` | Enables the optional internal AI call. |
| `APP_AI_SERVICE_URL` | Backend | `http://localhost:8081` | Default Python AI-service base URL. |
| `BVS_AI_SERVICE_URL` | Backend | value of `APP_AI_SERVICE_URL` | Optional backend-specific override. |
| `BVS_AI_SERVICE_TOKEN` | Both services | empty | Shared internal authentication token. Required outside local development. |
| `BVS_BOOTSTRAP_ADMIN_USERNAME/PASSWORD` | Backend | empty | Provision the first administrator; remove after initial setup. |
| `BVS_AUTH_TOKEN_TTL` | Backend | `8h` | Application Bearer-token lifetime (maximum 24h). |
| `AI_SERVICE_HOST` | AI service | `127.0.0.1` | Bind address. |
| `AI_SERVICE_PORT` | AI service | `8081` | Internal service port. |
| `ANTHROPIC_API_KEY` | AI service | empty | Provider credential. Never expose to frontend/backend source. |
| `ANTHROPIC_MODEL` | AI service | empty | Supported provider model ID. |

## Data and rule version

The supplied machine-readable corpus contains Rules 1-105 plus Rule 26-B from the MP Bhumi Vikas Rules, 2012, consolidated through 6 March 2025. The official English PDF is the source of truth. The Hindi PDF is included for visual reference, but its legacy font is not safe for automatic text extraction.

Repository material identifies a later August 2026 amendment affecting Rule 2(5)(c); the current safety warning starts on 27 August 2026. Its authoritative text and exact legal effective date require verification. Until supplied, independently reviewed and activated, affected questions return review status rather than treating the March 2025 text as current. The implemented lifecycle supports effective dates and supersession once verified material is available.

Local verification on 11 October 2026: 34 Java tests and 40 Python tests passed, including all 17 official sample-response cases and five real PostgreSQL integration tests, with no skips. No live-provider accuracy or expert legal certification is claimed. See [the implementation handoff](brain/07-backend-ai-completion.md).

## Security and privacy

- Do not commit API keys, internal tokens or database credentials.
- Rotate any database password previously shared/committed; removing the password fallback does not revoke that credential.
- Do not expose the Python internal service directly to browsers or the internet.
- Do not enter personal, property-owner or application-identifying data.
- Treat AI output as untrusted until the Java backend validates it against official source text.
- Do not present the application as legal advice or automated approval.

## Source and credits

The Track 1 data and source PDFs come from the Bhopal Impact Lab 2026 participant pack prepared by Laksh Sadhwani:

- [Track 1 problem statement](track-1-bhumi-vikas-sahayak/PROBLEM-STATEMENT.md)
- [Data dictionary](track-1-bhumi-vikas-sahayak/data/DATA_DICTIONARY.md)
- [Sample questions](track-1-bhumi-vikas-sahayak/resources/sample_questions.md)
- [Original participant-pack repository](https://github.com/Laaaaksh/bhopal-impact-lab-2026)

Organisers: Aniket Sahu and Arun Pratap Singh Chandel.
