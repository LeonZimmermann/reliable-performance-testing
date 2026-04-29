package dev.leon.zimmermann.rpt.gatling.utils

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*
import dev.leon.zimmermann.rpt.gatling.config.AuthenticationConfig

object Authentication {

    private val TOKEN_URL = AuthenticationConfig.tokenUrl

    // Refresh when less than 30 seconds remain on the current token
    private const val REFRESH_BUFFER_MS = 30_000L

    fun fetchToken(
        username: String = AuthenticationConfig.username,
        password: String = AuthenticationConfig.password,
    ): ChainBuilder =
        exec(
            http("Fetch Token")
                .post(TOKEN_URL)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", AuthenticationConfig.clientId)
                .formParam("username", username)
                .formParam("password", password)
                .check(
                    status().`is`(200),
                    jsonPath("$.access_token").saveAs("accessToken"),
                    jsonPath("$.refresh_token").saveAs("refreshToken"),
                    jsonPath("$.expires_in").ofLong().saveAs("tokenExpiresIn"),
                )
        ).exec { session ->
            session.set("tokenExpiresAt", System.currentTimeMillis() + session.getLong("tokenExpiresIn") * 1000L)
        }

    val refreshToken: ChainBuilder =
        exec(
            http("Refresh Token")
                .post(TOKEN_URL)
                .header("Content-Type", "application/x-www-form-urlencoded")
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
            session.set("tokenExpiresAt", System.currentTimeMillis() + session.getLong("tokenExpiresIn") * 1000L)
        }

    // Call before authenticated request blocks to ensure the token is still valid
    val ensureValidToken: ChainBuilder =
        doIf { session ->
            !session.contains("tokenExpiresAt") ||
                System.currentTimeMillis() >= session.getLong("tokenExpiresAt") - REFRESH_BUFFER_MS
        }.then(refreshToken)
}
