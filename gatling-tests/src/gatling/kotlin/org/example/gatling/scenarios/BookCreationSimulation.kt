package org.example.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.rampUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import org.example.gatling.behaviours.UserBehaviour.createBook
import org.example.gatling.feeders.BookFeeder
import org.example.gatling.scenarios.Constants.HIGH_NUMBER_OF_USERS
import org.example.gatling.scenarios.Constants.HTTP_PROTOCOL
import org.example.gatling.scenarios.Constants.MEDIUM_RESPONSE_TIME
import org.example.gatling.utils.seconds

class BookCreationSimulation : Simulation() {

    private val create = scenario("Create Books")
        .feed(BookFeeder.books())
        .exec(createBook())

    init {
        setUp(create.injectOpen(rampUsers(HIGH_NUMBER_OF_USERS).during(20.seconds)))
            .protocols(HTTP_PROTOCOL)
            .assertions(*MEDIUM_RESPONSE_TIME)
    }
}
