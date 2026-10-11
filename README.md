# Bhumi Vikas Sahayak

Bhumi Vikas Sahayak is a bilingual building-rules assistant for building-permission clerks at Bhopal Municipal Corporation. It is being built for Track 1 of Bhopal Impact Lab 2026.

The intended workflow is simple: a clerk asks a question in English or Hindi, the system finds the relevant Madhya Pradesh Bhumi Vikas Rules, performs deterministic calculations when required, and returns a concise answer with rule numbers, exact source text and PDF page references.

This software is an informational aid. It does not grant permission, make a legal decision or replace verification by the competent authority.

## Project status

Implemented foundations:

- Loading and cleaning all 106 supplied rule records.
- Detection of inserted rules such as 12-A, 17-a and 26-A.
- In-memory Lucene rule search with rule/topic pinning.
- Basic English/Hindi query normalization and measurement extraction.
- Optional Python AI service for bilingual intent extraction.
- Automatic deterministic fallback when the AI service is disabled or unavailable.
- Rule catalogue, rule detail, search and health APIs.

Still under development:

- Clerk-focused frontend (the current frontend is still the Vite starter screen).
- Final cited-answer API and citation validation.
- PostgreSQL persistence and migrations.
- Deterministic fee, setback, FAR and other legal calculators.
- Effective-dated amendment activation and administration.

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
  ├─ public APIs
  ├─ rule corpus and Lucene search
  ├─ deterministic validation/fallback
  └─ optional internal HTTP call
             │
             ▼
Python AI service (:8081)
  └─ provider prompts, credentials and intent extraction
```

The frontend calls only the Java backend. The Python AI service is internal and never decides applicable law, calculates official values or creates authoritative citations.

## Repository layout

```text
frontend/                       React 19, TypeScript and Vite
backend/                        Java 25, Spring Boot and Lucene
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
- An Anthropic API key and supported model ID only if you want AI-assisted intent extraction.

Maven does not need to be installed globally; the repository includes Maven Wrapper scripts.

## Quick start without AI

This is the simplest way to run the current project. Open two terminals from the repository root.

### macOS or Linux

Terminal 1 - backend:

```bash
cd backend
BVS_AI_ENABLED=false ./mvnw spring-boot:run
```

Terminal 2 - frontend:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. Backend health is available at `http://localhost:8080/api/health`.

### Windows PowerShell

Terminal 1 - backend:

```powershell
Set-Location backend
$env:BVS_AI_ENABLED = "false"
.\mvnw.cmd spring-boot:run
```

Terminal 2 - frontend:

```powershell
Set-Location frontend
npm install
npm run dev
```

Open `http://localhost:5173`. Backend health is available at `http://localhost:8080/api/health`.

## Run the complete three-service setup

Use the same non-empty internal token for the backend and AI service. Never commit the token or provider key.

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
curl http://localhost:8080/api/health
curl http://localhost:8080/api/rules/56
```

Windows PowerShell:

```powershell
Invoke-RestMethod http://localhost:8081/internal/v1/health
Invoke-RestMethod http://localhost:8080/api/health
Invoke-RestMethod http://localhost:8080/api/rules/56
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
| `BVS_DATA_DIR` | Backend | `../track-1-bhumi-vikas-sahayak/data` | Directory containing rule JSONL. |
| `BVS_PDF_FILE` | Backend | supplied English PDF | Official source PDF path. |
| `BVS_CORS_ORIGINS` | Backend | `http://localhost:5173` | Allowed frontend origin(s). |
| `BVS_AI_ENABLED` | Backend | `true` | Enables the optional internal AI call. |
| `BVS_AI_SERVICE_URL` | Backend | `http://localhost:8081` | Python AI-service base URL. |
| `BVS_AI_SERVICE_TOKEN` | Both services | empty | Shared internal authentication token. Required outside local development. |
| `AI_SERVICE_HOST` | AI service | `127.0.0.1` | Bind address. |
| `AI_SERVICE_PORT` | AI service | `8081` | Internal service port. |
| `ANTHROPIC_API_KEY` | AI service | empty | Provider credential. Never expose to frontend/backend source. |
| `ANTHROPIC_MODEL` | AI service | empty | Supported provider model ID. |

## Data and rule version

The supplied machine-readable corpus contains Rules 1-105 plus Rule 26-B from the MP Bhumi Vikas Rules, 2012, consolidated through 6 March 2025. The official English PDF is the source of truth. The Hindi PDF is included for visual reference, but its legacy font is not safe for automatic text extraction.

Repository material also identifies a later amendment dated 28 August 2026 affecting Rule 2(5)(c). Its authoritative official text is not included in the supplied corpus, so the application must warn rather than silently treat the March 2025 text as current for affected questions.

## Security and privacy

- Do not commit API keys, internal tokens or database credentials.
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
