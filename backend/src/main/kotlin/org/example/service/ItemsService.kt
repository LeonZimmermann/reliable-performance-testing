package org.example.service

import org.example.entity.Book as BookEntity
import org.example.generated.model.Book
import org.example.generated.model.NewBook
import org.example.repository.BookRepository
import org.springframework.stereotype.Service

@Service
class BooksService(
    private val bookRepository: BookRepository
) {

    fun createBook(newBook: NewBook): Book {
        with(
            bookRepository.save(
                BookEntity(
                    title = newBook.title,
                    author = newBook.author,
                    isbn = newBook.isbn,
                    price = newBook.price,
                    publisher = newBook.publisher
                )
            )
        ) {
            return Book(
                id = id!!,
                title = title,
                author = author,
                isbn = isbn,
                price = price,
                publisher = publisher
            )
        }
    }

    fun getAllBooks(): List<Book> {
        return bookRepository.findAll().map { bookEntity ->
            Book(
                id = bookEntity.id!!,
                title = bookEntity.title,
                author = bookEntity.author,
                isbn = bookEntity.isbn,
                price = bookEntity.price,
                publisher = bookEntity.publisher
            )
        }
    }
}
