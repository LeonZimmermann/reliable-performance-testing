package org.example.entity

import jakarta.persistence.*

@Entity
@Table(name = "books")
data class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val author: String,

    @Column(nullable = false, unique = true)
    val isbn: String,

    @Column(nullable = false)
    val price: Double,

    @Column
    val publisher: String? = null
)
