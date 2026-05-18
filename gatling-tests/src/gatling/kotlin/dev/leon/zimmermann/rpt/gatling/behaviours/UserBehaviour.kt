package dev.leon.zimmermann.rpt.gatling.behaviours

import io.gatling.javaapi.core.CoreDsl.*
import dev.leon.zimmermann.rpt.gatling.feeders.BookFeeder
import dev.leon.zimmermann.rpt.gatling.pages.AuthorsPage.getAuthors
import dev.leon.zimmermann.rpt.gatling.pages.BooksPage.createBookFromSession
import dev.leon.zimmermann.rpt.gatling.pages.BooksPage.getAllBooks
import dev.leon.zimmermann.rpt.gatling.pages.BooksPage.getBookByIdFromSession
import dev.leon.zimmermann.rpt.gatling.pages.BooksPage.getBooks

object UserBehaviour {
    fun browseBooksV1() =
        group("Browse Books").on(
            exec(getBooks()),
            pause(1),
            exec(getBooks()),
            pause(1),
            exec(getBooks()),
        )

    fun browseBooksV2(pageSize: Int = 50) =
        group("Browse Books").on(
            exec(getBooks()),
            pause(1),
            exec(getBooks(page = 0, size = pageSize)),
            pause(1),
            exec(getBooks(page = 0, size = pageSize)),
        )

    fun browseAuthors(pageSize: Int = 50) =
        group("Browse Authors").on(
            exec(getAuthors()),
            pause(1),
            exec(getAuthors(page = 0, size = pageSize)),
            pause(1),
            exec(getAuthors(page = 0, size = pageSize)),
        )

    fun browseAndOpenBook() =
        group("Browse and Open Book").on(
            exec(getAllBooks()),
            pause(1),
            exec(getBookByIdFromSession()),
        )
}
