package org.example.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.*
import org.example.gatling.pages.AuthorsPage
import java.time.Duration

class AuthorsSimulation : Simulation() {

    private val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")

    private val authorFeeder = listFeeder(listOf(
        mapOf("name" to "George Orwell",       "origin" to "England", "biography" to "Authored 1984 and Animal Farm"),
        mapOf("name" to "Jane Austen",          "origin" to "England", "biography" to "Authored Pride and Prejudice"),
        mapOf("name" to "F. Scott Fitzgerald",  "origin" to "USA",     "biography" to "Authored The Great Gatsby"),
        mapOf("name" to "Harper Lee",           "origin" to "USA",     "biography" to "Authored To Kill a Mockingbird"),
        mapOf("name" to "J.D. Salinger",        "origin" to "USA",     "biography" to "Authored The Catcher in the Rye"),
    )).circular()

    // Browse paginated author results
    private val browse = scenario("Browse Authors")
        .exec(AuthorsPage.getAuthors())
        .pause(Duration.ofSeconds(1))
        .exec(AuthorsPage.getAuthors(page = 0, size = 5))
        .pause(Duration.ofSeconds(1))
        .exec(AuthorsPage.getAuthors(page = 0, size = 10))

    // Create an author and immediately fetch by the returned ID
    private val createAndRead = scenario("Create and Read Author")
        .exec(AuthorsPage.createAuthor("Robert C. Martin", origin = "USA", biography = "Clean Code author"))
        .pause(Duration.ofSeconds(1))
        .exec(AuthorsPage.getAuthorByIdFromSession())   // uses session "id" saved by createAuthor

    // Full CRUD lifecycle: create → read → update → delete
    private val fullLifecycle = scenario("Author Full Lifecycle")
        .exec(AuthorsPage.createAuthor("Dave Thomas", origin = "England", biography = "Pragmatic Programmer author"))
        .pause(Duration.ofMillis(500))
        .exec(AuthorsPage.getAuthorByIdFromSession())
        .pause(Duration.ofMillis(500))
        .exec(AuthorsPage.updateAuthorFromSession())    // PUT /authors/#{id} with session name
        .pause(Duration.ofMillis(500))
        .exec(AuthorsPage.deleteAuthorFromSession())    // DELETE /authors/#{id}

    // Create authors from feeder at steady load
    private val createFromFeeder = scenario("Create Authors from Feeder")
        .feed(authorFeeder)
        .exec(AuthorsPage.createAuthorFromSession())
        .pause(Duration.ofSeconds(1))

    init {
        setUp(
            browse.injectOpen(rampUsers(20).during(Duration.ofSeconds(20))),
            createAndRead.injectOpen(rampUsers(10).during(Duration.ofSeconds(15))),
            fullLifecycle.injectOpen(rampUsers(10).during(Duration.ofSeconds(15))),
            createFromFeeder.injectOpen(constantUsersPerSec(3.0).during(Duration.ofSeconds(20))),
        ).protocols(httpProtocol)
            .assertions(
                global().responseTime().max().lt(1000),
                global().responseTime().mean().lt(300),
                global().successfulRequests().percent().gt(95.0),
            )
    }
}
