package dev.leon.zimmermann.rpt.gatling.domain

import kotlin.random.Random

@JvmInline
value class BookTitle(val value: String) {
    companion object {
        private val ADJECTIVES = listOf(
            "Lost", "Hidden", "Forgotten", "Silent", "Burning", "Frozen", "Golden",
            "Broken", "Endless", "Ancient", "Hollow", "Crimson", "Dark", "Last",
        )
        private val NOUNS = listOf(
            "Kingdom", "Echo", "Mirror", "Storm", "Shadow", "Path", "Gate",
            "Winter", "Fire", "Hour", "Garden", "Star", "Dream", "River",
        )

        fun generate(): BookTitle =
            BookTitle("${ADJECTIVES.random()} ${NOUNS.random()} ${Random.nextInt(1, 1000)}")
    }
}
