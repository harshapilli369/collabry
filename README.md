# Collabry - Brand & Influencer Collaboration Platform

Collabry is a full-stack web application that connects **brands** with **influencers** for collaborative marketing campaigns. It provides dedicated portals for brands, influencers, and administrators — covering everything from campaign creation and influencer discovery to payments, ratings, and AI-powered content suggestions.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Dependencies](#dependencies)
- [Installation](#installation)
- [Build Instructions](#build-instructions)
- [Deployment Instructions](#deployment-instructions)
- [Usage Scenarios](#usage-scenarios)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)
- [Contact / Support](#contact--support)

---

## Project Overview

### Key Features

- **Authentication & Security** — JWT-based auth, Google OAuth 2.0, email confirmation signup flow, password reset via email tokens
- **Brand Portal** — Create and manage campaigns, search and invite influencers, track collaborations, process payments, rate influencers
- **Influencer Portal** — Browse campaigns, accept/negotiate invitations, submit deliverables, receive payments, view ratings
- **Admin Dashboard** — User management, moderation, platform analytics, verification request handling
- **AI-Powered Features** — Campaign description generation and bio enhancement via Groq LLM API
- **Image Upload** — Cloudinary integration for profile pictures and campaign assets
- **Email Notifications** — Signup confirmation, password reset, payment notifications (SMTP via Gmail)
- **Payments Management** — Track and manage payment statuses (Pending, Paid, Delayed)
- **Ratings & Reviews** — Post-collaboration feedback system for brands and influencers

---

## Technology Stack

| Layer        | Technology                                              |
|--------------|---------------------------------------------------------|
| **Backend**  | Java 17, Spring Boot 3.5, Spring Security, Spring Data JPA |
| **Frontend** | React 19, TypeScript, Vite 7, Ant Design 5             |
| **Database** | H2 (development), MySQL (production)                    |
| **Auth**     | JWT (JJWT 0.11.5), Google OAuth 2.0                    |
| **External** | Cloudinary (images), Groq API (AI), Gmail SMTP (email)  |
| **DevOps**   | Docker, Docker Compose, GitLab CI/CD                    |
| **Testing**  | JUnit 5, Mockito, Vitest, React Testing Library, JaCoCo |

---

## Prerequisites

Install the following before setting up:

| Tool              | Version | Purpose                                  |
|-------------------|---------|------------------------------------------|
| **Java JDK**      | 17+     | Backend (Spring Boot)                    |
| **Maven**         | 3.6+    | Backend build (or use included `./mvnw`) |
| **Node.js**       | 18+     | Frontend (npm, Vite dev server)          |
| **npm**           | 9+      | Frontend dependency management           |
| **Docker**        | 20+     | Containerized deployment (optional)      |
| **Docker Compose**| 2.0+    | Multi-container orchestration (optional) |
| **Git**           | 2.30+   | Version control                          |

### Access Requirements

- **Google Cloud Console** — OAuth 2.0 Client ID for Google login
- **Gmail Account** — With [App Password](https://myaccount.google.com/apppasswords) enabled (requires 2-Step Verification) for sending emails
- **Cloudinary Account** — Free tier for image upload (cloud name, API key, API secret)
- **Groq API Key** — For AI-powered features (optional; falls back gracefully)

---

## Dependencies

### Backend Dependencies (Java / Maven)

All backend dependencies are managed in [`backend/pom.xml`](backend/pom.xml).

| Dependency                        | Version   | Purpose                          |
|-----------------------------------|-----------|----------------------------------|
| `spring-boot-starter-web`         | 3.5.10    | REST API framework               |
| `spring-boot-starter-security`    | 3.5.10    | Authentication & authorization   |
| `spring-boot-starter-data-jpa`    | 3.5.10    | Database ORM (Hibernate)         |
| `spring-boot-starter-validation`  | 3.5.10    | Request validation               |
| `spring-boot-starter-mail`        | 3.5.10    | SMTP email sending               |
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` | 0.11.5 | JWT token generation & parsing |
| `h2`                              | (managed) | In-memory database (dev/test)    |
| `mysql-connector-j`               | (managed) | MySQL driver (production)        |
| `google-api-client`               | 2.2.0     | Google OAuth token verification  |
| `cloudinary-http5`                | 1.39.0    | Image upload to Cloudinary       |
| `jacoco-maven-plugin`             | 0.8.12    | Code coverage reporting          |
| `maven-failsafe-plugin`           | 3.5.2     | Integration test execution       |

Install backend dependencies:

```bash
cd backend
./mvnw dependency:resolve       # Linux/macOS
.\mvnw.cmd dependency:resolve   # Windows
```

### Frontend Dependencies (Node.js / npm)

All frontend dependencies are managed in [`frontend/package.json`](frontend/package.json).

| Dependency                  | Version  | Purpose                        |
|-----------------------------|----------|--------------------------------|
| `react`                     | 19.2.0   | UI library                     |
| `react-dom`                 | 19.2.0   | React DOM rendering            |
| `react-router`              | 7.13.0   | Client-side routing            |
| `antd`                      | 5.29.3   | UI component library           |
| `@ant-design/icons`         | 5.4.0    | Icon set                       |
| `@react-oauth/google`       | 0.12.1   | Google OAuth integration       |
| `recharts`                  | 3.8.1    | Data visualization / charts    |
| `axios`                     | 1.9.0    | HTTP client for API calls      |

**Dev Dependencies:**

| Dependency                        | Version  | Purpose                  |
|-----------------------------------|----------|--------------------------|
| `typescript`                      | 5.9.3    | Type checking            |
| `vite`                            | 7.0.0    | Build tool & dev server  |
| `vitest`                          | 3.2.4    | Unit testing framework   |
| `@testing-library/react`         | 16.3.0   | Component testing        |
| `@testing-library/jest-dom`      | 6.6.3    | DOM assertions           |
| `eslint`                          | 9.28.0   | Code linting             |

Install frontend dependencies:

```bash
cd frontend
npm install
```

---

## Installation

### Option 1: Docker Setup (Recommended)

This is the easiest way to run the full application with a single command.

1. **Clone the repository:**

   ```bash
   git clone <repository-url>
   cd group04
   ```

2. **Create the environment file:**

   ```bash
   cp .env.example .env
   ```

3. **Edit `.env`** and set your values (see [Configuration](#configuration) for details).

4. **Start the application:**

   ```bash
   docker compose up --build
   ```

   - **Backend:** http://localhost:9090
   - **Frontend:** http://localhost:5173

   Press `Ctrl+C` to stop. Use `--build` only on the first run or after code changes.

### Option 2: Manual Setup

Run backend and frontend in two separate terminals.

**Terminal 1 — Backend:**

```bash
cd backend

# Linux/macOS
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

The API runs on **http://localhost:9090** by default.

**Terminal 2 — Frontend:**

```bash
cd frontend
cp .env.example .env    # Edit .env with your values
npm install
npm run dev
```

The dev server runs on **http://localhost:5173**.

### Seed Data

On startup, the backend automatically seeds demo data (admin, brand, and influencer accounts) via `DatabaseSeeder` and `DataInitializer`. Default seed accounts:

| Email                    | Role        | Password     |
|--------------------------|-------------|--------------|
| `admin@collabry.com`     | Admin       | `password123`|
| `brand@collabry.com`     | Brand       | `password123`|
| `influencer@collabry.com`| Influencer  | `password123`|

---

## Build Instructions

### Backend Build

```bash
cd backend

# Compile only
./mvnw compile

# Package as JAR (skip tests for faster build)
./mvnw package -DskipTests

# Full build with tests and coverage
./mvnw verify
```

**Build artifacts:**
- JAR file: `backend/target/backend-0.0.1-SNAPSHOT.jar`
- JaCoCo report: `backend/target/site/jacoco/index.html`

### Frontend Build

```bash
cd frontend

# Development build (with hot reload)
npm run dev

# Production build
npm run build
```

**Build artifacts:**
- Production bundle: `frontend/dist/`

### Production Docker Image (Multi-stage)

The root [`Dockerfile`](Dockerfile) builds a single production image that bundles the React frontend into Spring Boot's static resources:

```bash
docker build \
  --build-arg VITE_API_BASE_URL=http://your-server:8073/api/auth \
  --build-arg VITE_GOOGLE_CLIENT_ID=your-client-id \
  -t collabry:latest \
  .
```

The resulting image exposes port **8073** and serves both the API and frontend from a single container.

---

## Deployment Instructions

### CI/CD Pipeline (GitLab)

The project uses GitLab CI/CD with the following stages:

| Stage       | Jobs                  | Description                              |
|-------------|-----------------------|------------------------------------------|
| **build**   | build-backend, build-frontend | Maven compile, Docker npm build    |
| **test**    | test                  | Maven verify with JaCoCo coverage        |
| **publish** | publish               | Docker build & push to Docker Hub        |
| **deploy**  | deploy-dev / deploy-prod | SSH deploy to server                  |

### Branch Strategy

| Branch      | Image Tag         | Container    | Environment |
|-------------|-------------------|--------------|-------------|
| `develop`   | `dev-latest`      | `my-app-dev` | Development |
| `main`      | `latest`          | `my-app`     | Production  |

Both environments deploy to **port 8073** on the server.

### Required CI/CD Variables

Configure these in **GitLab > Settings > CI/CD > Variables**:

| Variable                 | Masked | Description                                |
|--------------------------|--------|--------------------------------------------|
| `DOCKERHUB_USER`         | No     | Docker Hub username                        |
| `DOCKERHUB_PASSWORD`     | Yes    | Docker Hub token/password                  |
| `SERVER_IP`              | No     | Deployment server address                  |
| `SERVER_USER`            | No     | SSH user on the server                     |
| `ID_RSA`                 | File   | SSH private key for server access          |
| `VITE_GOOGLE_CLIENT_ID`  | No     | Google OAuth client ID                     |
| `SPRING_MAIL_HOST`       | No     | SMTP host (e.g. `smtp.gmail.com`)         |
| `SPRING_MAIL_PORT`       | No     | SMTP port (`465` for SSL)                 |
| `SPRING_MAIL_USERNAME`   | Yes    | SMTP email address                         |
| `SPRING_MAIL_PASSWORD`   | Yes    | Gmail App Password (16 chars, no spaces)   |
| `APP_MAIL_FROM`          | No     | Sender email address                       |
| `GROQ_API_KEY`           | Yes    | Groq API key for AI features               |
| `CLOUDINARY_CLOUD_NAME`  | No     | Cloudinary cloud name                      |
| `CLOUDINARY_API_KEY`     | Yes    | Cloudinary API key                         |
| `CLOUDINARY_API_SECRET`  | Yes    | Cloudinary API secret                      |
| `DB_PASSWORD`            | Yes    | MySQL database password                    |

### Manual Deployment

```bash
# Build production image
docker build \
  --build-arg VITE_API_BASE_URL=http://your-server:8073/api/auth \
  --build-arg VITE_GOOGLE_CLIENT_ID=your-client-id \
  -t collabry:latest .

# Run the container
docker run -d \
  --name my-app \
  -p 8073:8073 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://db-host:3306/CSCI5308_5_PRODUCTION \
  -e SPRING_DATASOURCE_USERNAME=CSCI5308_5_PRODUCTION_USER \
  -e SPRING_DATASOURCE_PASSWORD=<db-password> \
  -e SPRING_MAIL_HOST=smtp.gmail.com \
  -e SPRING_MAIL_PORT=465 \
  -e SPRING_MAIL_USERNAME=your-email@gmail.com \
  -e SPRING_MAIL_PASSWORD=<app-password> \
  collabry:latest
```

### Post-Deployment Verification

```bash
# Check container is running
docker ps | grep my-app

# View logs
docker logs my-app

# Health check
curl http://your-server:8073/api/auth/health
```

### Rollback

```bash
# Stop current container
docker stop my-app && docker rm my-app

# Run previous image version
docker run -d --name my-app -p 8073:8073 <previous-image-tag>
```

---

## Usage Scenarios

[View Usage Scenarios](./USAGE.md)

---

## Project Structure

```
group04/
├── backend/                        # Spring Boot REST API
│   ├── pom.xml                     # Maven dependencies & build config
│   ├── mvnw / mvnw.cmd            # Maven wrapper scripts
│   ├── Dockerfile                  # Backend-only Docker image
│   └── src/
│       ├── main/java/com/group4/backend/
│       │   ├── BackendApplication.java   # Application entry point
│       │   ├── config/                   # Security, CORS, mail, Cloudinary, data init
│       │   ├── controller/               # REST controllers (Auth, Campaign, Brand, etc.)
│       │   ├── dto/                      # Request/response data transfer objects
│       │   ├── model/                    # JPA entities & enums
│       │   ├── repository/              # Spring Data JPA repositories
│       │   ├── security/                # JWT filter & utilities
│       │   └── service/                 # Business logic services
│       ├── main/resources/
│       │   └── application.properties   # App configuration
│       └── test/                        # Unit & integration tests
│
├── frontend/                       # React + Vite + TypeScript
│   ├── package.json                # npm dependencies & scripts
│   ├── vite.config.ts              # Vite build configuration
│   ├── tsconfig.json               # TypeScript configuration
│   ├── Dockerfile                  # Frontend-only Docker image
│   ├── .env.example                # Environment variable template
│   └── src/
│       ├── main.tsx                # Application entry point
│       ├── App.tsx                 # Root component with routing
│       ├── pages/                  # Page components (Login, Dashboards, etc.)
│       ├── components/             # Reusable UI components
│       ├── services/               # API client services (auth, campaign, etc.)
│       ├── constants/              # Shared constants
│       └── content/                # Static content (guidelines)
│
├── docs/                           # Documentation & diagrams
│   ├── INTEGRATION_TESTS.md        # Integration test conventions
│   └── diagrams/                   # Architecture diagrams
│
├── docker-compose.yml              # Multi-container dev setup
├── Dockerfile                      # Production multi-stage build
├── .env.example                    # Root environment template
├── .gitlab-ci.yml                  # CI/CD pipeline configuration
├── DEPLOYMENT.md                   # Deployment guide
├── DESIGN_PRINCIPLES.md            # Architecture & design patterns
└── SETUP.md                        # Quick developer setup checklist
```

---

## Configuration

### Environment Variables

Copy `.env.example` to `.env` in the project root and configure:

```env
# Google OAuth
VITE_GOOGLE_CLIENT_ID=your_google_client_id

# Backend API URL
VITE_API_BASE_URL=http://localhost:9090/api/auth

# SMTP Email (Gmail with App Password)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=465
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_16_char_app_password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_ENABLE=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_TRUST=*
APP_MAIL_FROM=your_email@gmail.com

# Cloudinary Image Upload
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

### Backend Configuration

The main config file is [`backend/src/main/resources/application.properties`](backend/src/main/resources/application.properties):

| Property                          | Default                          | Description                    |
|-----------------------------------|----------------------------------|--------------------------------|
| `SERVER_PORT`                     | `9090`                           | Backend server port            |
| `SPRING_DATASOURCE_URL`          | `jdbc:h2:file:./data/collabry`   | Database connection URL        |
| `SPRING_DATASOURCE_USERNAME`     | `sa`                             | Database username              |
| `SPRING_DATASOURCE_PASSWORD`     | `password`                       | Database password              |
| `spring.jpa.hibernate.ddl-auto`  | `update`                         | Schema management strategy     |
| `GROQ_API_KEY`                   | `dummy`                          | Groq LLM API key              |
| `spring.servlet.multipart.max-file-size` | `10MB`                   | Max upload size                |

### Email Configuration

Without SMTP config, emails are logged to the console. To send real emails:

1. Enable 2-Step Verification on your Gmail account
2. Generate an [App Password](https://myaccount.google.com/apppasswords)
3. Set the `SPRING_MAIL_*` variables in your `.env` file

### Database

- **Development:** H2 file-based database at `./data/collabry` (zero setup required)
- **Production:** MySQL — set `SPRING_DATASOURCE_URL` to your MySQL connection string
- **Schema:** Managed automatically by Hibernate (`ddl-auto=update`)

---

## Testing

### Backend Tests

```bash
cd backend

# Unit tests only
./mvnw test

# Unit + integration tests with coverage
./mvnw verify
```

- **Unit tests:** Run by Maven Surefire (classes named `*Test`)
- **Integration tests:** Run by Maven Failsafe (classes named `*IT` or `*ITCase`)
- **Coverage report:** `backend/target/site/jacoco/index.html`

Integration tests use H2 in-memory database with `ddl-auto=create-drop` and auto-seeded test data. See [`docs/INTEGRATION_TESTS.md`](docs/INTEGRATION_TESTS.md) for conventions and test catalog.

### Frontend Tests

```bash
cd frontend

# Watch mode
npm run test

# Single run
npm run test:run
```

Tests use Vitest with React Testing Library and jsdom environment.

---

## Contributing

1. Create a feature branch from `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```

2. Make your changes and write tests.

3. Run the test suites:
   ```bash
   cd backend && ./mvnw verify
   cd ../frontend && npm run test:run
   ```

4. Push and create a merge request targeting `develop`.

5. After review and approval, merge into `develop`. Production releases are merged from `develop` into `main`.

---

## Troubleshooting

| Issue                    | Solution                                                                                  |
|--------------------------|-------------------------------------------------------------------------------------------|
| Blank screen             | Ensure `.env` exists in `frontend/` with `VITE_GOOGLE_CLIENT_ID` and `VITE_API_BASE_URL` |
| Login / API errors       | Verify backend is running and `VITE_API_BASE_URL` matches the backend port                |
| CORS errors              | Check that `SecurityConfig` allows the frontend origin (`http://localhost:5173`)           |
| Google login fails       | Verify OAuth Client ID, authorized JavaScript origins, and redirect URIs in Google Console |
| No confirmation emails   | Set `SPRING_MAIL_*` variables; without them, links are only logged to console              |
| Docker build fails       | Check Maven/npm logs; ensure Docker has sufficient memory (4GB+ recommended)               |
| Deploy fails (SSH)       | Verify `ID_RSA` has correct permissions and is in server's `authorized_keys`               |

---

## License

This project was developed as part of the CSCI 5308 course at Dalhousie University.

---

## Contact / Support

For questions or issues related to this project:

- Open an issue in the GitLab repository
- Reach out to Group 4 team members via the course communication channels
- Check existing documentation: [`SETUP.md`](SETUP.md), [`DEPLOYMENT.md`](DEPLOYMENT.md), [`DESIGN_PRINCIPLES.md`](DESIGN_PRINCIPLES.md)
