package org.example.controller

import org.example.generated.api.BooksApi
import org.example.generated.model.Book
import org.example.generated.model.BookPage
import org.example.generated.model.NewBook
import org.example.service.BooksService
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
