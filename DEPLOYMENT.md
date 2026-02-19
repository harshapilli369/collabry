# Deployment Guide – Development and Production

- **`develop` branch**: Full CI/CD (build, test, publish, deploy) → dev server at **port 8073** (container `my-app-dev`, image tag `dev-latest` / `dev-<SHA>`). Uses same port as prod for firewall compatibility.
- **`main` branch**: Full CI/CD → production at **port 8073** (container `my-app`, image tag `latest` / `<SHA>`).

## 1. Working with the develop branch

Push or merge to `develop` to run the pipeline and deploy to the dev environment:

```bash
git checkout develop
git pull origin develop
# merge your feature branch or push commits
git push origin develop
```

- **Dev URL**: http://csci5308-vm2.research.cs.dal.ca:8073  
- **Prod URL**: http://csci5308-vm2.research.cs.dal.ca:8073  

## 2. Consolidate work into main (for demo)

Merge your feature branch into `main`:

```bash
# Ensure you're on main and up to date
git checkout main
git pull origin main

# Merge your feature branch (e.g. influencer profile work)
git merge feature/Influencer-Onboarding-Profile

# Resolve any conflicts, then push
git push origin main
```

To squash commits on merge (keeps history clean):

```bash
git checkout main
git pull origin main
git merge --squash feature/Influencer-Onboarding-Profile
git commit -m "feat: influencer profile setup and onboarding flow"
git push origin main
```

## 3. GitLab CI/CD variables

Configure these in **Settings → CI/CD → Variables** (mask sensitive ones):

| Variable | Type | Masked | Description |
|----------|------|--------|-------------|
| `DOCKERHUB_PASSWORD` | Variable | Yes | Docker Hub token (or password) |
| `DOCKERHUB_USER` | Variable | No | Docker Hub username (default: hpilli369) |
| `SERVER_IP` | Variable | No | Production server IP (e.g. csci5308-vm2.research.cs.dal.ca) |
| `SERVER_USER` | Variable | No | SSH user on production server |
| `ID_RSA` | File | No | SSH private key file for server access |
| `VITE_API_BASE_URL` | Variable | No | Optional. e.g. `http://csci5308-vm2.research.cs.dal.ca:8073/api/auth` |
| `VITE_GOOGLE_CLIENT_ID` | Variable | No | Optional. Google OAuth client ID for production |
| `VITE_API_BASE_URL_DEV` | Variable | No | Optional. Dev API base (default: `http://csci5308-vm2.research.cs.dal.ca:8073/api/auth`) |
| `SPRING_MAIL_HOST` | Variable | No | **Required for real emails.** SMTP host (e.g. `smtp.gmail.com`) |
| `SPRING_MAIL_PORT` | Variable | No | SMTP port (default: 587 for STARTTLS; use 465 for SSL) |
| `SPRING_MAIL_USERNAME` | Variable | Yes | SMTP username (e.g. your Gmail address) |
| `SPRING_MAIL_PASSWORD` | Variable | Yes | SMTP password (for Gmail: use an [App Password](https://support.google.com/accounts/answer/185833), not your regular password) |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_ENABLE` | Variable | No | Set to `true` if using Gmail port 465 (SSL). Omit for port 587 (STARTTLS). |

## 4. CI/CD pipeline

- **`develop`**: Build → Test → Publish (image tag `dev-latest`) → Deploy to port **8073** (container `my-app-dev`).
- **`main`**: Build → Test → Publish (image tag `latest`) → Deploy to port **8073** (container `my-app`).

Merge requests targeting either branch run build and test only.

## 5. URLs after deploy

- **Development**: http://csci5308-vm2.research.cs.dal.ca:8073  
- **Production**: http://csci5308-vm2.research.cs.dal.ca:8073  

The container serves both the React frontend and Spring Boot API from the same URL.

## 6. Local production build (optional)

```bash
# Build the production image locally
docker build \
  --build-arg VITE_API_BASE_URL=http://csci5308-vm2.research.cs.dal.ca:8073/api/auth \
  --build-arg VITE_GOOGLE_CLIENT_ID=your-client-id \
  -t group04:local \
  .

# Run it
docker run -p 8073:8073 group04:local
```

## 7. Troubleshooting

- **Build fails**: Check backend Maven logs and frontend npm build logs in the GitLab job output.
- **Deploy fails**: Confirm `ID_RSA` has correct permissions and the key is added to the server’s `authorized_keys`.
- **502 / app not loading**: Check `docker ps` on the server; production: `docker logs my-app`, dev: `docker logs my-app-dev`.
- **No confirmation emails received**: Add `SPRING_MAIL_HOST`, `SPRING_MAIL_USERNAME`, and `SPRING_MAIL_PASSWORD` (Gmail App Password) in CI/CD Variables. Without these, emails are only logged to the container console. For Gmail: enable 2-Step Verification, then create an App Password at https://myaccount.google.com/apppasswords.
