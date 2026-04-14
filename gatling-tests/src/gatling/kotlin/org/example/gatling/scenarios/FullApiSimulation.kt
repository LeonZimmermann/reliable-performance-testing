package org.example.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.*
import org.example.gatling.pages.BooksPage
import java.time.Duration

class FullApiSimulation : Simulation() {

    private val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")

    // Read-only users: browse multiple pages
    private val reader = scenario("Reader")
        .exec(BooksPage.getBooks(page = 0, size = 10))
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(3))
        .exec(BooksPage.getBooks(page = 1, size = 10))
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(3))
        .exec(BooksPage.getBooks(page = 0, size = 20))

    // Writers: create a book and look it up
    private val writer = scenario("Writer")
        .exec(BooksPage.createBook("Refactoring", "Martin Fowler", "978-0201485677", 49.99))
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.getBookByIdFromSession())

    // Power users: full create → update → delete cycle
    private val powerUser = scenario("Power User")
        .exec(BooksPage.createBook("The Pragmatic Programmer", "Dave Thomas", "978-0135957059", 49.95))
        // createBook saves id/title/author/isbn/price to session; patch the title before updating
        .exec { session -> session.set("title", "The Pragmatic Programmer, 20th Anniversary").set("price", 54.95) }
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.updateBookFromSession())   // PUT /books/#{id} with updated session fields
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.deleteBookFromSession())   // DELETE /books/#{id}
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.getBooks())

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
