package dev.leon.zimmermann.rpt.gatling.behaviours

import dev.leon.zimmermann.rpt.gatling.feeders.BookFeeder
import dev.leon.zimmermann.rpt.gatling.pages.BooksPage.createBookFromSession
import io.gatling.javaapi.core.CoreDsl
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.core.CoreDsl.feed

object AdminBehaviour {
    fun createBooks(count: Int = 20) =
        CoreDsl.repeat(count).on(
            feed(BookFeeder.feeder()),
            exec(createBookFromSession()),
        )
}
