package dev.leon.zimmermann.rpt.gatling.feeders

import dev.leon.zimmermann.rpt.gatling.domain.Price
import kotlin.random.Random

object PriceFeeder : TestDataGenerator<Price> {

    override fun generate(): Price = Price(Random.nextInt(500, 5000) / 100.0)

    override fun toSessionMap(value: Price): Map<String, Any> = mapOf("price" to value.value)
}
