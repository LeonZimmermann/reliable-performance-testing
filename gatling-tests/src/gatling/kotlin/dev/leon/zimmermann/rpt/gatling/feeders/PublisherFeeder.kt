package dev.leon.zimmermann.rpt.gatling.feeders

import dev.leon.zimmermann.rpt.gatling.domain.Publisher

object PublisherFeeder : TestDataGenerator<Publisher> {

    private val PUBLISHERS = listOf(
        "Horizon Books", "Page Turner Press", "Novel House", "Ink & Story",
        "The Written Word", "Lighthouse Publishing", "Ember Press",
    )

    override fun generate(): Publisher = Publisher(PUBLISHERS.random())

    override fun toSessionMap(value: Publisher): Map<String, Any> = mapOf("publisher" to value.value)
}
