package dev.leon.zimmermann.rpt.repository

import dev.leon.zimmermann.rpt.entity.Author
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthorRepository : JpaRepository<Author, Long>
