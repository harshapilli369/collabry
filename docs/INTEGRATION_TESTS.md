# Backend integration tests

This document describes the **API integration tests** for the Collabry backend (`backend/`). These tests exercise the Spring Boot application end-to-end over HTTP: security filters, controllers, services, JPA, and an in-memory **H2** database (see `src/test/resources/application.properties`).

For **unit and slice tests** (`*Test`, `*Tests`), see the `src/test/java` tree and run `mvn test`. Integration tests are separate (see below).

---

## How they run

| Where | Command | What runs |
|--------|---------|-----------|
| **Local** | `cd backend && ./mvnw verify` (Windows: `mvnw.cmd verify`) | Unit tests (Surefire) **then** integration tests (Failsafe) **then** JaCoCo on `verify` |
| **CI** | GitLab job **`test`** (`.gitlab-ci.yml`) | Same: `./mvnw verify` in `backend/` |

Integration tests are picked up by the **Maven Failsafe** plugin. Class names must end with **`IT`** (or `ITCase`). Surefire is configured to **exclude** `*IT` so those classes are not run twice.

---

## Conventions

- **Package:** `com.group4.backend.integration`
- **Annotations:** `@SpringBootTest` + `@AutoConfigureMockMvc` (full application context, `MockMvc` HTTP calls)
- **Database:** H2 in-memory, `ddl-auto=create-drop` for tests
- **Seed data:** `DataInitializer` runs on startup and creates fixed users used by many tests (see [Seed users](#seed-users))
- **External services:** Groq is not called in CI by default; some tests assert the **“not configured”** behavior, and **`GroqAiMockedIT`** uses `@MockBean` on `GroqApiClient` for happy-path coverage without network calls

---

## Seed users

Many tests log in with these accounts (password **`password123`** unless noted):

| Email | Role | Notes |
|--------|------|--------|
| `admin@collabry.com` | `ADMIN` | Admin API tests |
| `brand@collabry.com` | `BRAND` | Campaigns, payments, invitations, brand profile |
| `influencer@collabry.com` | `INFLUENCER` | Invitations, collaborations, influencer profile, payments as payee |

---

## Test catalog

### Authentication

| Class | What it covers |
|--------|----------------|
| **`AuthApiIT`** | `POST /api/auth/register` — success (pending signup persisted), validation error **400**, duplicate email **409** (seed brand email). |
| **`AuthLoginIT`** | `POST /api/auth/login` — valid credentials return JWT; wrong password **401** with message. |
| **`AuthEmailConfirmIT`** | Register → read confirmation token (via JPA) → `GET /api/auth/confirm-email` → user created, pending removed, login works; invalid token **400**. |

### Users & JWT

| Class | What it covers |
|--------|----------------|
| **`UserInfluencersIT`** | `GET /api/users/influencers` — without token **403**; with **brand** JWT **200** and JSON array. |

### Campaigns & invitations (core flow)

| Class | What it covers |
|--------|----------------|
| **`CampaignFlowIT`** | Brand JWT: `POST /api/campaigns`, `GET /api/campaigns/me`, `POST .../invitations` to seed influencer. |
| **`InvitationLifecycleIT`** | Reject (`REJECT`); negotiate → **NEGOTIATING** → brand `confirm-terms` → **CONFIRMED**; brand **withdraw** pending invite; brand **PUT** update pending invite; influencer **GET** invitation detail; brand **403** on influencer invitation list. |

### Collaborations & deliverables

| Class | What it covers |
|--------|----------------|
| **`CollaborationDeliverableIT`** | Brand campaign + invite → influencer **ACCEPT** → influencer **PUT** deliverable (**SUBMITTED** + link) → brand **approve** → **APPROVED**; influencer **`GET /api/collaborations/me`** includes the collaboration. |

### Influencer profile & search

| Class | What it covers |
|--------|----------------|
| **`InfluencerProfileSearchIT`** | Influencer **PUT/GET** `/api/influencers/me` (complete profile including **rate**); brand **GET** `/api/influencers/search` by niche; **collaboration-availability** toggle + `availableOnly` filter; invalid follower range **400**. |

### Brand profile

| Class | What it covers |
|--------|----------------|
| **`BrandProfileIT`** | Brand **PUT/GET** `/api/brands/me`; **GET** `/api/brands/{userId}/profile` with authenticated caller (influencer JWT); influencer **403** on brand `/me` routes. |

### User verification & social

| Class | What it covers |
|--------|----------------|
| **`UserVerificationSocialIT`** | Influencer **POST** `/api/users/me/verification/request` → **PENDING**, **GET** status, duplicate request **400**; **PUT** `/api/users/me/link-social` (**INSTAGRAM**) reflected on **GET** `/api/influencers/me`; empty body **400**. |

### AI / Groq

| Class | What it covers |
|--------|----------------|
| **`GroqAiNotConfiguredIT`** | With default test key (`dummy`): `POST /api/campaigns/generate-description` and `POST /api/influencers/enhance-bio` return **400** “AI service is not configured”. |
| **`GroqAiMockedIT`** | `@MockBean GroqApiClient`: stubbed `getTextCompletion` — both endpoints return **200** with `description` / `enhancedBio` (no real Groq HTTP). |

### Admin

| Class | What it covers |
|--------|----------------|
| **`AdminApiIT`** | Admin: **GET** `/api/admin/dashboard`, **GET** `/api/admin/users`; brand **403** on those; admin **PUT** `/api/admin/users/{id}` for seed influencer (`active` / `flagged`). |

### Payments

| Class | What it covers |
|--------|----------------|
| **`PaymentFlowIT`** | Brand: create campaign → **POST** `/api/payments` → **GET** by campaign → **PUT** status **PAID** → influencer **GET** `/api/payments/me` → brand **GET** invoice; influencer **POST** payment **403**. |
| **`PaymentEdgeCasesIT`** | Past-due **PENDING** payment appears in **GET** `/api/payments/delayed` as **DELAYED**; admin **GET** invoice **400** (no access); **PUT** status with missing or invalid `status` **400**. |

### Ratings

| Class | What it covers |
|--------|----------------|
| **`RatingFlowIT`** | Brand campaign + invite → influencer **ACCEPT** → brand **POST** `/api/ratings` → **201**; influencer **POST** ratings **403**. |

---

## Running only integration tests

From `backend/`, skip **Surefire** (unit tests) but still run **Failsafe** on `verify`:

```bash
./mvnw verify -Dsurefire.skip=true
```

Alternatively, after a normal compile/test-compile:

```bash
./mvnw failsafe:integration-test failsafe:verify
```

To run a **single** class:

```bash
./mvnw verify -Dit.test=AuthApiIT
```

(Use PowerShell quoting on Windows: `.\mvnw.cmd verify "-Dit.test=AuthApiIT"`.)

---

## Scope and limitations

- **MySQL** is not used in these tests; production uses MySQL — rare SQL/dialect differences are possible.
- **SMTP**, **Cloudinary**, and **real Groq** are not required for CI; email uses **console** implementation when `spring.mail.host` is unset.
- **Frontend** and **browser E2E** are out of scope here; this document covers **backend** `*IT` tests only.

---

## Related files

| File | Role |
|------|------|
| `backend/pom.xml` | Surefire excludes `*IT`; Failsafe runs them; JaCoCo `argLine` shared |
| `backend/src/test/resources/application.properties` | H2 test datasource |
| `.gitlab-ci.yml` | `test` job → `mvn verify` |

---

*Last updated to match integration tests under `backend/src/test/java/com/group4/backend/integration/`.*
