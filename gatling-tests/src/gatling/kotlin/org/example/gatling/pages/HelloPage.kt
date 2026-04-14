package org.example.gatling.pages

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*

/**
 * Page object for /hello endpoint operations.
 * Encapsulates all interactions with the Hello API.
 */
object HelloPage : BasePage() {

    /**
     * GET /hello without name parameter
     * Returns a default greeting
     */
    fun getDefaultGreeting(): ChainBuilder {
        return exec(
            http("Get Default Greeting")
                .get("$baseUrl/hello")
                .check(statusIs(200))
                .check(jsonPath("$.message").exists())
                .check(jsonPath("$.message").saveAs("greeting"))
        )
    }

    /**
     * GET /hello with name parameter
     * Returns a personalized greeting
     */
    fun getPersonalizedGreeting(name: String): ChainBuilder {
        return exec(
            http("Get Greeting for $name")
                .get("$baseUrl/hello")
                .queryParam("name", name)
                .check(statusIs(200))
                .check(jsonPath("$.message").exists())
                .check(jsonPath("$.message").saveAs("greeting"))
        )
    }

    /**
     * GET /hello with name from session
     * Useful for dynamic scenarios where name is retrieved from previous steps
     */
    fun getGreetingFromSession(): ChainBuilder {
        return exec { session ->
            val userName = session.getString("userName")
            session
        }.exec(
            http("Get Greeting with Session Name")
                .get("$baseUrl/hello")
                .queryParam("name", "#{userName}")
                .check(statusIs(200))
                .check(jsonPath("$.message").exists())
                .check(jsonPath("$.message").saveAs("greeting"))
        )
    }
}
