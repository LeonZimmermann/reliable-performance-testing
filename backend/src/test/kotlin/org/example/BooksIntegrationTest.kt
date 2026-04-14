package org.example

import org.example.entity.Book
import org.example.repository.BookRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@SpringBootTest
@AutoConfigureMockMvc
class BooksIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var bookRepository: BookRepository

    @BeforeEach
    fun setUp() {
        bookRepository.deleteAll()
    }

    private fun savedBook(title: String = "Test Book", isbn: String = "978-0-000000-00-0") =
        bookRepository.save(Book(title = title, author = "Author", isbn = isbn, price = 9.99))

    @Test
    fun `POST books - creates a book and returns 201`() {
        mockMvc.post("/books") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"Clean Code","author":"Robert Martin","isbn":"978-0-13-235088-4","price":29.99}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { exists() }
            jsonPath("$.title") { value("Clean Code") }
            jsonPath("$.author") { value("Robert Martin") }
            jsonPath("$.isbn") { value("978-0-13-235088-4") }
            jsonPath("$.price") { value(29.99) }
        }
    }

    @Test
    fun `GET books - returns paginated list of books`() {
        savedBook("Book One", "isbn-001")
        savedBook("Book Two", "isbn-002")

        mockMvc.get("/books?page=0&size=10").andExpect {
            status { isOk() }
            jsonPath("$.content") { isArray() }
            jsonPath("$.content.length()") { value(2) }
            jsonPath("$.totalElements") { value(2) }
            jsonPath("$.totalPages") { value(1) }
            jsonPath("$.size") { value(10) }
            jsonPath("$.number") { value(0) }
        }
    }

    @Test
    fun `GET books - respects page and size parameters`() {
        repeat(5) { i -> savedBook("Book $i", "isbn-$i") }

        mockMvc.get("/books?page=0&size=3").andExpect {
            status { isOk() }
            jsonPath("$.content.length()") { value(3) }
            jsonPath("$.totalElements") { value(5) }
            jsonPath("$.totalPages") { value(2) }
        }

        mockMvc.get("/books?page=1&size=3").andExpect {
            status { isOk() }
            jsonPath("$.content.length()") { value(2) }
        }
    }

    @Test
    fun `GET books by id - returns book when found`() {
        val book = savedBook()

        mockMvc.get("/books/${book.id}").andExpect {
            status { isOk() }
            jsonPath("$.id") { value(book.id) }
            jsonPath("$.title") { value("Test Book") }
            jsonPath("$.author") { value("Author") }
        }
    }

    @Test
    fun `GET books by id - returns 404 when not found`() {
        mockMvc.get("/books/99999").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `PUT books - updates a book and returns 200`() {
        val book = savedBook()

        mockMvc.put("/books/${book.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"Updated Title","author":"New Author","isbn":"978-0-000000-00-0","price":19.99,"publisher":"Publisher"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(book.id) }
            jsonPath("$.title") { value("Updated Title") }
            jsonPath("$.author") { value("New Author") }
            jsonPath("$.price") { value(19.99) }
            jsonPath("$.publisher") { value("Publisher") }
        }
    }

    @Test
    fun `PUT books - returns 404 when not found`() {
        mockMvc.put("/books/99999") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"Title","author":"Author","isbn":"123","price":9.99}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `DELETE books - deletes a book and returns 204`() {
        val book = savedBook()

        mockMvc.delete("/books/${book.id}").andExpect {
            status { isNoContent() }
        }

        mockMvc.get("/books/${book.id}").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `DELETE books - returns 404 when not found`() {
        mockMvc.delete("/books/99999").andExpect {
            status { isNotFound() }
        }
    }
}
