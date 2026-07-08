package dev.leon.zimmermann.rpt.gatling.utils

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*
import dev.leon.zimmermann.rpt.gatling.config.AuthenticationConfig
import org.slf4j.LoggerFactory
import kotlin.collections.remove
import kotlin.times

object Authentication {

    private val TOKEN_URL = AuthenticationConfig.tokenUrl

    // Refresh when less than 30 seconds remain on the current token
    private const val REFRESH_BUFFER_MS = 30_000L

    private var tokenExpiresAt: Long? = null
    private var accessToken: String? = null

    fun login(
        username: String = AuthenticationConfig.username,
        password: String = AuthenticationConfig.password,
    ): ChainBuilder = doIf { accessToken == null }.then(
        exec(
            http("Login")
                .post(TOKEN_URL)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "")
                .formParam("grant_type", "password")
                .formParam("client_id", AuthenticationConfig.clientId)
                .formParam("username", username)
                .formParam("password", password)
                .check(
                    status().`is`(200),
                    bodyString().saveAs("loginResponseBody"),
                    jsonPath("$.access_token").saveAs("accessToken"),
                    jsonPath("$.refresh_token").saveAs("refreshToken"),
                    jsonPath("$.expires_in").ofLong().saveAs("tokenExpiresIn"),
                )
        ).exec { session ->
            tokenExpiresAt = System.currentTimeMillis() + session.getLong("tokenExpiresIn") * 1000L
            accessToken = session.getString("accessToken")
            session
        }).exec { session -> session.set("accessToken", accessToken) }

    val refreshToken: ChainBuilder =
        exec(
            http("Refresh Token")
                .post(TOKEN_URL)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "")
                .formParam("grant_type", "refresh_token")
                .formParam("client_id", AuthenticationConfig.clientId)
                .formParam("refresh_token", "#{refreshToken}")
                .check(
                    status().`is`(200),
                    jsonPath("$.access_token").saveAs("accessToken"),
                    jsonPath("$.refresh_token").saveAs("refreshToken"),
                    jsonPath("$.expires_in").ofLong().saveAs("tokenExpiresIn"),
                )
        ).exec { session ->
            tokenExpiresAt = System.currentTimeMillis() + session.getLong("tokenExpiresIn") * 1000L
            accessToken = session.getString("accessToken")
            session
        }

    // Call before authenticated request blocks to ensure the token is still valid
    val ensureValidToken: ChainBuilder =
        doIf {
            tokenExpiresAt == null ||
                    System.currentTimeMillis() >= tokenExpiresAt!! - REFRESH_BUFFER_MS
        }.then(refreshToken)
}
