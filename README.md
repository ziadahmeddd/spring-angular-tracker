# spring-angular-tracker

Expense tracker: Spring Boot REST backend + Angular frontend.

## Layout

| Path | What |
| --- | --- |
| `tracker/` | Spring Boot 4.1.1 API, Java 21, H2 in-memory DB |
| `tracker-ui/` | Angular 22 frontend, TypeScript 6 |

## Run the backend

```bash
cd tracker
./mvnw spring-boot:run
```

Serves on `http://localhost:8080`. H2 console at `http://localhost:8080/h2-console`
(JDBC URL `jdbc:h2:mem:trackerdb`, user `sa`, empty password).

## Run the frontend

```bash
cd tracker-ui
npm install
npm start
```

Serves on `http://localhost:4200`.

## API

| Method | Path | What |
| --- | --- | --- |
| `POST` | `/api/expenses` | Create an expense |
| `GET` | `/api/expenses` | List expenses (paged) |
| `GET` | `/api/expenses/{id}` | Get one expense |
| `PUT` | `/api/expenses/{id}` | Update an expense |
| `DELETE` | `/api/expenses/{id}` | Delete an expense |
| `GET` | `/api/health` | Health check |

The database is in-memory, so data is discarded when the backend stops.
