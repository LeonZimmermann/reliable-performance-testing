package org.example.service

import org.example.entity.Book as BookEntity
import org.example.generated.model.Book
import org.example.generated.model.BookPage
import org.example.generated.model.NewBook
import org.example.repository.BookRepository
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class BooksService(private val bookRepository: BookRepository) {

    fun createBook(newBook: NewBook): Book {
        return bookRepository.save(newBook.toEntity()).toModel()
    }

    fun getBooks(page: Int, size: Int): BookPage {
        val result = bookRepository.findAll(PageRequest.of(page, size))
        return BookPage(
            content = result.content.map { it.toModel() },
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            propertySize = result.size,
            number = result.number
        )
    }

    fun getBookById(id: Long): Book {
        return bookRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: $id") }
            .toModel()
    }

    fun updateBook(id: Long, newBook: NewBook): Book {
        val existing = bookRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: $id") }
        return bookRepository.save(existing.copy(
            title = newBook.title,
            author = newBook.author,
            isbn = newBook.isbn,
            price = newBook.price,
            publisher = newBook.publisher
        )).toModel()
    }

    fun deleteBook(id: Long) {
        if (!bookRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: $id")
        }
        bookRepository.deleteById(id)
    }

    private fun NewBook.toEntity() = BookEntity(
        title = title,
        author = author,
        isbn = isbn,
        price = price,
        publisher = publisher
    )

    private fun BookEntity.toModel() = Book(
        id = id!!,
        title = title,
        author = author,
        isbn = isbn,
        price = price,
        publisher = publisher
    )
}
