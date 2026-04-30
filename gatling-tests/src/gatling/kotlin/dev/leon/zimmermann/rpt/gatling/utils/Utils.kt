package dev.leon.zimmermann.rpt.gatling.utils

import io.gatling.javaapi.core.Assertion
import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.OpenInjectionStep
import io.gatling.javaapi.core.ScenarioBuilder
import io.gatling.javaapi.core.Simulation
import dev.leon.zimmermann.rpt.gatling.config.SimulationConfig
import dev.leon.zimmermann.rpt.gatling.utils.Constants.HTTP_PROTOCOL
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

inline val Int.milliseconds: Duration get() = toDuration(ChronoUnit.MILLIS)
inline val Int.seconds: Duration get() = toDuration(ChronoUnit.SECONDS)
fun Int.toDuration(unit: TemporalUnit): Duration = Duration.of(this.toLong(), unit)

fun Simulation.setup(
    scenario: ScenarioBuilder,
    normalInjection: OpenInjectionStep,
    assertions: Array<Assertion> = emptyArray(),
    debugInjection: OpenInjectionStep = atOnceUsers(1),
) = setUp(scenario.injectOpen(if (SimulationConfig.isDebugMode) debugInjection else normalInjection))
    .protocols(HTTP_PROTOCOL)
    .let { if (SimulationConfig.isDebugMode || assertions.isEmpty()) it else it.assertions(*assertions) }
