package org.example.gatling.pages

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*

/**
 * Page object for /books endpoint operations.
 * Encapsulates all interactions with the Books API.
 */
object BooksPage : BasePage() {

    /**
     * GET /books - Retrieve all books
     */
    fun getAllBooks(): ChainBuilder {
        return exec(
            http("Get All Books")
                .get("$baseUrl/books")
                .check(statusIs(200))
                .check(jsonPath("$[*]").ofList().saveAs("booksList"))
        )
    }

    /**
     * POST /books - Create a new book with all required fields
     */
    fun createBook(title: String, author: String, isbn: String, price: Double): ChainBuilder {
        return exec(
            http("Create Book: $title")
                .post("$baseUrl/books")
                .header("Content-Type", "application/json")
                .body(StringBody("""{
                    "title": "$title",
                    "author": "$author",
                    "isbn": "$isbn",
                    "price": $price
                }"""))
                .check(statusIs(201))
                .check(jsonPath("$.id").saveAs("bookId"))
                .check(jsonPath("$.title").saveAs("bookTitle"))
        )
    }

    /**
     * POST /books - Create a new book with optional publisher field
     */
    fun createBookWithPublisher(
        title: String,
        author: String,
        isbn: String,
        price: Double,
        publisher: String
    ): ChainBuilder {
        return exec(
            http("Create Book with Publisher: $title")
                .post("$baseUrl/books")
                .header("Content-Type", "application/json")
                .body(StringBody("""{
                    "title": "$title",
                    "author": "$author",
                    "isbn": "$isbn",
                    "price": $price,
                    "publisher": "$publisher"
                }"""))
                .check(statusIs(201))
                .check(jsonPath("$.id").saveAs("bookId"))
                .check(jsonPath("$.title").saveAs("bookTitle"))
                .check(jsonPath("$.publisher").saveAs("bookPublisher"))
        )
    }

    /**
     * POST /books - Create a book using data from session variables
     * Useful for dynamic test scenarios
     */
    fun createBookFromSession(): ChainBuilder {
        return exec(
            http("Create Book from Session")
                .post("$baseUrl/books")
                .header("Content-Type", "application/json")
                .body(StringBody("""{
                    "title": "#{bookTitle}",
                    "author": "#{bookAuthor}",
                    "isbn": "#{bookIsbn}",
                    "price": #{bookPrice}
                }"""))
                .check(statusIs(201))
                .check(jsonPath("$.id").saveAs("bookId"))
        )
    }

    /**
     * Helper method to set book data in session for later use
     */
    fun prepareBookData(title: String, author: String, isbn: String, price: Double): ChainBuilder {
        return exec { session ->
            session
                .set("bookTitle", title)
                .set("bookAuthor", author)
                .set("bookIsbn", isbn)
                .set("bookPrice", price)
        }
    }
}
