package org.example.gatling.scenarios

import io.gatling.javaapi.core.CoreDsl.global
import io.gatling.javaapi.http.HttpDsl.http

object Constants {
    val HTTP_PROTOCOL = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")

    const val HIGH_NUMBER_OF_USERS = 10_000

    val LOW_RESPONSE_TIME = arrayOf(
        global().responseTime().max().lt(100),
        global().responseTime().mean().lt(50),
        global().successfulRequests().percent().gt(95.0),
    )

    val MEDIUM_RESPONSE_TIME = arrayOf(
        global().responseTime().max().lt(1000),
        global().responseTime().mean().lt(300),
        global().successfulRequests().percent().gt(95.0),
    )
}
