package dev.leon.zimmermann.rpt.gatling.scenarios

import dev.leon.zimmermann.rpt.gatling.pages.BooksPage.getBooks
import dev.leon.zimmermann.rpt.gatling.utils.Authentication
import dev.leon.zimmermann.rpt.gatling.utils.Constants.BASE_URL
import dev.leon.zimmermann.rpt.gatling.utils.Constants.HIGH_NUMBER_OF_USERS
import dev.leon.zimmermann.rpt.gatling.utils.Constants.HTTP_PROTOCOL
import dev.leon.zimmermann.rpt.gatling.utils.Constants.LOW_RESPONSE_TIME
import dev.leon.zimmermann.rpt.gatling.utils.seconds
import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

/*
    A simulation runs a defined set of requests concurrently
 */
@Suppress("unused")
class YourFirstSimulation : Simulation() {

    /*
    The scenario defines what each simulated user should do
     */
    private val scenario = scenario("Browse Books")
        .exec(browseBooks())

    /*
    here we group different actions. We can reuse this method for different scenarios, once we have refactored it and
    put it into a "Page Object"
     */
    fun browseBooks() =
        group("Browse Books").on(
            exec(getAllBooks()),
            pause(1),
            exec(getAllBooks()),
            pause(1),
            exec(getAllBooks()),
        )

    /*
    here we define a specific request: GET /v1/books.
    we store values using saveAs
     */
    fun getAllBooks(): ChainBuilder {
        var req = http("getAllBooks").get("/v1/books")
        return exec(req
            .check(status().`is`(200))
            .check(jsonPath("$[0].id").saveAs("id"))
        )
    }

    val HTTP_PROTOCOL = HttpDsl.http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")

    init {
        /*
        The setUp method starts the scenario and defines how many users should run the simulation.

        We differentiate between open and closed injection profiles.
         */
        setUp(scenario.injectOpen(rampUsers(HIGH_NUMBER_OF_USERS).during(20.seconds)))
            .protocols(HTTP_PROTOCOL)
            .assertions(*LOW_RESPONSE_TIME)
    }
}
