package org.example.gatling.pages

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*

/**
 * Base class for all page objects.
 * Provides common functionality for API interactions.
 */
abstract class BasePage {

    protected val baseUrl: String = System.getenv("GATLING_BASE_URL") ?: "http://localhost:8080"

    /**
     * Helper method to create a named check for status code
     */
    protected fun statusIs(code: Int) = status().`is`(code)

    /**
     * Helper method to extract and save a JSON field
     */
    protected fun saveJsonField(fieldName: String, sessionKey: String) =
        jsonPath("$$.$fieldName").saveAs(sessionKey)
}
