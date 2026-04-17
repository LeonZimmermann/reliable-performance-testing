package dev.leon.zimmermann.rpt.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "authors")
data class Author(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column
    val birthdate: LocalDate? = null,

    @Column
    val origin: String? = null,

    @Column(columnDefinition = "TEXT")
    val biography: String? = null
)
