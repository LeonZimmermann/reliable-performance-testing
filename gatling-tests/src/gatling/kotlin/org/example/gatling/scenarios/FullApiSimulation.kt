package org.example.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.*
import org.example.gatling.pages.BooksPage
import org.example.gatling.pages.HelloPage
import java.time.Duration

class FullApiSimulation : Simulation() {

    private val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")

    private val userJourney = scenario("User Journey")
        .exec(HelloPage.getHello("TestUser"))
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.getBooks())
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.createBook("Gatling in Action", "Test Author", "978-0000000001", 39.99))
        .pause(Duration.ofSeconds(1))
        .exec(BooksPage.getBooks())
        .pause(Duration.ofSeconds(1))
        .exec(HelloPage.getHello())

    private val browseOnly = scenario("Browse Only")
        .exec(HelloPage.getHello())
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(3))
        .exec(BooksPage.getBooks())

    private val heavyCreate = scenario("Heavy Create")
        .exec(BooksPage.createBook("Book A", "Author A", "978-1111111111", 19.99))
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.createBook("Book B", "Author B", "978-2222222222", 24.99))
        .pause(Duration.ofMillis(500))
        .exec(BooksPage.getBooks())

    init {
        setUp(
            userJourney.injectOpen(rampUsers(20).during(Duration.ofSeconds(30))),
            browseOnly.injectOpen(rampUsers(50).during(Duration.ofSeconds(30))),
            heavyCreate.injectOpen(rampUsers(10).during(Duration.ofSeconds(20))),
        ).protocols(httpProtocol)
            .assertions(
                global().responseTime().max().lt(2000),
                global().responseTime().mean().lt(500),
                global().successfulRequests().percent().gt(95.0),
            )
            .maxDuration(Duration.ofSeconds(60))
    }
}
