package dev.leon.zimmermann.rpt.gatling.scenarios

import dev.leon.zimmermann.rpt.gatling.behaviours.UserBehaviour.browseAndOpenBook
import dev.leon.zimmermann.rpt.gatling.utils.Authentication
import dev.leon.zimmermann.rpt.gatling.utils.Constants.HIGH_NUMBER_OF_USERS
import dev.leon.zimmermann.rpt.gatling.utils.Constants.LOW_RESPONSE_TIME
import dev.leon.zimmermann.rpt.gatling.utils.seconds
import dev.leon.zimmermann.rpt.gatling.utils.setup
import io.gatling.javaapi.core.CoreDsl.rampUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation

@Suppress("unused")
class BookDetailsBrowsingSimulation : Simulation() {

    private val scenario = scenario("Browse and Open Book")
        .exec(Authentication.login())
        .exec(browseAndOpenBook())

    init {
        setup(scenario, rampUsers(HIGH_NUMBER_OF_USERS).during(20.seconds), LOW_RESPONSE_TIME)
    }
}
