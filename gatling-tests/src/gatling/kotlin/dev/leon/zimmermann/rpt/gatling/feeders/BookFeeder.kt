package dev.leon.zimmermann.rpt.gatling.feeders

import kotlin.random.Random

object BookFeeder {

    private val TITLE_ADJECTIVES = listOf(
        "Lost", "Hidden", "Forgotten", "Silent", "Burning", "Frozen", "Golden",
        "Broken", "Endless", "Ancient", "Hollow", "Crimson", "Dark", "Last",
    )
    private val TITLE_NOUNS = listOf(
        "Kingdom", "Echo", "Mirror", "Storm", "Shadow", "Path", "Gate",
        "Winter", "Fire", "Hour", "Garden", "Star", "Dream", "River",
    )
    private val FIRST_NAMES = listOf(
        "Alice", "Bob", "Clara", "David", "Elena", "Frank", "Grace",
        "Henry", "Iris", "James", "Karen", "Liam", "Maya", "Noah",
    )
    private val LAST_NAMES = listOf(
        "Ashford", "Blake", "Chen", "Drake", "Evans", "Fischer", "Grant",
        "Hayes", "Irons", "Jung", "Klein", "Lowe", "Marsh", "Nash",
    )
    private val PUBLISHERS = listOf(
        "Horizon Books", "Page Turner Press", "Novel House", "Ink & Story",
        "The Written Word", "Lighthouse Publishing", "Ember Press",
    )

    /**
     * Returns an infinite iterator of book records. Each record contains the
     * four required NewBook fields: title, author, isbn, and price.
     * An optional publisher is included on roughly half of all records.
     */
    fun books(): Iterator<Map<String, Any>> =
        generateSequence { nextBook() }.iterator()

    private fun nextBook(): Map<String, Any> {
        val record = mutableMapOf<String, Any>(
            "title" to "${TITLE_ADJECTIVES.random()} ${TITLE_NOUNS.random()} ${Random.nextInt(1, 1000)}",
            "author" to "${FIRST_NAMES.random()} ${LAST_NAMES.random()}",
            "isbn" to generateIsbn(),
            "price" to (Random.nextInt(500, 5000) / 100.0),
        )
        if (Random.nextBoolean()) {
            record["publisher"] = PUBLISHERS.random()
        }
        return record
    }

    private fun generateIsbn(): String {
        val part1 = Random.nextInt(0, 10)
        val part2 = Random.nextInt(100, 999)
        val part3 = Random.nextInt(10000, 99999)
        val part4 = Random.nextInt(0, 10)
        return "978-$part1-$part2-$part3-$part4"
    }
}
