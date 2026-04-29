package dev.leon.zimmermann.rpt.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import dev.leon.zimmermann.rpt.gatling.behaviours.UserBehaviour.createManyBooks
import dev.leon.zimmermann.rpt.gatling.utils.Constants.HTTP_PROTOCOL
import dev.leon.zimmermann.rpt.gatling.utils.Constants.MEDIUM_RESPONSE_TIME
import dev.leon.zimmermann.rpt.gatling.utils.KeycloakAuth

object CreateOneMillionBooksSimulation : Simulation() {

    const val NUMBER_OF_BOOKS_TO_BE_GENERATED = 1_000_000
    const val NUMBER_OF_CONCURRENT_USERS = 500

    private val create = scenario("Create Many Books")
        .exec(KeycloakAuth.fetchToken(username = "admin", password = "admin"))
        .exec(createManyBooks(NUMBER_OF_BOOKS_TO_BE_GENERATED / NUMBER_OF_CONCURRENT_USERS))

    init {
        setUp(create.injectOpen(atOnceUsers(NUMBER_OF_CONCURRENT_USERS)))
            .protocols(HTTP_PROTOCOL)
            .assertions(*MEDIUM_RESPONSE_TIME)
    }
}
