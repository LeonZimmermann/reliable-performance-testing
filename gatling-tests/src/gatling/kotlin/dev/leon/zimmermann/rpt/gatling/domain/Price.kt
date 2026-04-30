package dev.leon.zimmermann.rpt.gatling.domain

import kotlin.random.Random

@JvmInline
value class Price(val value: Double) {
    companion object {
        fun generate(): Price = Price(Random.nextInt(500, 5000) / 100.0)
    }
}
