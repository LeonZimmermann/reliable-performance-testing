package dev.leon.zimmermann.rpt.gatling.feeders

import dev.leon.zimmermann.rpt.gatling.domain.ISBN
import dev.leon.zimmermann.rpt.gatling.domain.NewBook
import dev.leon.zimmermann.rpt.gatling.domain.Price
import dev.leon.zimmermann.rpt.gatling.domain.Publisher
import kotlin.random.Random

object BookFeeder : TestDataGenerator<NewBook> {

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

    override fun generate(): NewBook = NewBook(
        title = "${TITLE_ADJECTIVES.random()} ${TITLE_NOUNS.random()} ${Random.nextInt(1, 1000)}",
        author = "${FIRST_NAMES.random()} ${LAST_NAMES.random()}",
        isbn = generateISBN(),
        price = Price(Random.nextInt(500, 5000) / 100.0),
        publisher = if (Random.nextBoolean()) Publisher(PUBLISHERS.random()) else null,
    )

    override fun toSessionMap(value: NewBook): Map<String, Any> = buildMap {
        put("title", value.title)
        put("author", value.author)
        put("isbn", value.isbn.value)
        put("price", value.price.value)
        value.publisher?.let { put("publisher", it.value) }
    }

    private fun generateISBN(): ISBN {
        val registrant = Random.nextInt(0, 10)
        val publication = Random.nextInt(100, 1000)
        val title = Random.nextInt(10000, 100000)
        val base = "978$registrant$publication$title"
        val checkDigit = calculateIsbnCheckDigit(base)
        return ISBN("978-$registrant-$publication-$title-$checkDigit")
    }

    private fun calculateIsbnCheckDigit(digits: String): Int {
        require(digits.length == 12) { "ISBN-13 base must be 12 digits" }
        val sum = digits.mapIndexed { i, c -> c.digitToInt() * if (i % 2 == 0) 1 else 3 }.sum()
        val remainder = sum % 10
        return if (remainder == 0) 0 else 10 - remainder
    }
}
