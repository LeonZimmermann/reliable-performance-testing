package dev.leon.zimmermann.rpt.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.rampUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import dev.leon.zimmermann.rpt.gatling.behaviours.UserBehaviour.browseBooksV2
import dev.leon.zimmermann.rpt.gatling.utils.Authentication
import dev.leon.zimmermann.rpt.gatling.utils.Constants.HIGH_NUMBER_OF_USERS
import dev.leon.zimmermann.rpt.gatling.utils.Constants.LOW_RESPONSE_TIME
import dev.leon.zimmermann.rpt.gatling.utils.setup
import dev.leon.zimmermann.rpt.gatling.utils.seconds

@Suppress("unused")
class BookBrowsingV2Simulation : Simulation() {

    private val scenario = scenario("Browse Books V2")
        .exec(Authentication.login())
        .exec(browseBooksV2())

    init {
        setup(scenario, rampUsers(HIGH_NUMBER_OF_USERS).during(20.seconds), LOW_RESPONSE_TIME)
    }
}
