package org.example.controller

import org.example.generated.api.AuthorsApi
import org.example.generated.model.Author
import org.example.generated.model.AuthorPage
import org.example.generated.model.NewAuthor
import org.example.service.AuthorsService
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
