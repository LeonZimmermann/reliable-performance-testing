package dev.leon.zimmermann.rpt.gatling.feeders

import kotlin.random.Random

object BookTitleFeeder : TestDataGenerator<String> {

    private val ADJECTIVES = listOf(
        "Lost", "Hidden", "Forgotten", "Silent", "Burning", "Frozen", "Golden",
        "Broken", "Endless", "Ancient", "Hollow", "Crimson", "Dark", "Last",
    )
    private val NOUNS = listOf(
        "Kingdom", "Echo", "Mirror", "Storm", "Shadow", "Path", "Gate",
        "Winter", "Fire", "Hour", "Garden", "Star", "Dream", "River",
    )

    override fun generate(): String = "${ADJECTIVES.random()} ${NOUNS.random()} ${Random.nextInt(1, 1000)}"

    override fun toSessionMap(value: String): Map<String, Any> = mapOf("title" to value)
}
