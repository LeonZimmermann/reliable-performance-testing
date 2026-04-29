package dev.leon.zimmermann.rpt.gatling.config

object AuthenticationConfig {
    val url: String = System.getProperty("url", "http://localhost:8180")
    val realm: String = System.getProperty("realm", "bookstore")
    val clientId: String = System.getProperty("clientId", "bookstore-frontend")
    val tokenUrl: String = "$url/realms/$realm/protocol/openid-connect/token"
    val username: String = System.getProperty("adminUsername", "admin")
    val password: String = System.getProperty("adminPassword", "admin")
}
