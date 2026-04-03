# Collabry – Group 04

Collabry is a platform that connects brands with influencers. Brands can create campaigns, find influencers, send collaboration invitations, and manage payments. Influencers can set up their profile, respond to invitations, submit deliverables, and track earnings. There's also an admin panel for user management.

The backend is Spring Boot (Java 17) and the frontend is React with TypeScript. We use H2 for local dev and MySQL in production. Authentication is JWT-based with Google OAuth support.

---

## Table of Contents

- [What's in this repo](#whats-in-this-repo)
- [Dependencies](#dependencies)
- [How to run it](#how-to-run-it)
- [Running tests](#running-tests)
- [Features and usage](#features-and-usage)
- [Design principles](#design-principles)
- [TDD](#tdd)
- [Code smells](#code-smells)

---

## What's in this repo

```
group04/
├── backend/              # Spring Boot API
├── frontend/             # React + Vite frontend
├── CodeSmells_Designite/ # DesigniteJava smell reports
├── docs/
│   ├── USAGE_SCENARIOS.md
│   └── INTEGRATION_TESTS.md
├── DESIGN_PRINCIPLES.md
├── DEPLOYMENT.md
├── quality/README.md
├── docker-compose.yml
└── README.md
```

---

## Dependencies

### What you need installed

| Tool | Version | Why |
|------|---------|-----|
| Java JDK | 17+ | to run the backend |
| Maven | 3.6+ | to build the backend (we also include `./mvnw` so you can skip this) |
| Node.js | 18+ | to run the frontend |
| npm | 9+ | to install frontend packages |
| Docker + Docker Compose | any recent version | if you want to run everything with one command |
| MySQL | 8+ | only needed in production, local dev uses H2 |

#### Java 17

```bash
# Ubuntu/Debian
sudo apt update && sudo apt install openjdk-17-jdk -y

# macOS
brew install openjdk@17

# Windows – grab the installer from https://adoptium.net
```

Check it worked: `java -version`

#### Node.js 18+

```bash
# Ubuntu/Debian
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# macOS
brew install node@18

# Windows – download from https://nodejs.org
```

Check: `node -version`

#### Docker

```bash
# Ubuntu/Debian
sudo apt install docker.io docker-compose-plugin -y
sudo usermod -aG docker $USER  # so you don't need sudo every time

# macOS/Windows – just install Docker Desktop
```

### Backend libraries (Maven handles these automatically)

You don't need to install these manually, Maven downloads them when you build:

- Spring Boot 3.5 (web, security, data-jpa, validation, mail)
- H2 (in-memory DB for dev)
- MySQL connector (for prod)
- JWT (jjwt 0.11.5)
- Google API client 2.2.0 (for OAuth token verification)
- Cloudinary 1.39.0 (profile image uploads)
- JaCoCo 0.8.13 (test coverage)

To download them ahead of time: `./mvnw dependency:resolve` inside `backend/`

### Frontend libraries (npm handles these)

Same deal, `npm install` grabs everything:

- React 19, React Router 7
- Ant Design 5 (UI components)
- Recharts (dashboard charts)
- `@react-oauth/google` (Google login button)
- Vite 7 (build tool)
- Vitest + Testing Library (tests)
- TypeScript 5.9

---

## How to run it

### Option 1 – Docker (easiest)

This runs the whole app with one command.

**Step 1:** Clone the repo
```bash
git clone <repo-url>
cd group04
```

**Step 2:** Set up your env file
```bash
cp .env.example .env
```

Open `.env` and fill in at minimum:
- `VITE_GOOGLE_CLIENT_ID` – your Google OAuth client ID
- `VITE_API_BASE_URL` – set to `http://localhost:9090/api/auth` for local Docker
- SMTP settings (`SPRING_MAIL_HOST`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, etc.) – these are optional for local testing, if you leave them out confirmation links just print to the console instead of being emailed

**Step 3:** Build and start
```bash
docker compose up --build
```

The app will be at `http://localhost:9090`. Press Ctrl+C to stop.

---

### Option 2 – Run manually (without Docker)

Better for development since you can see logs from each service separately.

**Step 1:** Clone the repo
```bash
git clone <repo-url>
cd group04
```

**Step 2:** Start the backend
```bash
cd backend

# macOS/Linux
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

Backend starts on port 8080. H2 runs automatically, no database setup needed. Test accounts are seeded on startup (see below).

If you want port 9090 instead (to match the frontend default), add this to `backend/src/main/resources/application.properties`:
```properties
server.port=9090
```

**Step 3:** Start the frontend (new terminal)
```bash
cd frontend
npm install
cp .env.example .env
```

Edit `frontend/.env`:
```
VITE_GOOGLE_CLIENT_ID=your_google_client_id
VITE_API_BASE_URL=http://localhost:8080/api/auth
```
(change 8080 to 9090 if you changed the backend port)

```bash
npm run dev
```

Frontend will be at `http://localhost:5173`.

---

### Test accounts

These are created automatically on startup:

| Email | Password | Role |
|-------|----------|------|
| admin@collabry.com | password123 | Admin |
| brand@collabry.com | password123 | Brand |
| influencer@collabry.com | password123 | Influencer |

---

### Real email setup (optional)

By default the app just prints confirmation/reset links to the console log instead of emailing them. If you want real emails:

1. Copy `backend/src/main/resources/application-local.properties.example` → `application-local.properties` (it's gitignored)
2. Fill in your Gmail address and an App Password (not your regular password – generate one at myaccount.google.com/apppasswords)
3. Start the backend with the local profile:

```bash
# Windows
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Dspring.profiles.active=local"

# macOS/Linux
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments=-Dspring.profiles.active=local
```

---

## Running tests

### Backend unit tests
```bash
cd backend
./mvnw test          # macOS/Linux
.\mvnw.cmd test      # Windows
```

### Backend integration tests
Integration tests (files ending in `IT`) run against a full Spring context with H2:
```bash
cd backend
./mvnw verify        # macOS/Linux
.\mvnw.cmd verify    # Windows
```

`mvn verify` runs unit tests first, then integration tests, then generates the JaCoCo coverage report at `backend/target/site/jacoco/index.html`.

For a full list of what each integration test covers, see [docs/INTEGRATION_TESTS.md](docs/INTEGRATION_TESTS.md).

### Frontend tests
```bash
cd frontend
npm test
```

---

## Features and usage

For detailed step-by-step walkthroughs of every feature, see [docs/USAGE_SCENARIOS.md](docs/USAGE_SCENARIOS.md).

Quick summary of what's implemented:

**Brand side:**
- Sign up / log in (email with confirmation link, or Google OAuth)
- Set up brand company profile
- Create campaigns with goal, budget range, niche, and dates
- Manage campaign lifecycle (draft → active → complete / cancel)
- Search influencers by niche, location, follower range, availability
- AI-powered influencer recommendations with match scores via Groq
- Send collaboration invitations, withdraw or edit pending ones
- Negotiate terms (counter-offer flow back and forth)
- Approve submitted deliverables
- Download PDF campaign report
- Record and track milestone payments with invoice download
- Rate and review influencers after a collaboration

**Influencer side:**
- Sign up / log in
- Multi-step profile setup (bio, niche, location, social handles, rate, follower count)
- Upload profile photo (stored on Cloudinary)
- Request verified badge (Blue Checkmark)
- Toggle availability for collaborations
- View and respond to invitations (accept / reject / counter-offer)
- Submit deliverables with a content URL and notes
- Track active collaborations and progress
- View incoming payments and escrow releases

**Admin:**
- View all users and manage accounts (enable/disable/flag)
- Process influencer verification requests (approve/reject with reason)
- Dashboard with summary stats

---

## Design principles

For the full breakdown with code examples and metrics, see [DESIGN_PRINCIPLES.md](DESIGN_PRINCIPLES.md) and [quality/README.md](quality/README.md).

### Architecture

We followed a standard layered architecture throughout:

```
Controllers → Services → Repositories → Models
```

Controllers are intentionally thin and only handle HTTP routing. Business logic lives in services. This made unit testing a lot easier since we can mock the service layer without touching the controller.

### SOLID

**SRP** – Each class has one job. `RatingService` only handles ratings. `GroqApiClient` only talks to the Groq API. `InfluencerSearchRanker` only computes relevance scores. We split out services when we noticed a class was growing too large (e.g. `AuthService` was split into `AuthService`, `RegistrationService`, and `PasswordResetService`).

**OCP** – Campaign and invitation statuses are enums (`CampaignStatus`, `InvitationStatus`, `DeliverableStatus`). Adding a new status means just adding an enum constant, no rewriting of service logic.

**LSP** – Services talk to Spring Data JPA interfaces, not concrete implementations. Spring swaps in H2 locally or MySQL in production without any code changes.

**ISP** – We have separate DTOs for each operation. For example invitations have `RespondRequest` (just accept/reject), `NegotiationRequest` (counter-offer fields), and `UpdateInvitationRequest` (brand edits). Clients never have to send fields they don't use.

**DIP** – All dependencies are constructor-injected. Controllers don't instantiate services themselves, Spring does it. This is what makes mocking work in tests without a lot of setup.

### Cohesion and coupling metrics

We ran DesigniteJava on the backend to get objective numbers (full table in `CodeSmells_Designite/TypeMetrics.csv`):

| Class | Layer | WMC | CBO | LCOM |
|-------|-------|-----|-----|------|
| `CampaignService` | Service | 11 | 5 | 1 |
| `InvitationService` | Service | 16 | 7 | 2 |
| `RatingService` | Service | 4 | 2 | 0 |
| `CampaignController` | Controller | 8 | 3 | 1 |
| `InfluencerProfile` | Model | 32 | 1 | 0 |
| `InfluencerSearchRanker` | Utility | 1 | 0 | 0 |

LCOM is Lack of Cohesion of Methods – lower is better (0 means all methods share the same fields/dependencies). CBO is Coupling Between Objects – how many other classes a class depends on. Our controllers sit at CBO 3 because they only depend on a service and standard HTTP components, which is what we were going for.

---

## TDD

We followed TDD for the image upload (Cloudinary) feature. The commits below show the Red → Green → Refactor cycle:

| Step | Commit | Description |
|------|--------|-------------|
| Red | `d66db57` | `test(image-upload): [TDD Red] add failing tests for CloudinaryService and ImageUploadController` |
| Green | `aaaa49a` | `feat(image-upload): [TDD Green] implement Cloudinary image upload to satisfy all Red tests` |
| Refactor | `4df2ade` | `refactor(image-upload): [TDD Refactor] replace URL text inputs with direct file upload UI` |
| Fix | `345f458` | `test(image-upload): [TDD Fix] correct unauthenticated POST expectation from 401 to 403` |

You can run `git show <hash>` on any of these to see the test-first approach in action.

More broadly, every new service and controller was paired with a `*Test` or `*IT` class. The commit history shows test commits consistently coming alongside or before implementation commits.

---

## Code smells

We used DesigniteJava to scan the codebase and exported reports for architecture, design, and implementation smells. All raw reports are in `CodeSmells_Designite/`. We went through each one and either refactored it or documented why we left it as is – that justification is in the `_Resolved` versions of each CSV:

| File | What it is |
|------|-----------|
| `CodeSmells_Designite/ArchitectureSmells.csv` | architecture smells, raw |
| `CodeSmells_Designite/DesignSmells_Resolved.csv` | design smells with justification column |
| `CodeSmells_Designite/ImplementationSmells_Resolved.csv` | implementation smells with justification column |
| `CodeSmells_Designite/TestSmells_Resolved.csv` | test smells with justification column |
| `CodeSmells_Designite/TypeMetrics.csv` | WMC, CBO, LCOM for all classes |

---

## Troubleshooting

**Blank screen on frontend** – make sure `VITE_GOOGLE_CLIENT_ID` is set in your `.env` file.

**401/403 errors** – check that `VITE_API_BASE_URL` in the frontend `.env` matches the port the backend is actually running on.

**CORS errors** – the backend `ApplicationConfig` needs `http://localhost:5173` as an allowed origin.

**Google login doesn't work** – double check your OAuth client ID in Google Cloud Console, and make sure `http://localhost:5173` is listed as an authorized JavaScript origin.

**Emails not sending** – without SMTP config, the links just print to the console. That's expected in local dev.

---

## Useful links

- Live dev deployment: `http://csci5308-vm2.research.cs.dal.ca:8073`
- Detailed usage walkthroughs: [docs/USAGE_SCENARIOS.md](docs/USAGE_SCENARIOS.md)
- Integration test runbook: [docs/INTEGRATION_TESTS.md](docs/INTEGRATION_TESTS.md)
- Design principles with examples: [DESIGN_PRINCIPLES.md](DESIGN_PRINCIPLES.md)
- CI/CD and deployment: [DEPLOYMENT.md](DEPLOYMENT.md)
