# Deployment Guide – Production from main

This guide covers deploying Collabry to production from the `main` branch and consolidating work for demo.

## 1. Consolidate work into main (for demo)

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

## 2. GitLab CI/CD variables

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

## 3. CI/CD pipeline (main branch)

On push to `main`, the pipeline runs:

1. **Build** – Compile backend (Maven) and frontend (npm build)
2. **Test** – Backend unit tests
3. **Publish** – Build Docker image, push to Docker Hub
4. **Deploy** – SSH to production, pull image, run container on port 8073

## 4. Production URL

After deploy, the app is at:

- **Frontend & API**: http://csci5308-vm2.research.cs.dal.ca:8073

The container serves both the React frontend and Spring Boot API from this single URL.

## 5. Local production build (optional)

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

## 6. Troubleshooting

- **Build fails**: Check backend Maven logs and frontend npm build logs in the GitLab job output.
- **Deploy fails**: Confirm `ID_RSA` has correct permissions and the key is added to the server’s `authorized_keys`.
- **502 / app not loading**: Check `docker ps` on the server and logs via `docker logs my-app`.
