package org.example.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.*
import org.example.gatling.pages.HelloPage
import java.time.Duration

/**
 * Simple load test simulation for the Hello endpoint.
 * Demonstrates basic usage of the HelloPage page object.
 */
class HelloSimulation : Simulation() {

    // HTTP protocol configuration
    private val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")

    // Scenario 1: Test default greeting
    private val defaultGreetingScenario = scenario("Default Greeting")
        .exec(HelloPage.getDefaultGreeting())
        .pause(Duration.ofSeconds(1))

    // Scenario 2: Test personalized greetings with different names
    private val personalizedGreetingScenario = scenario("Personalized Greeting")
        .exec(HelloPage.getPersonalizedGreeting("Alice"))
        .pause(Duration.ofSeconds(1))
        .exec(HelloPage.getPersonalizedGreeting("Bob"))
        .pause(Duration.ofSeconds(1))
        .exec(HelloPage.getPersonalizedGreeting("Charlie"))

    // Scenario 3: Test with dynamic names from a feeder
    private val nameFeeder = listFeeder(listOf(
        mapOf("userName" to "David"),
        mapOf("userName" to "Emma"),
        mapOf("userName" to "Frank"),
        mapOf("userName" to "Grace")
    )).random()

    private val dynamicGreetingScenario = scenario("Dynamic Greeting")
        .feed(nameFeeder)
        .exec(HelloPage.getGreetingFromSession())
        .pause(Duration.ofSeconds(1))

    // Load simulation setup
    init {
        setUp(
            defaultGreetingScenario.injectOpen(
                rampUsers(10).during(Duration.ofSeconds(5))
            ),
            personalizedGreetingScenario.injectOpen(
                rampUsers(20).during(Duration.ofSeconds(10))
            ),
            dynamicGreetingScenario.injectOpen(
                constantUsersPerSec(5.0).during(Duration.ofSeconds(10))
            )
        ).protocols(httpProtocol)
            .assertions(
                global().responseTime().max().lt(500),
                global().successfulRequests().percent().gt(95.0)
            )
    }
}
