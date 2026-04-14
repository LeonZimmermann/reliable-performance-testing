package org.example.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.*
import org.example.gatling.pages.HelloPage
import java.time.Duration

class HelloSimulation : Simulation() {

    private val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")

    private val defaultGreeting = scenario("Default Greeting")
        .exec(HelloPage.getHello())
        .pause(Duration.ofSeconds(1))

    private val namedGreeting = scenario("Named Greeting")
        .exec(HelloPage.getHello("Alice"))
        .pause(Duration.ofSeconds(1))
        .exec(HelloPage.getHello("Bob"))

    private val names = listFeeder(listOf(
        mapOf("name" to "Carol"),
        mapOf("name" to "Dave"),
        mapOf("name" to "Eve"),
    )).random()

    private val dynamicGreeting = scenario("Dynamic Greeting")
        .feed(names)
        .exec(HelloPage.getHelloFromSession())

    init {
        setUp(
            defaultGreeting.injectOpen(rampUsers(10).during(Duration.ofSeconds(10))),
            namedGreeting.injectOpen(rampUsers(10).during(Duration.ofSeconds(10))),
            dynamicGreeting.injectOpen(constantUsersPerSec(2.0).during(Duration.ofSeconds(10))),
        ).protocols(httpProtocol)
            .assertions(
                global().responseTime().max().lt(500),
                global().successfulRequests().percent().gt(95.0),
            )
    }
}
