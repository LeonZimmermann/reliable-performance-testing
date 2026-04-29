package dev.leon.zimmermann.rpt.gatling.config

object BackendConfig {
    val baseUrl: String = System.getProperty("baseUrl", "http://localhost:8080")
}
