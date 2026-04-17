package dev.leon.zimmermann.rpt.repository

import dev.leon.zimmermann.rpt.entity.Book
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BookRepository : JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId")
    fun findByAuthorId(@Param("authorId") authorId: Long): List<Book>
}
