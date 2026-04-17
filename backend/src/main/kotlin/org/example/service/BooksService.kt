package org.example.service

import org.example.entity.Book as BookEntity
import org.example.generated.model.AuthorSummary
import org.example.generated.model.Book
import org.example.generated.model.BookPage
import org.example.generated.model.NewBook
import org.example.repository.AuthorRepository
import org.example.repository.BookRepository
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class BooksService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository
) {

    fun createBook(newBook: NewBook): Book {
        val entity = BookEntity(
            title = newBook.title,
            author = newBook.author,
            isbn = newBook.isbn,
            price = newBook.price,
            publisher = newBook.publisher
        )
        entity.authors = resolveAuthors(newBook.authorIds)
        return bookRepository.save(entity).toModel()
    }

    @Transactional(readOnly = true)
    fun getAllBooks(): List<Book> = bookRepository.findAll().map { it.toModel() }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    fun getBookById(id: Long): Book {
        return bookRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: $id") }
            .toModel()
    }

    fun updateBook(id: Long, newBook: NewBook): Book {
        val existing = bookRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: $id") }
        val updated = existing.copy(
            title = newBook.title,
            author = newBook.author,
            isbn = newBook.isbn,
            price = newBook.price,
            publisher = newBook.publisher
        )
        updated.authors = resolveAuthors(newBook.authorIds)
        return bookRepository.save(updated).toModel()
    }

    fun deleteBook(id: Long) {
        if (!bookRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: $id")
        }
        bookRepository.deleteById(id)
    }

    private fun resolveAuthors(authorIds: List<Long>?): MutableSet<org.example.entity.Author> =
        authorIds?.takeIf { it.isNotEmpty() }
            ?.let { authorRepository.findAllById(it).toMutableSet() }
            ?: mutableSetOf()

    private fun BookEntity.toModel() = Book(
        id = id!!,
        title = title,
        author = author,
        isbn = isbn,
        price = price,
        publisher = publisher,
        authors = authors.map { AuthorSummary(id = it.id!!, name = it.name) }
    )
}
