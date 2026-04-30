package dev.leon.zimmermann.rpt.gatling.domain

import kotlin.random.Random

data class Book(
    val title: BookTitle,
    val author: AuthorName,
    val isbn: ISBN,
    val price: Price,
    val publisher: Publisher? = null,
) {
    companion object {
        fun generate(): Book = Book(
            title = BookTitle.generate(),
            author = AuthorName.generate(),
            isbn = ISBN.generate(),
            price = Price.generate(),
            publisher = if (Random.nextBoolean()) Publisher.generate() else null,
        )
    }
}
