package org.example.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import org.example.gatling.behaviours.UserBehaviour.createManyBooks
import org.example.gatling.scenarios.Constants.HTTP_PROTOCOL
import org.example.gatling.scenarios.Constants.MEDIUM_RESPONSE_TIME

class BookCreationLoadSimulation : Simulation() {

    private val create = scenario("Create Many Books")
        .exec(createManyBooks())

    init {
        setUp(create.injectOpen(atOnceUsers(500)))
            .protocols(HTTP_PROTOCOL)
            .assertions(*MEDIUM_RESPONSE_TIME)
    }
}
