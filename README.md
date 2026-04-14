# OAS Demo

A full-stack demo application showcasing OpenAPI-driven development with a Spring Boot backend and a Vue 3 frontend.

## Project Structure

```
OASDemo/
├── oas/               # OpenAPI specification (openapi.yaml)
├── backend/           # Spring Boot REST API (Kotlin, JPA, H2)
├── frontend/          # Vue 3 SPA (TypeScript, Vite, Vue Router)
└── gatling-tests/     # Performance tests (Gatling, Kotlin)
```

## Prerequisites

- **Java 21** (backend)
- **Node.js 18+** and **npm** (frontend)

## Running the Application

### 1. Start the Backend

From the project root:

```bash
./gradlew :backend:bootRun
```

The API starts at `http://localhost:8080`. The H2 console is available at `http://localhost:8080/h2-console` and the Swagger UI at `http://localhost:8080/swagger-ui.html`.

### 2. Start the Frontend

Open a second terminal and run:

```bash
cd frontend
npm install        # only needed the first time
npm run generate   # regenerate API client from oas/openapi.yaml (re-run after spec changes)
npm run dev
```

The app opens at **`http://localhost:5173`**.

> The Vite dev server automatically proxies all `/books` requests to the backend, so no CORS configuration is required.

---

## Frontend

### Features

| View | Route | Description |
|------|-------|-------------|
| Book list | `/books` | Paginated table of all books |
| Book detail | `/books/:id` | Full book details with inline editing |
| Create book | `/books/create` | Form to add a new book |

### What you can do

- **Browse** the book list with pagination (10 books per page)
- **Create** a new book via the *+ New Book* button in the navigation bar
- **View** a book's details by clicking its title or the *View* button
- **Edit** a book inline on the detail page — click *Edit*, modify the fields, and *Save Changes*
- **Delete** a book from either the list (row-level *Delete* button) or the detail page

### Tech stack

- **Vue 3** with Composition API (`<script setup>`)
- **Vue Router** for client-side navigation
- **TypeScript** with strict mode
- **Vite** as the dev server and build tool
- Native `fetch` for HTTP — no third-party HTTP client

### API client generation

The TypeScript API client and models are generated from `oas/openapi.yaml` using `@openapitools/openapi-generator-cli` (generator: `typescript-fetch`). Run this after any spec change:

```bash
cd frontend
npm run generate
```

The generated folder `src/generated/` is gitignored — it must be regenerated on each fresh checkout.

### Building for production

```bash
cd frontend
npm run generate   # ensure types are up to date
npm run build
```

The optimised output is written to `frontend/dist/`.

---

## Backend

The API is defined in `oas/openapi.yaml` and the Spring controller interfaces are generated from it at build time.

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/books?page=0&size=10` | List books (paginated) |
| `POST` | `/books` | Create a book |
| `GET` | `/books/{id}` | Get a book by ID |
| `PUT` | `/books/{id}` | Update a book |
| `DELETE` | `/books/{id}` | Delete a book |

### Running the tests

```bash
./gradlew :backend:test
```

---

## Performance Tests

See [`gatling-tests/README.md`](gatling-tests/README.md) for details on running the Gatling load tests.
