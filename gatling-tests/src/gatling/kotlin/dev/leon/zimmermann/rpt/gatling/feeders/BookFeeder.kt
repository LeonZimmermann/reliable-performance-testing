package dev.leon.zimmermann.rpt.gatling.feeders

import dev.leon.zimmermann.rpt.gatling.domain.NewBook
import dev.leon.zimmermann.rpt.gatling.domain.ISBN
import dev.leon.zimmermann.rpt.gatling.domain.Name
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

    override fun generate(): NewBook = NewBook(
        title = "${TITLE_ADJECTIVES.random()} ${TITLE_NOUNS.random()} ${Random.nextInt(1, 1000)}",
        author = Name.generate().fullName,
        isbn = ISBN.generate().value,
        price = Price.generate().value,
        publisher = if (Random.nextBoolean()) Publisher.generate().value else null,
    )

    override fun toSessionMap(value: NewBook): Map<String, Any> = buildMap {
        put("title", value.title)
        put("author", value.author)
        put("isbn", value.isbn)
        put("price", value.price)
        value.publisher?.let { put("publisher", it) }
    }
}
