# Bhumi Vikas Sahayak Frontend

The frontend is the browser interface for a Bhopal Municipal Corporation building-permission clerk. It uses React 19, TypeScript and Vite and must call only the Java backend.

The intended interface supports English/Hindi questions, structured follow-up inputs, deterministic calculation breakdowns, exact rule citations, PDF page links and safe `not found`/`needs review` states.

## Current status

The frontend toolchain is configured, but `src/App.tsx` is still the Vite starter screen. The production clerk workflow described in [`../brain/02-product-requirements.md`](../brain/02-product-requirements.md) remains to be implemented.

## Prerequisites

- Node.js 20.19+ or 22.12+ (matching Vite 8 requirements).
- npm.
- The backend running on `http://localhost:8080` when API integration is used.

Check installation:

```text
node --version
npm --version
```

## Run on macOS or Linux

From the repository root:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## Run on Windows PowerShell

From the repository root:

```powershell
Set-Location frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

The same commands work in Windows Command Prompt after running `cd frontend`.

## Available commands

| Command | Purpose |
|---|---|
| `npm run dev` | Start Vite development server with hot reload. |
| `npm run build` | Type-check and create the production build in `dist/`. |
| `npm run lint` | Run ESLint. |
| `npm run preview` | Preview the production build locally. |

## Backend connection

The backend currently permits the default Vite origin `http://localhost:5173`. If the frontend runs on another host/port, set `BVS_CORS_ORIGINS` before starting the backend.

Do not call `http://localhost:8081` from React. Port 8081 belongs to the internal Python AI service and must be reached only through the backend.

## Production build

macOS/Linux or Windows PowerShell:

```text
npm install
npm run lint
npm run build
npm run preview
```

## Planned source structure

```text
src/
├── api/          typed backend client
├── components/   query, answer, calculation and citation components
├── features/     ask, source viewer and feedback
├── i18n/         English/Hindi resources
├── models/       discriminated API response types
├── pages/        clerk workflow page
└── test/         component and end-to-end fixtures
```

See [`../brain/prompts/05-frontend.md`](../brain/prompts/05-frontend.md) for the implementation prompt and acceptance requirements.
