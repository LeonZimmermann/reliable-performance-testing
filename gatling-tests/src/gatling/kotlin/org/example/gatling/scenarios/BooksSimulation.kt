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

    private val readBooks = scenario("Read Books")
        .exec(BooksPage.getBooks())
        .pause(Duration.ofSeconds(1))

    private val createBooks = scenario("Create Books")
        .exec(BooksPage.createBook("Clean Code",     "Robert C. Martin", "978-0132350884", 44.99))
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.createBook("Design Patterns", "Erich Gamma",     "978-0201633610", 54.99, publisher = "Addison-Wesley"))

    private val createFromFeeder = scenario("Create Books from Feeder")
        .feed(bookFeeder)
        .exec(BooksPage.createBookFromSession())
        .pause(Duration.ofSeconds(1))

    private val mixedOps = scenario("Mixed Operations")
        .exec(BooksPage.getBooks())
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.createBook("Effective Java", "Joshua Bloch", "978-0134685991", 49.99))
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.getBooks())

    init {
        setUp(
            readBooks.injectOpen(constantUsersPerSec(2.0).during(Duration.ofSeconds(20))),
            createBooks.injectOpen(rampUsers(10).during(Duration.ofSeconds(15))),
            createFromFeeder.injectOpen(constantUsersPerSec(3.0).during(Duration.ofSeconds(15))),
            mixedOps.injectOpen(rampUsers(10).during(Duration.ofSeconds(20))),
        ).protocols(httpProtocol)
            .assertions(
                global().responseTime().max().lt(1000),
                global().responseTime().mean().lt(300),
                global().successfulRequests().percent().gt(95.0),
            )
    }
}
