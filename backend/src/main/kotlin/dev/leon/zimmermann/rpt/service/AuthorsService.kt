package dev.leon.zimmermann.rpt.service

import dev.leon.zimmermann.rpt.entity.Author as AuthorEntity
import dev.leon.zimmermann.rpt.generated.model.Author
import dev.leon.zimmermann.rpt.generated.model.AuthorPage
import dev.leon.zimmermann.rpt.generated.model.NewAuthor
import dev.leon.zimmermann.rpt.repository.AuthorRepository
import dev.leon.zimmermann.rpt.repository.BookRepository
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class AuthorsService(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository
) {

    fun createAuthor(newAuthor: NewAuthor): Author {
        return authorRepository.save(newAuthor.toEntity()).toModel()
    }

    @Transactional(readOnly = true)
    fun getAuthors(page: Int, size: Int): AuthorPage {
        val result = authorRepository.findAll(PageRequest.of(page, size))
        return AuthorPage(
            content = result.content.map { it.toModel() },
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            propertySize = result.size,
            number = result.number
        )
    }

    @Transactional(readOnly = true)
    fun getAuthorById(id: Long): Author {
        return authorRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found: $id") }
            .toModel()
    }

    fun updateAuthor(id: Long, newAuthor: NewAuthor): Author {
        val existing = authorRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found: $id") }
        return authorRepository.save(
            existing.copy(
                name = newAuthor.name,
                birthdate = newAuthor.birthdate,
                origin = newAuthor.origin,
                biography = newAuthor.biography
            )
        ).toModel()
    }

    fun deleteAuthor(id: Long) {
        if (!authorRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found: $id")
        }
        // Remove author from all books before deleting (clean up join table)
        bookRepository.findByAuthorId(id).forEach { book ->
            book.authors.removeIf { it.id == id }
            bookRepository.save(book)
        }
        authorRepository.deleteById(id)
    }

    private fun NewAuthor.toEntity() = AuthorEntity(
        name = name,
        birthdate = birthdate,
        origin = origin,
        biography = biography
    )

    private fun AuthorEntity.toModel() = Author(
        id = id!!,
        name = name,
        birthdate = birthdate,
        origin = origin,
        biography = biography
    )
}
