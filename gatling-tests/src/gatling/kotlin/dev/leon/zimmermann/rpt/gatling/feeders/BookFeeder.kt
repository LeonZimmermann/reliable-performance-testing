package dev.leon.zimmermann.rpt.gatling.feeders

import dev.leon.zimmermann.rpt.gatling.domain.Book

object BookFeeder : TestDataGenerator<Book> {

    override fun generate(): Book = Book.generate()

    override fun toSessionMap(value: Book): Map<String, Any> = buildMap {
        put("title", value.title.value)
        put("author", value.author.fullName)
        put("isbn", value.isbn.value)
        put("price", value.price.value)
        value.publisher?.let { put("publisher", it.value) }
    }

    fun books(): Iterator<Map<String, Any>> = DomainFeeder.of(this)
}
