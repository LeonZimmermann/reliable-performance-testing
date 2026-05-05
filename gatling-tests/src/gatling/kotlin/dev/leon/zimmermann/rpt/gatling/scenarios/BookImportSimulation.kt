package dev.leon.zimmermann.rpt.gatling.scenarios

import dev.leon.zimmermann.rpt.gatling.behaviours.AdminBehaviour.createBooks
import dev.leon.zimmermann.rpt.gatling.utils.Authentication
import dev.leon.zimmermann.rpt.gatling.utils.Constants.MEDIUM_RESPONSE_TIME
import dev.leon.zimmermann.rpt.gatling.utils.setup
import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import kotlin.random.Random

@Suppress("unused")
class BookImportSimulation : Simulation() {

    companion object {
        const val NUMBER_OF_BOOKS_TO_BE_GENERATED = 1_000_000
        const val NUMBER_OF_CONCURRENT_USERS = 1
    }

    // dedicated Book Feeder should be used instead
    private val bookFeeder = intArrayOf(100).map { index ->
        buildMap {
            put("title", "Title $index")
            put("author", "Author")
            put("isbn", "978-11-123-${10000 + index}-21")
            put("price", Random.nextInt(10, 20))
            put("publisher", "Publisher")
        }
    }.iterator()

    private val scenario = scenario("Create Many Books")
        .exec(Authentication.login())
        .feed(bookFeeder)
        .exec(createBooks(NUMBER_OF_BOOKS_TO_BE_GENERATED))

    init {
        setup(scenario, atOnceUsers(NUMBER_OF_CONCURRENT_USERS), MEDIUM_RESPONSE_TIME)
    }
}
