# CoWork Hub

## Requirements

- Docker Desktop
- Java 25
- Maven Wrapper is included in each backend service

## Run With Docker Compose

From the project root:

```powershell
docker compose up --build
```

Run in the background:

```powershell
docker compose up --build -d
```

Stop the project:

```powershell
docker compose down
```

Stop and remove Docker volumes/database data:

```powershell
docker compose down -v
```

## Service URLs

- API Gateway: `http://localhost:8080`
- Eureka Dashboard: `http://localhost:8761`
- User Service: `http://localhost:8081`
- Booking Service: `http://localhost:8082`
- Invoice Service: `http://localhost:8083`
- MySQL: `localhost:3307`
- Kafka: `localhost:9092`

## Run Tests

Run tests for a service from its folder:

```powershell
.\mvnw.cmd test
```

Example:

```powershell
cd user
.\mvnw.cmd test
```

## Frontend

Open the frontend files from the `frontend` folder, or run:

```powershell
.\start-frontend.ps1
```

## Useful Scripts

```powershell
.\run-all-services.ps1
.\stop-services.ps1
.\start-frontend.ps1
```
