package dev.leon.zimmermann.rpt.gatling.config

object SimulationConfig {
    val isDebugMode: Boolean = System.getProperty("debug", "false").toBoolean()
}
