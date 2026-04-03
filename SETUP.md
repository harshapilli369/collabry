# Developer Setup Guide

Follow these steps to get the project running on your local machine.

## Prerequisites
*   Node.js (v18+)
*   Java JDK 17+
*   Maven

## Docker (recommended)
To run with Docker, use the **root** `.env` file (not only `frontend/.env`):
1.  In the project root (`group04/`), copy `cp .env.example .env` and edit `.env` with your Google Client ID and Gmail SMTP (App Password) settings.
2.  Run `docker compose up --build`. Backend and frontend read all config from this root `.env` file.
See the main **README.md** for the full list of variables and Docker steps.

## 1. Backend Setup (Spring Boot)
The backend runs on port **9090**.

1.  Navigate to the backend folder:
    ```bash
    cd backend
    ```
2.  Run the application:
    ```bash
    ./mvnw spring-boot:run
    ```

## 2. Frontend Setup (React)
1.  Navigate to the frontend folder:
    ```bash
    cd frontend
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  **Create your Environment File**:
    *   Copy `.env.example` to `.env`:
        ```bash
        cp .env.example .env
        ```
    *   Open `.env` and paste your Google Client ID:
        ```
        VITE_GOOGLE_CLIENT_ID=your_actual_google_client_id
        VITE_API_BASE_URL=http://localhost:9090/api/auth
        ```
4.  Run the frontend:
    ```bash
    npm run dev
    ```

## Troubleshooting
*   **Blank Screen?** Check if you created the `.env` file with `VITE_GOOGLE_CLIENT_ID`.
*   **Login Fails?** Ensure the backend is running on port 9090.
