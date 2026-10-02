# Kricon InterviewAce Backend

Spring Boot backend for the InterviewAce AI internship project.

## Implemented

- User registration with BCrypt password hashing
- User login with JWT authentication
- Protected dashboard API
- User-specific dashboard statistics
- Question bank for DSA, Java and Python
- Question completion API (one completion per user per question)
- Backend progress tracking (completed / total per category, reaches 100%)
- Recent activity tracking
- JPA/Hibernate with H2 database
- Validation and global exception handling
- CORS configuration for the Vite frontend

## Run

```powershell
mvn spring-boot:run
```

Backend: `http://localhost:8080`

## Main APIs

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/dashboard/me` (JWT required)
- `GET /api/questions?category=DSA` (JWT required; each question includes a `completed` flag for the logged-in user)
- `POST /api/questions/{id}/complete` (JWT required; each question counts once per user)
- `POST /api/dashboard/activity` (JWT required)

## Frontend

The frontend runs separately with Vite and calls the backend at `http://localhost:8080/api`.

## Configuration

- `JWT_SECRET` environment variable overrides the dev-only default secret.
- If you upgrade from an older version, delete the `data/` folder once to reset the local H2 database.

Database files live in `~/.interviewace/` (your user folder), not inside the project, so OneDrive cannot lock them. Delete that folder to reset all accounts.
