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
2. Backend (Spring Boot)

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

By default, the backend **logs** confirmation emails to the console (no SMTP). To send real emails to users:

1. In **backend** `src/main/resources/application.properties` (or via environment variables), set:
   - **spring.mail.host** – e.g. `smtp.gmail.com`, `smtp.sendgrid.net`, or your provider’s SMTP host
   - **spring.mail.port** – usually `587` (TLS) or `465` (SSL)
   - **spring.mail.username** – your sending account (e.g. Gmail address)
   - **spring.mail.password** – app password or SMTP password (never commit this; use env vars in production)
   - **spring.mail.properties.mail.smtp.auth=true** and **spring.mail.properties.mail.smtp.starttls.enable=true** for TLS
   - **app.mail.from** (optional) – sender address shown in the email (defaults to `spring.mail.username`)

2. As soon as **spring.mail.host** is set, the app uses **SmtpEmailService** and sends real emails. If **spring.mail.host** is not set, **ConsoleEmailService** is used and messages are only printed to the backend console.

Example for Gmail (use an [App Password](https://support.google.com/accounts/answer/185833), not your normal password):

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=yourname@gmail.com
spring.mail.password=your-16-char-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.mail.from=yourname@gmail.com
```

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

- **Backend port:** 8080 (configurable in `application.properties`)
- **Frontend dev server:** usually `http://localhost:5173`
- **Auth API base:** `http://localhost:<backend-port>/api/auth`
- **More detail:** see `SETUP.md`

This README describes how to set up and run the **entire project** so any developer can get the backend and frontend running on their machine.
