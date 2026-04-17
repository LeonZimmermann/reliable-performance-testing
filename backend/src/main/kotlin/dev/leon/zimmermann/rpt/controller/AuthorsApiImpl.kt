package dev.leon.zimmermann.rpt.controller

import dev.leon.zimmermann.rpt.generated.api.AuthorsApi
import dev.leon.zimmermann.rpt.generated.model.Author
import dev.leon.zimmermann.rpt.generated.model.AuthorPage
import dev.leon.zimmermann.rpt.generated.model.NewAuthor
import dev.leon.zimmermann.rpt.service.AuthorsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthorsApiImpl(private val authorsService: AuthorsService) : AuthorsApi {

    override fun createAuthor(newAuthor: NewAuthor): ResponseEntity<Author> =
        ResponseEntity.status(HttpStatus.CREATED).body(authorsService.createAuthor(newAuthor))

    override fun getAuthors(page: Int, size: Int): ResponseEntity<AuthorPage> =
        ResponseEntity.ok(authorsService.getAuthors(page, size))

    override fun getAuthorById(id: Long): ResponseEntity<Author> =
        ResponseEntity.ok(authorsService.getAuthorById(id))

    override fun updateAuthor(id: Long, newAuthor: NewAuthor): ResponseEntity<Author> =
        ResponseEntity.ok(authorsService.updateAuthor(id, newAuthor))

    override fun deleteAuthor(id: Long): ResponseEntity<Unit> {
        authorsService.deleteAuthor(id)
        return ResponseEntity.noContent().build()
    }
}
