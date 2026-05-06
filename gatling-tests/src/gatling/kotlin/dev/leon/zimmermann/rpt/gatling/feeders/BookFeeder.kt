package dev.leon.zimmermann.rpt.gatling.feeders

import dev.leon.zimmermann.rpt.gatling.domain.NewBook
import kotlin.random.Random

object BookFeeder : TestDataGenerator<NewBook> {

    override fun generate(): NewBook = NewBook(
        title = BookTitleFeeder.generate(),
        author = PersonNameFeeder.generate().let { "${it.firstName} ${it.lastName}" },
        isbn = ISBNFeeder.generate(),
        price = PriceFeeder.generate(),
        publisher = if (Random.nextBoolean()) PublisherFeeder.generate() else null,
    )

    override fun toSessionMap(value: NewBook): Map<String, Any> = buildMap {
        put("title", value.title)
        put("author", value.author)
        put("isbn", value.isbn.value)
        put("price", value.price.value)
        value.publisher?.let { put("publisher", it.value) }
    }
}
