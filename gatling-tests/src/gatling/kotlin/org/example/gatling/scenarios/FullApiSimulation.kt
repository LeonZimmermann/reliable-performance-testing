package org.example.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.*
import org.example.gatling.pages.HelloPage
import org.example.gatling.pages.BooksPage
import java.time.Duration

/**
 * Comprehensive simulation testing the entire API.
 * Demonstrates how to combine multiple page objects for complex scenarios.
 */
class FullApiSimulation : Simulation() {

    // HTTP protocol configuration
    private val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")

    // User journey scenario: A typical user interacting with the API
    private val userJourneyScenario = scenario("Complete User Journey")
        // Start with a greeting
        .exec(HelloPage.getPersonalizedGreeting("TestUser"))
        .pause(Duration.ofSeconds(1))

        // Browse books
        .exec(BooksPage.getAllBooks())
        .pause(Duration.ofSeconds(2))

        // Add a new book
        .exec(BooksPage.createBook(
            "Gatling Performance Testing",
            "Test Author",
            "978-1234567890",
            39.99
        ))
        .pause(Duration.ofSeconds(1))

        // Check books again to see the new entry
        .exec(BooksPage.getAllBooks())
        .pause(Duration.ofSeconds(1))

        // Say goodbye
        .exec(HelloPage.getDefaultGreeting())

    // Browse-only scenario: Users who only read data
    private val browseOnlyScenario = scenario("Browse Only Users")
        .exec(HelloPage.getDefaultGreeting())
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(3))
        .exec(BooksPage.getAllBooks())
        .pause(Duration.ofSeconds(2), Duration.ofSeconds(5))
        .exec(BooksPage.getAllBooks())

    // Power user scenario: Heavy book creation
    private val powerUserScenario = scenario("Power Users")
        .exec(BooksPage.createBook("Book 1", "Author 1", "978-1111111111", 19.99))
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.createBook("Book 2", "Author 2", "978-2222222222", 24.99))
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.createBook("Book 3", "Author 3", "978-3333333333", 29.99))
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.getAllBooks())

    // Realistic load simulation with different user types
    init {
        setUp(
            // Regular users following complete journey
            userJourneyScenario.injectOpen(
                rampUsers(30).during(Duration.ofSeconds(30))
            ),

            // Many browsers, few creators (realistic ratio)
            browseOnlyScenario.injectOpen(
                rampUsers(100).during(Duration.ofSeconds(45))
            ),

            // Small number of power users
            powerUserScenario.injectOpen(
                rampUsers(10).during(Duration.ofSeconds(20))
            )
        ).protocols(httpProtocol)
            .assertions(
                global().responseTime().max().lt(2000),
                global().responseTime().mean().lt(500),
                global().successfulRequests().percent().gt(95.0)
            )
            .maxDuration(Duration.ofSeconds(60))
    }
}
