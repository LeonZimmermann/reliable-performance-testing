package org.example.controller

import org.example.generated.api.BooksApi
import org.example.generated.model.Book
import org.example.generated.model.NewBook
import org.example.service.BooksService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class BooksApiImpl(
    private val booksService: BooksService
) : BooksApi {

    override fun createBook(newBook: NewBook): ResponseEntity<Book> {
        return ResponseEntity.status(HttpStatus.CREATED).body(booksService.createBook(newBook))
    }

    override fun getBooks(): ResponseEntity<List<Book>> {
        return ResponseEntity.ok(booksService.getAllBooks())
    }
}
