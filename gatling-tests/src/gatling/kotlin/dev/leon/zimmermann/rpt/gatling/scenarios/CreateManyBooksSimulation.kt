package dev.leon.zimmermann.rpt.gatling.scenarios

import dev.leon.zimmermann.rpt.gatling.behaviours.AdminBehaviour.createBooks
import dev.leon.zimmermann.rpt.gatling.utils.Authentication
import dev.leon.zimmermann.rpt.gatling.utils.Constants.MEDIUM_RESPONSE_TIME
import dev.leon.zimmermann.rpt.gatling.utils.setup
import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation

@Suppress("unused")
class CreateManyBooksSimulation : Simulation() {

    companion object {
        const val NUMBER_OF_BOOKS_TO_BE_GENERATED = 10_000
        const val NUMBER_OF_CONCURRENT_USERS = 500
    }

    private val scenario = scenario("Create Many Books")
        .exec(Authentication.login())
        .exec(createBooks(NUMBER_OF_BOOKS_TO_BE_GENERATED / NUMBER_OF_CONCURRENT_USERS))

    init {
        setup(scenario, atOnceUsers(NUMBER_OF_CONCURRENT_USERS), MEDIUM_RESPONSE_TIME)
    }
}
