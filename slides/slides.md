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
- How to make the codebase more maintainable
- How to model your tests correctly
- How to implement CI
- How to solve common problems in performance testing

---

# When and why performance testing matters

- An application with poor performance slows down the user and also the developers
- Poor performance leads to longer development times because manual and automated tests take longer
- Crucial for usability
- ISO 9241-11 Ergonomics of human-system interaction
    - Effectiveness
    - Efficiency
    - Satisfaction
-
- https://arxiv.org/abs/2408.12736
- https://www.sciencedirect.com/science/article/abs/pii/S0164121207000088

---

# Your first performance test

```kotlin
object FirstSimulation : Simulation() {
    private val browse = scenario("Browse Books V1") // define the workflow that should be tested
    /* ... */

    fun getBooks(): ChainBuilder { /* ... */
    } // define a single request

    val HTTP_PROTOCOL = HttpDsl.http
        .baseUrl(BASE_URL) // Url to your application
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")

    init {
        setUp(browse.injectOpen(rampUsers(10_000).during(20.seconds))) // define injection profile and number of users
            .protocols(HTTP_PROTOCOL)
            .assertions(/* ... */)
    }
}
```

---

# Your first performance test

```kotlin
object FirstSimulation : Simulation() {
    private val browse = scenario("Browse Books V1") // define the workflow that should be tested
        .exec(
            group("Browse Books").on( // grouping makes it easier to read the results
                exec(getBooks()), // execute the getBooks request defined below
                pause(1), // pause for one second
                exec(getBooks())
            )
        )

    fun getBooks(): ChainBuilder { // define a single request
        return exec(
            http("getBooks")
                .get("${Constants.BASE_URL}/books")
                .queryParam("page", 1)
                .queryParam("size", 10)
                .check(status().`is`(200)) // checks the response body
        )
    }

    init {
        setUp(browse.injectOpen(rampUsers(10_000).during(20.seconds))) // define injection profile and number of users
            .protocols(HTTP_PROTOCOL)
            .assertions(
                global().responseTime().max()
                    .lt(100), // You need to define values according to your business requirements
                global().responseTime().mean().lt(50),
                global().successfulRequests().percent()
                    .gt(95.0), // Under load some requests might fail. That can always happen
            )
    }
}
```

--- 

# How Gatlings session works

- essentially a map storing data that can be used in requests
- Write: `check(jsonPath().saveAs()) / set inside of exec`
- Read: `"#{}"` / `get` inside of exec


- ```kotlin
  fun getBooks(page: Int? = null, size: Int? = null): ChainBuilder {
        var req = http("getBooks").get("$baseUrl/books")
        if (page != null) req = req.queryParam("page", page)
        if (size != null) req = req.queryParam("size", size)
        return exec(Authentication.ensureValidToken)
            .exec(req
                .check(statusIs(200))
                .check(jsonPath("$.content").saveAs("content"))
                .check(jsonPath("$.totalElements").saveAs("totalElements"))
                .check(jsonPath("$.totalPages").saveAs("totalPages"))
                .check(jsonPath("$.size").saveAs("size"))
                .check(jsonPath("$.number").saveAs("number"))
            )
    }
  ```

---
layout: section
---

# Make the repo maintainable

---

# Make the repo maintainable

- extract Http Calls using the Page-Object pattern
- Define response time assertions as a constant
- Define load sizes as constants

```kotlin
object BooksPage {
    
    /* ... */
    
    fun getBooks(page: Int? = null, size: Int? = null): ChainBuilder {
        var req = http("getBooks").get("$baseUrl/books")
        if (page != null) req = req.queryParam("page", page) // add queryParams
        if (size != null) req = req.queryParam("size", size)
        return exec(Authentication.ensureValidToken) // will be explained later in the talk
            .exec(req
                .check(statusIs(200)) // make sure that the request is successful
                .check(jsonPath("$.content").saveAs("content")) // store all response values with check().saveAs()
                .check(jsonPath("$.totalElements").saveAs("totalElements"))
                .check(jsonPath("$.totalPages").saveAs("totalPages"))
                .check(jsonPath("$.size").saveAs("size"))
                .check(jsonPath("$.number").saveAs("number"))
            )
    }

    /* ... */
  
}
```

---

# Make the repo maintainable

- This approach creates a lot of repetitive code
  - set query params and body
  - check that the request was successful
  - save all response values

What can we do to reduce boilerplate?

---

# Make the repo maintainable

- We can generate that code automatically

Hey Claude, please create Gatling PageObjects in Kotlin and without comments for the OpenAPI Spec that I provided here:
```yaml
paths:
  /v1/books:
    get:
      tags: [Books]
      operationId: getAllBooks
      summary: Get all books (no pagination)
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Book'
  /books:
    get:
      tags: [Books]
      operationId: getBooks
      summary: Get all books (paginated)
      parameters:
        - in: query
          name: page
          schema:
            type: integer
            default: 0
          required: false
        - in: query
          name: size
          schema:
            type: integer
            default: 20
          required: false
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/BookPage'
```

Result:
```kotlin
package api

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*

object BooksApi {

  val getAllBooks =
    exec(
      http("GET All Books - /v1/books")
        .get("/v1/books")
        .check(status().`is`(200))
    )

  val getBooksPaginated =
    exec(
      http("GET Books Paginated - /books")
        .get("/books")
        .queryParam("page", "#{page}")
        .queryParam("size", "#{size}")
        .check(status().`is`(200))
    )

  val getBooksPaginatedWithDefaults =
    exec { session ->
      val page = session.getIntOrNull("page") ?: 0
      val size = session.getIntOrNull("size") ?: 20
      session.setAll(mapOf("page" to page, "size" to size))
    }.exec(getBooksPaginated)

  fun getBooks(page: Int = 0, size: Int = 20) =
    exec(
      http("GET Books page=$page size=$size")
        .get("/books")
        .queryParam("page", page)
        .queryParam("size", size)
        .check(status().`is`(200))
    )
}
```

---

# Make the repo maintainable

- This approach is not deterministic
- A good alternative: Hey Claude, write a Gradle Task that converts any OpenApi Spec into Gatling Page Objects in Kotlin without comments. Apply TDD for development. Here is a reference for the OpenAPI Spec: ...
- The task is deterministic. It always works the same
- Because the task is deterministic, it is verifiable
- If the task doesn't work, you (or AI) can fix it

---
layout: section
---

# Integrate gatling in your CI pipeline

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

# Integrate gatling in your CI pipeline

- Some tests should be part of the build pipeline
- You should have the possibility to run a specific test against a specific stage with the press of a button and receive
  the gatling report and monitoring data
- You can define simulations that should be run during the night

![Pipeline Workflow](./pipeline-workflow.png)

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

---

# Gatlings injection profiles for modeling different kinds of performance tests

| Profile               	   | Description	                        | Use Case                 |
|---------------------------|-------------------------------------|--------------------------|
| constantUsersPerSec       | Constant rate of users per second	  | Sustained load testing   |
| rampUsers               	 | Gradually increase users over time	 | Realistic traffic growth |
| atOnceUsers               | All users start at once	            | Spike testing            |
| stressPeak                | Peak stress pattern	                | Find breaking points     |

[https://docs.gatling.io/ai/assistant/vscode/create-simulation/#step-3-injection-profile]

---

# Examples for injection profile usages

- Scenario 1: You want to test if the book store can handle the average amount of users properly
    - You start with rampUsers for a few seconds then continue with constantUsersPerSec
    - You should look into your monitoring application to figure out the average amount of users
- Scenario 2: You want to test if there are memory leaks in the book store application
    - You run the constantUsersPerSec for a very long time, for example for two hours against a dedicated machine
    - You monitor memory usage and see if the memory usages increases significantly over time
    - You can do this with different sets of requests to figure out which requests cause issues
    - Once you have found significant increased memory usages you should run the application locally and use the
      profiler to investigate further
- Scenario 3: The book store now sells tickets for signature sessions with authors and you want to make sure that the
  application can handle peaks when the sale starts
    - You need to guess the amount of users that try to login at peak times
    - You use atOnceUsers and see if the application can handle the load
    - In the future when you have some data about how many people tend to login at peak times you can take that number
      for the test

---
layout: section
---

# How to solve common problems

---
layout: section
---

# Authentication

---

# How to test endpoints that need authentication

- Login request at the start of the simulation: `exec(Authentication.login(username, password))`
- Before every request that needs to be authenticated: `exec(Authentication.ensureValidToken)`
- Add Authorization Header to every request by adding it to the HTTP_PROTOCOL constant
- Remove Authorization Header from login and refresh calls with `header("Authorization", "")`

BrowseBooksSimulation.kt

```kotlin
scenario("Browse Books")
    .exec(Authentication.login("username", "password")) // call login at the start of the scenario
    .exec(browseBooks())
```

BooksPage.kt

```kotlin
fun getAllBooks(): ChainBuilder {
    var req = http("getAllBooks").get("$baseUrl/v1/books")
    return exec(Authentication.ensureValidToken) // call ensureValidToken before making request
        .exec(
            req
                .check(statusIs(200))
                .check(jsonPath("$[*]").ofList().saveAs("getAllBooksList"))
        )
}
```

---

# How to test endpoints that need authentication

```kotlin
fun login(username: String, password: String): ChainBuilder = exec(
    http("Login")
        .post(TOKEN_URL) // keycloak for example
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("Authorization", "") // authorization header should not be set here
        .formParam("grant_type", "password") // start of credentials
        .formParam("client_id", AuthenticationConfig.clientId)
        .formParam("username", username)
        .formParam("password", password) // end of credentials
        .check(
            status().`is`(200),
            jsonPath("$.access_token").saveAs("accessToken"), // store data in session
            jsonPath("$.refresh_token").saveAs("refreshToken"),
            jsonPath("$.expires_in").ofLong().saveAs("tokenExpiresIn"),
        )
).exec { session ->
    session.set("tokenExpiresAt", System.currentTimeMillis() + session.getLong("tokenExpiresIn") * 1000L)
}
```

---

# How to test endpoints that need authentication

- Add Authorization header to every request by adding it to the HTTP_PROTOCOL constant
- Before executing an authenticated request: `exec(Authentication.ensureValidToken)`

```kotlin
val HTTP_PROTOCOL = HttpDsl.http
    .baseUrl(BASE_URL)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .header("Authorization", "Bearer #{accessToken}")
```

```kotlin
val ensureValidToken: ChainBuilder =
    doIf { session ->
        !session.contains("tokenExpiresAt") ||
                System.currentTimeMillis() >= session.getLong("tokenExpiresAt") - REFRESH_BUFFER_MS
    }.then(refreshToken)
```

---

```kotlin
val refreshToken: ChainBuilder = exec(
    http("Refresh Token")
        .post(TOKEN_URL) // keycloak for example
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("Authorization", "") // authorization header should not be set here
        .formParam("grant_type", "refresh_token")
        .formParam("client_id", AuthenticationConfig.clientId)
        .formParam("refresh_token", "#{refreshToken}")
        .check(
            status().`is`(200),
            jsonPath("$.access_token").saveAs("accessToken"),
            jsonPath("$.refresh_token").saveAs("refreshToken"),
            jsonPath("$.expires_in").ofLong().saveAs("tokenExpiresIn"),
        )
).exec { session ->
    session.set("tokenExpiresAt", System.currentTimeMillis() + session.getLong("tokenExpiresIn") * 1000L)
}
```

---
layout: section
---

# Test Data Generation

---

# How to generate test data

- Generating test data can become extremely painful for complex domain objects
- A good architecture for generating test data is crucial to prevent that pain
- In Gatling feeders are used to generate test data
- I really like the use of custom feeders
- You can create a custom feeder for every kind of value that exists in your application, e.g. ISBN, Names, Emails etc.
- Use Value Objects in your domain objects: Instead of storing names as Strings, create a value object Name, FirstName
  or LastName and store the Strings inside of that. Then write test data generation logic for each value object. Then
  the rest of the generation of test data can be automatated. For each field, check type and select the correct feeder

---

# How to generate test data

Example of a feeder:
```kotlin
private val bookFeeder = intArrayOf(100).map { index ->
    buildMap {
        put("title", "Title $index") // it would be nice if the values would differ more
        put("author", "Author") // it would be nice if the values were realistic and 
        put("isbn", "978-11-123-${10000 + index}-21") // if the backend does validation, this needs to be a valid value
        put("price", Random.nextInt(10, 20))
        put("publisher", "Publisher")
    }
}.iterator()
```

Usage of the feeder:
```kotlin
private val scenario = scenario("Create Many Books")
    .exec(Authentication.login())
    .feed(bookFeeder)
    .exec(createBooks(NUMBER_OF_BOOKS_TO_BE_GENERATED))
```

---

# How to generate test data

Introducing domain and value objects:
```kotlin
data class Book(
  val title: Title, // Title value object instead of String
  val author: Name, // Name value object instead of String
  val isbn: ISBN,
  val price: Price,
  val publisher: Publisher? = null,
)
```

Generating a book:
```kotlin
fun generate(): Book = Book(
    title = Title.generate(),
    author = Name.generate(),
    isbn = ISBN.generate(),
    price = Price.generate(),
    publisher = if (Random.nextBoolean()) Publisher.generate() else null,
)
```

---

Generating a valid ISBN:
```kotlin
@JvmInline
value class ISBN(val value: String) {
  companion object {
    fun generate(): ISBN {
      val prefix = "978"
      val registrant = Random.nextInt(0, 10)
      val publication = Random.nextInt(100, 1000)
      val title = Random.nextInt(10000, 100000)
      val checkDigit = calculateCheckDigit("$prefix$registrant$publication$title")
      return ISBN("978-$registrant-$publication-$title-$checkDigit")
    }

    private fun calculateCheckDigit(digits: String): Int { /* ... */ }
  }
}
```

---

Generating random Names:
```kotlin
data class Name(val firstName: String, val lastName: String) {
    val fullName: String get() = "$firstName $lastName"

    companion object {
        private val FIRST_NAMES = listOf(
            "Alice", "Bob", "Clara", "David", "Elena", "Frank", "Grace",
            "Henry", "Iris", "James", "Karen", "Liam", "Maya", "Noah",
        )
        private val LAST_NAMES = listOf(
            "Ashford", "Blake", "Chen", "Drake", "Evans", "Fischer", "Grant",
            "Hayes", "Irons", "Jung", "Klein", "Lowe", "Marsh", "Nash",
        )

        fun generate(): Name = Name(FIRST_NAMES.random(), LAST_NAMES.random())
    }
}
```

---

Mapping objects to feeders:
```kotlin
interface TestDataGenerator<T> {
  fun generate(): T
  fun toSessionMap(value: T): Map<String, Any>

  fun feeder(): Iterator<Map<String, Any>> = generateSequence { toSessionMap(generate()) }.iterator()
}
```

```kotlin
object BookFeeder : TestDataGenerator<Book> {

    override fun generate(): Book = Book.generate()

    override fun toSessionMap(value: Book): Map<String, Any> = buildMap {
        put("title", value.title.value)
        put("author", value.author.fullName)
        put("isbn", value.isbn.value)
        put("price", value.price.value)
        value.publisher?.let { put("publisher", it.value) }
    }
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
