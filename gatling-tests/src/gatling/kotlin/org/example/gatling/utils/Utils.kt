package org.example.gatling.utils

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

inline val Int.milliseconds: Duration get() = toDuration(ChronoUnit.MILLIS)
inline val Int.seconds: Duration get() = toDuration(ChronoUnit.SECONDS)
fun Int.toDuration(unit: TemporalUnit): Duration = Duration.of(this.toLong(), unit)
