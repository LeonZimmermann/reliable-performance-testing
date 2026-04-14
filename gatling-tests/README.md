# Gatling Performance Tests

This module contains Gatling performance tests for the OAS Demo API, written in **Kotlin** and following the **Page Object Pattern** for better maintainability and reusability.

## Architecture

### Page Objects (`src/gatling/kotlin/org/example/gatling/pages`)

Page objects encapsulate API endpoint interactions:

- **BasePage**: Base class providing common functionality and API client initialization
- **HelloPage**: Encapsulates all `/hello` endpoint operations
- **BooksPage**: Encapsulates all `/books` endpoint operations

### Scenarios (`src/gatling/kotlin/org/example/gatling/scenarios`)

Test scenarios compose page objects to create realistic load tests:

- **HelloSimulation**: Tests the greeting endpoint with various load patterns
- **BooksSimulation**: Tests CRUD operations on the books endpoint
- **FullApiSimulation**: Comprehensive test combining multiple endpoints

## OpenAPI Code Generation

The build automatically generates a Java API client from `oas/openapi.yaml` using OpenAPI Generator. The generated client is available in the `org.example.generated` package and can be used within page objects if direct API calls are needed.

## Running Tests

### Prerequisites

1. Start the backend API server:
   ```bash
   ./gradlew :backend:bootRun
   ```

2. Ensure the API is accessible at `http://localhost:8080`

### Run All Simulations

```bash
./gradlew :gatling-tests:gatlingRun
```

### Run a Specific Simulation

```bash
./gradlew :gatling-tests:gatlingRun-org.example.gatling.scenarios.HelloSimulation
./gradlew :gatling-tests:gatlingRun-org.example.gatling.scenarios.BooksSimulation
./gradlew :gatling-tests:gatlingRun-org.example.gatling.scenarios.FullApiSimulation
```

### View Reports

After running tests, HTML reports are generated in:
```
gatling-tests/build/reports/gatling/
```

Open the `index.html` file in your browser to view detailed performance metrics.

## Creating New Scenarios

Thanks to the page object pattern and OAS code generation, creating new scenarios is straightforward:

1. **Use existing page objects**:
   ```kotlin
   val myScenario = scenario("My Test")
       .exec(HelloPage.getPersonalizedGreeting("John"))
       .exec(BooksPage.getAllBooks())
   ```

2. **Add new methods to page objects** if you need additional endpoint interactions:
   ```kotlin
   // In BooksPage.kt
   fun getBookById(id: Long): ChainBuilder {
       return exec(
           http("Get Book $id")
               .get("$baseUrl/books/$id")
               .check(statusIs(200))
       )
   }
   ```

3. **Create new page objects** for new API endpoints (automatically regenerate the API client when the OAS spec changes):
   ```bash
   ./gradlew :gatling-tests:generateApiClient
   ```

## Configuration

- **Base URL**: Configured in `BasePage.kt` (default: `http://localhost:8080`)
- **Gatling Settings**: Configured in `src/gatling/resources/gatling.conf`
- **Load Profiles**: Configured in each simulation file's `init` block

## Benefits of This Approach

1. **Kotlin Language**: Type-safe, expressive, and familiar to Java/Spring developers
2. **Reusability**: Page objects can be reused across multiple scenarios
3. **Maintainability**: API changes only require updates in one place
4. **Type Safety**: Both Kotlin and generated API client provide compile-time type checking
5. **Easy Scenario Creation**: Compose complex scenarios from simple building blocks
6. **Clear Separation**: Business logic (scenarios) separated from technical details (page objects)
