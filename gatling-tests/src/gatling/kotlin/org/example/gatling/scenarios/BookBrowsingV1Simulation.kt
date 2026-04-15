package org.example.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.rampUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import org.example.gatling.behaviours.UserBehaviour.browseBooksV1
import org.example.gatling.scenarios.Constants.HIGH_NUMBER_OF_USERS
import org.example.gatling.scenarios.Constants.HTTP_PROTOCOL
import org.example.gatling.scenarios.Constants.LOW_RESPONSE_TIME
import org.example.gatling.utils.seconds

class BookBrowsingV1Simulation : Simulation() {

    private val browse = scenario("Browse Books V1")
        .exec(browseBooksV1())

    init {
        setUp(browse.injectOpen(rampUsers(HIGH_NUMBER_OF_USERS).during(20.seconds)))
            .protocols(HTTP_PROTOCOL)
            .assertions(*LOW_RESPONSE_TIME)
    }
}
