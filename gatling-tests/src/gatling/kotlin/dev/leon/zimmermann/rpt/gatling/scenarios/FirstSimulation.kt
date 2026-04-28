package dev.leon.zimmermann.rpt.gatling.scenarios

import dev.leon.zimmermann.rpt.gatling.utils.Constants
import dev.leon.zimmermann.rpt.gatling.utils.Constants.HTTP_PROTOCOL
import dev.leon.zimmermann.rpt.gatling.utils.seconds
import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

object FirstSimulation : Simulation() {

    private val browse = scenario("Browse Books V1")
        .exec(
            group("Browse Books").on(
                exec(getBooks()),
                pause(1),
                exec(getBooks()),
                pause(1),
                exec(getBooks())
            )
        )

    fun getBooks(): ChainBuilder {
        return exec(
            http("getBooks")
                .get("${Constants.BASE_URL}/books")
                .queryParam("page", 1)
                .queryParam("size", 10)
                .check(status().`is`(200))
                .check(jsonPath("$.content").saveAs("content"))
                .check(jsonPath("$.totalElements").saveAs("totalElements"))
                .check(jsonPath("$.totalPages").saveAs("totalPages"))
                .check(jsonPath("$.size").saveAs("size"))
                .check(jsonPath("$.number").saveAs("number"))
        )
    }

    init {
        setUp(browse.injectOpen(rampUsers(10_000).during(20.seconds)))
            .protocols(HTTP_PROTOCOL)
            .assertions(
                global().responseTime().max().lt(100),
                global().responseTime().mean().lt(50),
                global().successfulRequests().percent().gt(95.0),
            )
    }
}
