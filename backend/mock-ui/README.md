# Backend Mock UI

Simple HTML page to **manually verify** the Collabry backend API (influencer search, invitations, withdraw, edit).

## How to use

1. **Start the backend** (from repo root `backend/`):
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
   Default port is **9090** (see `application.properties`). If you use another port, set it in the mock UI.

2. **Open the mock UI** in a browser:
   - Open `index.html` directly (e.g. double-click or `file:///path/to/backend/mock-ui/index.html`), or
   - Serve it with any static server (e.g. `npx serve backend/mock-ui`).

3. **Set API base URL** in the first section (e.g. `http://localhost:9090` if backend runs on 9090).

4. **Log in** with a **brand** user (email + password). This calls `POST /api/auth/login` and stores the JWT. All other requests use this token.

5. **Use the sections** to:
   - **Search influencers** – `GET /api/influencers/search` with optional filters.
   - **Load sent invitations** – `GET /api/invitations/sent`.
   - **Send invitation** – `POST /api/campaigns/:campaignId/invitations` with campaign details (deliverables, timeline, budget, platform, expiresInDays).
   - **Withdraw** – `DELETE /api/invitations/:id` (only PENDING/NEGOTIATING).
   - **Edit invitation** – `PUT /api/invitations/:id` (only PENDING/NEGOTIATING).

Responses are shown as JSON below each section. Use this to confirm status codes, response bodies, and error messages so the backend works as expected.
