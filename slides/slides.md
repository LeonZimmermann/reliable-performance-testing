---
theme: default
title: Stop praying, start testing - reliable performance testing with gatling
class: text-center
drawings:
  persist: false
mdc: true
duration: 45min
---

# Stop praying, start testing

## Reliable Performance Testing with Gatling

---

# Outline

- When and why performance testing matters
- Your first performance test
- Refactoring your first performance test
- How to make the codebase more maintainable using code generation
- How to model your tests correctly
- How to implement CI
- How to solve common problems in performance testing

---

# When and why performance testing matters

- An application with poor performance slows down the user and also the developers
- Poor performance leads to longer development times because manual and automated tests take longer
- ISO 9241-11
    - Effektivität
    - Effizienz
    - Zufriedenstellung
- https://arxiv.org/abs/2408.12736
- https://www.sciencedirect.com/science/article/abs/pii/S0164121207000088

---

# Your first performance test

```kotlin
object FirstSimulation : Simulation() {
    private val browse = scenario("Browse Books V1")
        .exec(
            group("Browse Books").on(
                exec(getBooks()),
                pause(1),
                exec(getBooks())
            )
        )

    fun getBooks(): ChainBuilder {
        return exec(
            http("getBooks")
                .get("${Constants.BASE_URL}/books")
                .queryParam("page", 1)
                .queryParam("size", 10)
                .check(status().`is`(200))
        )
    }

    init {
        setUp(browse.injectOpen(rampUsers(10_000).during(20.seconds)))
            .protocols(HTTP_PROTOCOL)
            .assertions(
                global().responseTime().max().lt(100),
                global().responseTime().mean().lt(50),
                global().successfulRequests().percent().gt(95.0),
            )
    }

}

```

---

- Building blocks of a performance test in Gatling
    - Scenario
    - Http-Requests
    - setUp call

--- 

# How Gatlings session works

- essentially a map storing data that can be used in requests
- Write: `check(jsonPath().saveAs()) / set inside of exec`
- Read: `"#{}"` / `get` inside of exec

---

# Refactoring our simulation

- Extract getBooks() into separate page object
- Define response time assertions as a constant
- Define load sizes as constants

---

# Make the repo maintainable

- extract Http Calls using the Page-Object pattern
    - Create a class for each controller in your backend that simulates its endpoints
- Generate page objects automatically from OpenAPI spec
    - Generate OAS Schema by backend or do spec first
    - Tell an AI agent to write page objects accordingly or tell AI to write a gradle task which automatically converts
      the code
    - Prompt: TODO

---

# Generate page objects from OpenAPI spec

TODO

---

# Integrate gatling in your CI pipeline

- Run application and gatling locally for testing purposes
- Run application and gatling in Pipeline for CI quality gates
- Run application on a dedicated server and gatling in pipeline for dedicated tests
- Run gatling against prod environment for dedicated tests (analyze when a good time is beforehand)
- Always monitor your application during performance tests

- How?
    - The same way you would do locally
    - Run your application using docker
    - Wait for the application to be ready using a custom bash probe
    - Seed data if necessary
    - Run the performance test
    - Store the report and monitoring data

---

# Considerations when integrating gatling in your CI pipeline

- Pipeline runtime should be low. So maybe you dont want it to be an automatic CI task
- Manual or nightly executions are good alternatives to a separate build step
- Your approach is going to depend on how critical performance really is for your application
- Maybe performance is not just a quality metric for you but actually a functional requirement. Then it should be run as
  a separate build step
    - Examples: Ticketing system. If it can't handle realistic spikes, its kind of useless, because no one will be able
      to use it then

---
layout: section
---

# How to create realistic simulations

## Different kinds of performance tests

---

# Different kinds of performance tests

- Load tests: Can the system manage a certain load
- Spike tests: Can the system respond to sudden bursts of peak loads
- Stress tests: Handle peak loads that are beyond the limits of anticipated loads
- Scalability tests: How well can the system grow
- Concurrency tests: Does the system still behave correctly with many concurrent users

---

# Translating kind of performance test into Gatling

- You need monitoring as a prerequisite
- Load profiles
    - Define an expected average load according to your monitoring data
    - Define an expected peak load according to your monitoring data
- Define acceptable response time limits for your application (business consideration)
- Define injection profiles that model your type of performance test
    - TODO Examples

---

# Gatlings injection profiles for modelling different kinds of performance tests

| Profile               	   | Description	                        | Use Case                 |
|---------------------------|-------------------------------------|--------------------------|
| constantUsersPerSec       | Constant rate of users per second	  | Sustained load testing   |
| rampUsers               	 | Gradually increase users over time	 | Realistic traffic growth |
| atOnceUsers               | All users start at once	            | Spike testing            |
| stressPeak                | Peak stress pattern	                | Find breaking points     |

[https://docs.gatling.io/ai/assistant/vscode/create-simulation/#step-3-injection-profile]

---
layout: section
---

# How to solve common problems

## Authentication and test data generation

---

# How to test endpoints that need authentication

```kotlin

object OAuthFlow {

    private const val ACCESS_TOKEN = "accessToken"
    private const val REFRESH_TOKEN = "refreshToken"
    private const val AUTH_STATUS = "authStatus"

    fun authenticate(): ChainBuilder {
        return exec(
            http("OAuth - Login")
                .post("/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "your-client-id")
                .formParam("client_secret", "your-client-secret")
                .formParam("username", "test-user")
                .formParam("password", "test-password")
                .check(status().saveAs(AUTH_STATUS))
                .check(jsonPath("$.access_token").saveAs(ACCESS_TOKEN))
                .check(jsonPath("$.refresh_token").saveAs(REFRESH_TOKEN))
        ).exec(
            // Falls Login direkt fehlschlägt
            doIf(session -> session.getInt(AUTH_STATUS) != 200).then(
        exec { session ->
            session.markAsFailed()
        }
        )
        )
    }

    fun refreshToken(): ChainBuilder {
        return exec(
            http("OAuth - Refresh Token")
                .post("/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("grant_type", "refresh_token")
                .formParam("client_id", "your-client-id")
                .formParam("client_secret", "your-client-secret")
                .formParam("refresh_token", "#{" + REFRESH_TOKEN + "}")
                .check(status().saveAs(AUTH_STATUS))
                .check(jsonPath("$.access_token").saveAs(ACCESS_TOKEN))
                .check(jsonPath("$.refresh_token").optional().saveAs(REFRESH_TOKEN))
        ).exec(
            doIf(session -> session.getInt(AUTH_STATUS) != 200).then(
        exec { session ->
            session.markAsFailed()
        }
        )
        )
    }

    fun attachBearerToken(): ChainBuilder {
        return exec(
            addHeader("Authorization", "Bearer #{accessToken}")
        )
    }

    fun requestWithAutoRefresh(
        requestName: String,
        requestBuilder: io.gatling.javaapi.http.HttpRequestActionBuilder
    ): ChainBuilder {
        return exec(
            http(requestName)
                .get("/protected/resource")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().saveAs(AUTH_STATUS))
        ).doIf(session -> session.getInt(AUTH_STATUS) == 401).then(
        refreshToken()
        ).exec(
        http("$requestName - retry after refresh")
            .get("/protected/resource")
            .header("Authorization", "Bearer #{accessToken}")
        )
    }
}
```

---

# How to generate test data

- Feeders
- Use Value Objects in your domain objects: Instead of storing names as Strings, create a value object Name, FirstName
  or LastName and store the Strings inside of that. Then write test data generation logic for each value object. Then
  the rest of the generation of test data can be automatated. For each field, check type and select the correct feeder

```
fun books(): Iterator<Map<String, Any>> =
        generateSequence { nextBook() }.iterator()

    private fun nextBook(): Map<String, Any> {
        val record = mutableMapOf<String, Any>(
            "title" to "${TITLE_ADJECTIVES.random()} ${TITLE_NOUNS.random()} ${Random.nextInt(1, 1000)}",
            "author" to "${FIRST_NAMES.random()} ${LAST_NAMES.random()}",
            "isbn" to generateIsbn(),
            "price" to (Random.nextInt(500, 5000) / 100.0),
        )
        if (Random.nextBoolean()) {
            record["publisher"] = PUBLISHERS.random()
        }
        return record
    }

    private fun generateIsbn(): String {
        val part1 = Random.nextInt(0, 10)
        val part2 = Random.nextInt(100, 999)
        val part3 = Random.nextInt(10000, 99999)
        val part4 = Random.nextInt(0, 10)
        return "978-$part1-$part2-$part3-$part4"
    }
```

---
layout: two-cols
---

My linked in profile

![LinkedIn profile Leon Zimmermann](./qr-code-linked-in.png)
https://www.linkedin.com/in/leon-zimmermann-5179831a4/

::right::

The github repository for this talk

![Github Repository](./qr-code-github-repo.png)
https://github.com/LeonZimmermann/reliable-performance-testing
