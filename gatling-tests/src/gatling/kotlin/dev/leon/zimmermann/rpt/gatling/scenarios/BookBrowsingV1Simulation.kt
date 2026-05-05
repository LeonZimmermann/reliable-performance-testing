package dev.leon.zimmermann.rpt.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.rampUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import dev.leon.zimmermann.rpt.gatling.behaviours.UserBehaviour.browseBooksV1
import dev.leon.zimmermann.rpt.gatling.utils.Authentication
import dev.leon.zimmermann.rpt.gatling.utils.Constants.HIGH_NUMBER_OF_USERS
import dev.leon.zimmermann.rpt.gatling.utils.Constants.LOW_RESPONSE_TIME
import dev.leon.zimmermann.rpt.gatling.utils.setup
import dev.leon.zimmermann.rpt.gatling.utils.seconds

@Suppress("unused")
class BookBrowsingV1Simulation : Simulation() {

    private val browse = scenario("Browse Books V1")
        .exec(Authentication.login())
        .exec(browseBooksV1())

    init {
        setup(browse, rampUsers(HIGH_NUMBER_OF_USERS).during(20.seconds), LOW_RESPONSE_TIME)
    }
}
