package org.example.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.*
import org.example.gatling.pages.AuthorsPage
import org.example.gatling.pages.BooksPage
import java.time.Duration

class FullApiSimulation : Simulation() {

    private val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")

    // Read-only users: browse books and authors
    private val reader = scenario("Reader")
        .exec(BooksPage.getBooks(page = 0, size = 10))
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(3))
        .exec(AuthorsPage.getAuthors(page = 0, size = 10))
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(3))
        .exec(BooksPage.getBooks(page = 1, size = 10))

    // Writers: create an author then a book attributed to them
    private val writer = scenario("Writer")
        .exec(AuthorsPage.createAuthor("Martin Fowler", origin = "England", biography = "Refactoring author"))
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.createBook("Refactoring", "Martin Fowler", "978-0201485677", 49.99))
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.getBookByIdFromSession())

    // Power users: full CRUD on both Books and Authors
    private val powerUser = scenario("Power User")
        .exec(AuthorsPage.createAuthor("Dave Thomas", origin = "USA"))
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.createBook("The Pragmatic Programmer", "Dave Thomas", "978-0135957059", 49.95))
        // patch the book title and price before updating
        .exec { session -> session.set("title", "The Pragmatic Programmer, 20th Anniversary").set("price", 54.95) }
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.updateBookFromSession())
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.deleteBookFromSession())
        .pause(Duration.ofMillis(500))
        .exec(AuthorsPage.deleteAuthorFromSession())

    init {
        setUp(
            // Mostly readers, fewer writers, handful of power users
            reader.injectOpen(rampUsers(60).during(Duration.ofSeconds(40))),
            writer.injectOpen(rampUsers(20).during(Duration.ofSeconds(30))),
            powerUser.injectOpen(rampUsers(10).during(Duration.ofSeconds(20))),
        ).protocols(httpProtocol)
            .assertions(
                global().responseTime().max().lt(2000),
                global().responseTime().mean().lt(500),
                global().successfulRequests().percent().gt(95.0),
            )
            .maxDuration(Duration.ofSeconds(60))
    }
}
