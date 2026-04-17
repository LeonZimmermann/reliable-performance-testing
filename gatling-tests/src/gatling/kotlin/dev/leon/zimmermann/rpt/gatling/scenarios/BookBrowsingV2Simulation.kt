package dev.leon.zimmermann.rpt.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.rampUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import dev.leon.zimmermann.rpt.gatling.behaviours.UserBehaviour.browseBooksV2
import dev.leon.zimmermann.rpt.gatling.utils.Constants.HIGH_NUMBER_OF_USERS
import dev.leon.zimmermann.rpt.gatling.utils.Constants.HTTP_PROTOCOL
import dev.leon.zimmermann.rpt.gatling.utils.Constants.LOW_RESPONSE_TIME
import dev.leon.zimmermann.rpt.gatling.utils.seconds

object BookBrowsingV2Simulation : Simulation() {

    private val browse = scenario("Browse Books V2")
        .exec(browseBooksV2())

    init {
        setUp(browse.injectOpen(rampUsers(HIGH_NUMBER_OF_USERS).during(20.seconds)))
            .protocols(HTTP_PROTOCOL)
            .assertions(*LOW_RESPONSE_TIME)
    }
}
