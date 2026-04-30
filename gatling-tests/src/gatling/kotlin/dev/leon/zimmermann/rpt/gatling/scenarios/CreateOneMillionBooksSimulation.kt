package dev.leon.zimmermann.rpt.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import dev.leon.zimmermann.rpt.gatling.behaviours.UserBehaviour.createManyBooks
import dev.leon.zimmermann.rpt.gatling.utils.Constants.MEDIUM_RESPONSE_TIME
import dev.leon.zimmermann.rpt.gatling.utils.Authentication
import dev.leon.zimmermann.rpt.gatling.utils.setup

class CreateOneMillionBooksSimulation : Simulation() {

    companion object {
        const val NUMBER_OF_BOOKS_TO_BE_GENERATED = 1_000_000
        const val NUMBER_OF_CONCURRENT_USERS = 500
    }

    private val create = scenario("Create Many Books")
        .exec(Authentication.login())
        .exec(createManyBooks(NUMBER_OF_BOOKS_TO_BE_GENERATED / NUMBER_OF_CONCURRENT_USERS))

    init {
        setup(create, atOnceUsers(NUMBER_OF_CONCURRENT_USERS), MEDIUM_RESPONSE_TIME)
    }
}
