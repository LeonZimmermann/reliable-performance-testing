package dev.leon.zimmermann.rpt.gatling.utils

import io.gatling.javaapi.core.CoreDsl
import io.gatling.javaapi.http.HttpDsl

object Constants {
    val BASE_URL: String = System.getProperty("baseUrl", "http://localhost:8080")

    val HTTP_PROTOCOL = HttpDsl.http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")

    const val HIGH_NUMBER_OF_USERS = 10_000

    val LOW_RESPONSE_TIME = arrayOf(
        CoreDsl.global().responseTime().max().lt(100),
        CoreDsl.global().responseTime().mean().lt(50),
        CoreDsl.global().successfulRequests().percent().gt(95.0),
    )

    val MEDIUM_RESPONSE_TIME = arrayOf(
        CoreDsl.global().responseTime().max().lt(1000),
        CoreDsl.global().responseTime().mean().lt(300),
        CoreDsl.global().successfulRequests().percent().gt(95.0),
    )
}
