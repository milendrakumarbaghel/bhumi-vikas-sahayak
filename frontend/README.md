# Bhumi Vikas Sahayak Frontend

The frontend is a polished, government-tech browser interface designed specifically for building-permission clerks at Municipal Corporations across Madhya Pradesh (Bhopal, Indore, Jabalpur, Gwalior). Built with React 19, TypeScript, Tailwind CSS, and Vite.

## Design Implementation & Architecture

The interface matches the official Madhya Pradesh Urban Development & Housing Department reference design:

1. **Header**:
   - State Government emblem of India (Ashoka Lion Capital).
   - Branding: **Bhumi Vikas Sahayak** – MP Building Rules AI Assistant.
   - Live backend status indicator (`Spring Boot :8080` / `Local Statutory Engine`).
   - Statutory evaluation As-Of Date selector (`2026-10-11`, `2025-03-06`, `2023-01-01`).
   - Bilingual language switcher (`English` / `हिंदी`).
   - Theme toggle (Light / Dark).
   - Municipal Clerk profile with quick Corporation switcher.

2. **Top Heritage Banner**:
   - Warm sandstone skyline panorama featuring Madhya Pradesh architecture.
   - Devanagari headline: **भूमि विकास सहायक** – *Ask. Understand. Build Right.*
   - Five trust badges:
     - Based on Official MP Building Rules
     - Accurate Rule References
     - Hindi & English Support
     - Calculations & Drawings
     - Trusted for Municipal Clerks

3. **Left Navigation Sidebar**:
   - `+ New Chat`
   - `❓ Example Questions`: Interactive browser with all 17 official clerk benchmark questions.
   - `📑 Rule Categories`: Explorer for Parts I to VII across all 105 rules.
   - `🧮 Calculators`: Deterministic calculators for Setbacks (Rule 32), FAR & BUA (Rule 42), Fees (Rule 21), Height limits, and Electrical clearances (Rule 56).
   - `📜 Amendments & Gazette`: Legal regime tracking including the 28 August 2026 notification for Rule 2(5)(c).
   - `🔖 Saved Answers`: Clerk bookmarked answers with easy retrieval.
   - `🕒 Recent Chats`: History tracking.
   - Bottom card: MP Bhumi Vikas Niyam metadata (108 pages, Version 2025-03-06) with `View PDF` viewer.

4. **Main Interaction Area**:
   - User inquiry bubbles with Municipal Clerk badge and timestamp.
   - AI Assistant Response card:
     - Clear Answer summary.
     - High-precision statutory table (e.g. Front 6m, Rear 4m, Side 3m for 500 sq m in R-1).
     - Statutory explanation citing Rule 32 of MP Bhumi Vikas Rules, 2012.
     - Callout box: Important Notes with statutory conditions.
     - Interactive 2D technical plot blueprint diagram toggle (`PlotDiagramModal`).
     - Utilities: Copy Answer, Download / Print Official Inspection Slip (`PrintMemoModal`), Save answer, Feedback rating.
   - Suggestion chips row and quick example pills.
   - Comprehensive input bar with file attachment, microphone voice input (Web Speech API), and submit button.

5. **Right Citations & Related Rules Panel**:
   - Source cards with exact rule numbers (Rule 32(1), Rule 32(2)), Gazette page badges, and authentic Hindi and English excerpts.
   - Direct `View in PDF` buttons jumping to specific pages in the official gazette.
   - Related Rules navigation (Rule 31, 32, 33, 34).

6. **Spring Boot Integration (`/api/v1`)**:
   - `POST /api/v1/questions:answer`: Cited question evaluation with `question`, `language`, and `asOfDate`.
   - `GET /api/v1/health`: Live connectivity indicator.
   - `POST /api/v1/auth/login` & `GET /api/v1/auth/me`: Bearer token authentication and role management.
   - `GET /api/v1/rules`: Statutory rules retrieval.
   - `GET /api/v1/documents/rules.pdf`: Streams official gazette PDF.
   - Automatic fallback: Seamless local deterministic engine fallback when the backend is offline.

## Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.
To build for production:
```bash
npm run build
```
