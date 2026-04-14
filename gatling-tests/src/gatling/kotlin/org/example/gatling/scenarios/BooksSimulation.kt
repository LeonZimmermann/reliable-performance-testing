package org.example.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.*
import org.example.gatling.pages.BooksPage
import java.time.Duration

class BooksSimulation : Simulation() {

    private val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")

    private val bookFeeder = listFeeder(listOf(
        mapOf("title" to "The Great Gatsby",       "author" to "F. Scott Fitzgerald", "isbn" to "978-0743273565", "price" to 15.99),
        mapOf("title" to "1984",                   "author" to "George Orwell",       "isbn" to "978-0451524935", "price" to 13.99),
        mapOf("title" to "To Kill a Mockingbird",  "author" to "Harper Lee",          "isbn" to "978-0061120084", "price" to 14.99),
        mapOf("title" to "Pride and Prejudice",    "author" to "Jane Austen",         "isbn" to "978-0141439518", "price" to 12.99),
        mapOf("title" to "The Catcher in the Rye", "author" to "J.D. Salinger",       "isbn" to "978-0316769174", "price" to 13.50),
    )).circular()

    // Browse paginated results
    private val browse = scenario("Browse Books")
        .exec(BooksPage.getBooks())
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.getBooks(page = 1, size = 5))
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.getBooks(page = 0, size = 10))

    // Create a book and immediately fetch it by the returned ID
    private val createAndRead = scenario("Create and Read")
        .exec(BooksPage.createBook("Clean Code", "Robert C. Martin", "978-0132350884", 44.99))
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.getBookByIdFromSession())  // uses session "id" saved by createBook

    // Full CRUD lifecycle: create → read → update → delete
    private val fullLifecycle = scenario("Full Lifecycle")
        .exec(BooksPage.createBook("Design Patterns", "Erich Gamma", "978-0201633610", 54.99, publisher = "Addison-Wesley"))
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.getBookByIdFromSession())
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.updateBookFromSession())   // updates with same session fields (title/author/isbn/price)
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.deleteBookFromSession())   // deletes by session "id"

    // Create books from feeder data at steady load
    private val createFromFeeder = scenario("Create from Feeder")
        .feed(bookFeeder)
        .exec(BooksPage.createBookFromSession())
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
