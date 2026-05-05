package dev.leon.zimmermann.rpt.gatling.domain

import kotlin.random.Random

data class Book(
    val title: Title,
    val author: Name,
    val isbn: ISBN,
    val price: Price,
    val publisher: Publisher? = null,
) {
    companion object {
        fun generate(): Book = Book(
            title = Title.generate(),
            author = Name.generate(),
            isbn = ISBN.generate(),
            price = Price.generate(),
            publisher = if (Random.nextBoolean()) Publisher.generate() else null,
        )
    }

    @JvmInline
    value class Title(val value: String) {
        companion object {
            private val ADJECTIVES = listOf(
                "Lost", "Hidden", "Forgotten", "Silent", "Burning", "Frozen", "Golden",
                "Broken", "Endless", "Ancient", "Hollow", "Crimson", "Dark", "Last",
            )
            private val NOUNS = listOf(
                "Kingdom", "Echo", "Mirror", "Storm", "Shadow", "Path", "Gate",
                "Winter", "Fire", "Hour", "Garden", "Star", "Dream", "River",
            )

            fun generate(): Title =
                Title("${ADJECTIVES.random()} ${NOUNS.random()} ${Random.nextInt(1, 1000)}")
        }
    }
}
