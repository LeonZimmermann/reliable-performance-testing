package org.example

import org.example.entity.Author
import org.example.repository.AuthorRepository
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
class AuthorsIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var authorRepository: AuthorRepository
    @Autowired lateinit var bookRepository: BookRepository

    @BeforeEach
    fun setUp() {
        bookRepository.deleteAll()
        authorRepository.deleteAll()
    }

    private fun savedAuthor(name: String = "Leo Tolstoy") =
        authorRepository.save(Author(name = name, origin = "Russia", biography = "Famous Russian novelist"))

    @Test
    fun `POST authors - creates an author with all fields and returns 201`() {
        mockMvc.post("/authors") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Leo Tolstoy","birthdate":"1828-09-09","origin":"Russia","biography":"Famous Russian novelist"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { exists() }
            jsonPath("$.name") { value("Leo Tolstoy") }
            jsonPath("$.birthdate") { value("1828-09-09") }
            jsonPath("$.origin") { value("Russia") }
            jsonPath("$.biography") { value("Famous Russian novelist") }
        }
    }

    @Test
    fun `POST authors - creates an author with only required fields and returns 201`() {
        mockMvc.post("/authors") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Unknown Author"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { exists() }
            jsonPath("$.name") { value("Unknown Author") }
        }
    }

    @Test
    fun `GET authors - returns paginated list`() {
        savedAuthor("Author One")
        savedAuthor("Author Two")

        mockMvc.get("/authors?page=0&size=10").andExpect {
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
    fun `GET authors - respects page and size parameters`() {
        repeat(5) { i -> savedAuthor("Author $i") }

        mockMvc.get("/authors?page=0&size=3").andExpect {
            status { isOk() }
            jsonPath("$.content.length()") { value(3) }
            jsonPath("$.totalElements") { value(5) }
            jsonPath("$.totalPages") { value(2) }
        }

        mockMvc.get("/authors?page=1&size=3").andExpect {
            status { isOk() }
            jsonPath("$.content.length()") { value(2) }
        }
    }

    @Test
    fun `GET authors by id - returns author when found`() {
        val author = savedAuthor()

        mockMvc.get("/authors/${author.id}").andExpect {
            status { isOk() }
            jsonPath("$.id") { value(author.id) }
            jsonPath("$.name") { value("Leo Tolstoy") }
            jsonPath("$.origin") { value("Russia") }
        }
    }

    @Test
    fun `GET authors by id - returns 404 when not found`() {
        mockMvc.get("/authors/99999").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `PUT authors - updates an author and returns 200`() {
        val author = savedAuthor()

        mockMvc.put("/authors/${author.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Updated Name","birthdate":"1900-01-01","origin":"France","biography":"Updated bio"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(author.id) }
            jsonPath("$.name") { value("Updated Name") }
            jsonPath("$.birthdate") { value("1900-01-01") }
            jsonPath("$.origin") { value("France") }
            jsonPath("$.biography") { value("Updated bio") }
        }
    }

    @Test
    fun `PUT authors - returns 404 when not found`() {
        mockMvc.put("/authors/99999") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Name"}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `DELETE authors - deletes an author and returns 204`() {
        val author = savedAuthor()

        mockMvc.delete("/authors/${author.id}").andExpect {
            status { isNoContent() }
        }

        mockMvc.get("/authors/${author.id}").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `DELETE authors - returns 404 when not found`() {
        mockMvc.delete("/authors/99999").andExpect {
            status { isNotFound() }
        }
    }
}
