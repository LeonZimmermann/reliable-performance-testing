package dev.leon.zimmermann.rpt.controller

import dev.leon.zimmermann.rpt.generated.api.BooksApi
import dev.leon.zimmermann.rpt.generated.model.Book
import dev.leon.zimmermann.rpt.generated.model.BookPage
import dev.leon.zimmermann.rpt.generated.model.NewBook
import dev.leon.zimmermann.rpt.service.BooksService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class BooksApiImpl(private val booksService: BooksService) : BooksApi {

    override fun getAllBooks(): ResponseEntity<List<Book>> =
        ResponseEntity.ok(booksService.getAllBooks())

    override fun createBook(newBook: NewBook): ResponseEntity<Book> =
        ResponseEntity.status(HttpStatus.CREATED).body(booksService.createBook(newBook))

    override fun getBooks(page: Int, size: Int): ResponseEntity<BookPage> =
        ResponseEntity.ok(booksService.getBooks(page, size))

    override fun getBookById(id: Long): ResponseEntity<Book> =
        ResponseEntity.ok(booksService.getBookById(id))

    override fun updateBook(id: Long, newBook: NewBook): ResponseEntity<Book> =
        ResponseEntity.ok(booksService.updateBook(id, newBook))

    override fun deleteBook(id: Long): ResponseEntity<Unit> {
        booksService.deleteBook(id)
        return ResponseEntity.noContent().build()
    }
}
