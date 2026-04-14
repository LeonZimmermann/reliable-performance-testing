package org.example.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.*
import org.example.gatling.pages.BooksPage
import java.time.Duration

/**
 * Load test simulation for the Books endpoint.
 * Demonstrates CRUD operations using the BooksPage page object.
 */
class BooksSimulation : Simulation() {

    // HTTP protocol configuration
    private val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")

    // Feeder with book data for dynamic test scenarios
    private val bookFeeder = listFeeder(listOf(
        mapOf(
            "bookTitle" to "The Great Gatsby",
            "bookAuthor" to "F. Scott Fitzgerald",
            "bookIsbn" to "978-0743273565",
            "bookPrice" to 15.99
        ),
        mapOf(
            "bookTitle" to "1984",
            "bookAuthor" to "George Orwell",
            "bookIsbn" to "978-0451524935",
            "bookPrice" to 13.99
        ),
        mapOf(
            "bookTitle" to "To Kill a Mockingbird",
            "bookAuthor" to "Harper Lee",
            "bookIsbn" to "978-0061120084",
            "bookPrice" to 14.99
        ),
        mapOf(
            "bookTitle" to "Pride and Prejudice",
            "bookAuthor" to "Jane Austen",
            "bookIsbn" to "978-0141439518",
            "bookPrice" to 12.99
        ),
        mapOf(
            "bookTitle" to "The Catcher in the Rye",
            "bookAuthor" to "J.D. Salinger",
            "bookIsbn" to "978-0316769174",
            "bookPrice" to 13.50
        )
    )).circular()

    // Scenario 1: Simple read operation - get all books
    private val readBooksScenario = scenario("Read Books")
        .exec(BooksPage.getAllBooks())
        .pause(Duration.ofSeconds(1))

    // Scenario 2: Create books with static data
    private val createBooksScenario = scenario("Create Books")
        .exec(BooksPage.createBook("Clean Code", "Robert C. Martin", "978-0132350884", 44.99))
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.createBookWithPublisher(
            "Design Patterns",
            "Erich Gamma",
            "978-0201633610",
            54.99,
            "Addison-Wesley"
        ))
        .pause(Duration.ofSeconds(1))

    // Scenario 3: Create books using feeder data
    private val createBooksFromFeederScenario = scenario("Create Books from Feeder")
        .feed(bookFeeder)
        .exec(BooksPage.createBookFromSession())
        .pause(Duration.ofSeconds(2))

    // Scenario 4: Mixed operations - read, create, read again
    private val mixedOperationsScenario = scenario("Mixed Operations")
        .exec(BooksPage.getAllBooks())
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.createBook("Effective Java", "Joshua Bloch", "978-0134685991", 49.99))
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.getAllBooks())
        .pause(Duration.ofSeconds(1))

    // Scenario 5: High load scenario with dynamic data
    private val highLoadScenario = scenario("High Load Creation")
        .feed(bookFeeder)
        .exec { session ->
            val title = session.getString("bookTitle")
            val author = session.getString("bookAuthor")
            val isbn = session.getString("bookIsbn")
            val price = session.getDouble("bookPrice")
            session
                .set("bookTitle", title)
                .set("bookAuthor", author)
                .set("bookIsbn", isbn)
                .set("bookPrice", price)
        }
        .exec(BooksPage.createBookFromSession())
        .pause(Duration.ofMillis(500))

    // Load simulation setup with different injection profiles
    init {
        setUp(
            // Light read load throughout the test
            readBooksScenario.injectOpen(
                constantUsersPerSec(2.0).during(Duration.ofSeconds(30))
            ),

            // Gradual ramp-up of book creation
            createBooksScenario.injectOpen(
                rampUsers(10).during(Duration.ofSeconds(15))
            ),

            // Steady creation from feeder
            createBooksFromFeederScenario.injectOpen(
                constantUsersPerSec(3.0).during(Duration.ofSeconds(20))
            ),

            // Mixed operations with moderate load
            mixedOperationsScenario.injectOpen(
                rampUsers(15).during(Duration.ofSeconds(20))
            ),

            // Spike test
            highLoadScenario.injectOpen(
                nothingFor(Duration.ofSeconds(10)),
                atOnceUsers(50)
            )
        ).protocols(httpProtocol)
            .assertions(
                global().responseTime().max().lt(1000),
                global().responseTime().mean().lt(300),
                global().successfulRequests().percent().gt(95.0),
                forAll().failedRequests().percent().lte(5.0)
            )
    }
}
