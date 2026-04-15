package org.example.gatling.pages

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*
import org.example.gatling.utils.Constants

/**
 * Base class for all page objects.
 * Provides common functionality for API interactions.
 */
abstract class BasePage {

    protected val baseUrl: String = Constants.BASE_URL

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
