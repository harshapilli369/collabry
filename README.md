Group 4 – Brand & Influencer Platform

This repository is the Collabry  platform: a full-stack application that connects brands with influencers. It includes a Spring Boot backend and a React (Vite) frontend, with authentication, Google OAuth, and password reset as part of the core setup.

---

About the project

Backend: Spring Boot 3.5, Java 17 — REST API with JWT, Google OAuth, H2 (in-memory DB for development).
- Frontend: React 19, TypeScript, Vite 7, Ant Design, React Router.

The project is organized as a monorepo: `backend/` for the API and `frontend/` for the web app. Use this README to get the **entire project** running on your machine, regardless of branch.

---

Prerequisites

Install the following before setting up:

| Tool       | Version | Purpose                      |
|------------|---------|------------------------------|
| **Node.js** | v18+    | Frontend (npm, Vite)         |
| **Java JDK** | 17+   | Backend (Spring Boot)        |
| **Maven**  | 3.6+    | Backend build (or use `./mvnw`) |

---

Project structure

```
group04/
├── backend/          # Spring Boot API
│   ├── src/main/java/com/group4/backend/
│   │   ├── config/       # Security, CORS, data init
│   │   ├── controller/   # AuthController
│   │   ├── dto/          # LoginRequest, AuthResponse, etc.
│   │   ├── model/        # User, Role, PasswordResetToken
│   │   ├── repository/   # User, PasswordResetToken
│   │   ├── security/     # JWT filter & utils
│   │   └── service/      # AuthService
│   └── src/main/resources/
│       └── application.properties
├── frontend/         # React + Vite + TypeScript
│   ├── src/
│   │   ├── pages/        # Login, ForgotPassword, ResetPassword, etc.
│   │   └── services/     # authService and API clients
│   └── .env.example     # Copy to .env and configure
├── README.md          # This file
└── SETUP.md           # Short setup checklist
```

---

Developer setup (entire project)

1. Clone the repository

```bash
git clone <repository-url>
cd group04
```

### 2. Docker Setup (Recommended)
This is the easiest way to run the application. It sets up both backend and frontend with a single command. **All configuration and secrets are read from a root `.env` file**; nothing is hardcoded in `docker-compose.yml`.

1.  **Create the environment file**:
    In the **root** folder (`group04/`), copy the example file and edit it with your values:
    ```bash
    cp .env.example .env
    ```
    Then open `.env` and set at least:
    - **VITE_GOOGLE_CLIENT_ID** — Your Google OAuth client ID (for frontend login).
    - **VITE_API_BASE_URL** — Keep `http://localhost:9090/api/auth` when running Docker on your machine.
    - **SMTP / Gmail** — Required for signup confirmation and password-reset emails. Use your Gmail address and a [Gmail App Password](https://myaccount.google.com/apppasswords) (16 characters, no spaces; enable 2-Step Verification first):
      - `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, `APP_MAIL_FROM`, and the other `SPRING_MAIL_*` variables as in `.env.example`.

    The full list of variables is in **`.env.example`** in the project root. Do not commit `.env` to Git.

2.  **Run with Docker Compose**:
    ```bash
    docker compose up --build
    ```
    *(Or `docker-compose up --build` depending on your setup. The `--build` flag is only needed the first time or after pulling new code.)*

    - **Backend**: Runs on `http://localhost:9090`
    - **Frontend**: Runs on `http://localhost:5173`

    *(To stop the app, press `Ctrl+C`)*

---

## Developer setup (Manual)

If you prefer to run services manually (without Docker):

1. Clone the repository...


The API runs on **port 8080** by default (see `backend/src/main/resources/application.properties`). The frontend `.env.example` may use **port 9090**; either set the backend port to 9090 or set `VITE_API_BASE_URL` in the frontend to match your backend port.

1. Go to the backend folder and run:

   **Windows (PowerShell / CMD):**
   ```bash
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

   **macOS / Linux:**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

2. Optional: to use port 9090 (to match frontend default), in `backend/src/main/resources/application.properties` set:
   ```properties
   server.port=9090
   ```

3. H2 in-memory DB and JPA are already configured; no extra database setup is required for local development.

### 3. Frontend (React + Vite)

1. Install dependencies and run the dev server:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

2. Create a local env file (required for Google login and API URL):

   ```bash
   cp .env.example .env
   ```

3. Edit `.env` and set:

   - **VITE_GOOGLE_CLIENT_ID** — Your Google OAuth client ID (from Google Cloud Console).
   - **VITE_API_BASE_URL** — Backend auth base URL, e.g.:
     - If backend is on **8080:** `http://localhost:8080/api/auth`
     - If backend is on **9090:** `http://localhost:9090/api/auth`

   Example:

   ```env
   VITE_GOOGLE_CLIENT_ID=your_google_client_id
   VITE_API_BASE_URL=http://localhost:8080/api/auth
   ```

4. Open the URL shown in the terminal (typically `http://localhost:5173`).

---

## Running the full project

Use two terminals:

1. **Terminal 1 – Backend**
   ```bash
   cd backend
   .\mvnw.cmd spring-boot:run   # Windows
   # or: ./mvnw spring-boot:run  # macOS/Linux
   ```

2. **Terminal 2 – Frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

Then open the frontend URL (e.g. `http://localhost:5173`) in your browser.

---

## Sending real emails (signup confirmation)

Signup is **confirm-by-email**: the user is **not** saved in the database until they click the confirmation link in the email. Until then, their signup is stored as a pending record (expires in 24 hours).

By default, the backend **logs** the confirmation link to the console (no SMTP). To send **real emails** to users, use a **local config file** (not committed) so your password never goes into Git:

1. In **backend** `src/main/resources/`, copy the example file:
   - Copy **application-local.properties.example** to **application-local.properties**
   - `application-local.properties` is in `.gitignore` and will not be committed.

2. Edit **application-local.properties** and set your SMTP values (e.g. Gmail address and [App Password](https://support.google.com/accounts/answer/185833)). The example file lists all needed keys.

3. Run the backend with the **local** profile so Spring loads that file (PowerShell):
   ```bash
   cd backend
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Dspring.profiles.active=local"
   ```
   Or set `SPRING_PROFILES_ACTIVE=local` in your environment or IDE run config before starting the app.

4. As soon as **spring.mail.host** is set (via the local file), the app uses **SmtpEmailService** and sends real emails. Without it, **ConsoleEmailService** is used and the link is only printed to the console.

In production, use environment variables or a secrets manager for the password; do not commit `application-local.properties`.

---

## Troubleshooting

| Issue | What to check |
|-------|----------------|
| Blank screen | Ensure `.env` exists in `frontend/` with `VITE_GOOGLE_CLIENT_ID` and `VITE_API_BASE_URL`. |
| Login / API errors | Backend must be running. Confirm `VITE_API_BASE_URL` matches the backend port (8080 or 9090). |
| CORS errors | Backend `ApplicationConfig` / `SecurityConfig` should allow the frontend origin (e.g. `http://localhost:5173`). |
| Google login fails | Verify Google Cloud OAuth client ID, authorized JavaScript origins, and redirect URIs. |

---

## Quick reference

- **Docker:** All config and secrets come from the root **`.env`** file. Copy `.env.example` to `.env` and fill in your values before `docker compose up --build`.
- **Backend port:** 9090 (Docker) or 8080 (local; configurable in `application.properties`)
- **Frontend dev server:** usually `http://localhost:5173`
- **Auth API base:** `http://localhost:<backend-port>/api/auth`
- **More detail:** see `SETUP.md`

This README describes how to set up and run the **entire project** so any developer can get the backend and frontend running on their machine.
