package dev.leon.zimmermann.rpt

import dev.leon.zimmermann.rpt.entity.Author
import dev.leon.zimmermann.rpt.entity.Book
import dev.leon.zimmermann.rpt.repository.AuthorRepository
import dev.leon.zimmermann.rpt.repository.BookRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
@ConditionalOnProperty(name = ["app.seeding.enabled"], havingValue = "true", matchIfMissing = true)
class DataSeeder(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository,
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments) {
        if (authorRepository.count() > 0 || bookRepository.count() > 0) return

        val austen = authorRepository.save(
            Author(
                name = "Jane Austen",
                birthdate = LocalDate.of(1775, 12, 16),
                origin = "United Kingdom",
                biography = "Jane Austen was an English novelist known primarily for her six major novels, which interpret, critique, and comment upon the British landed gentry at the end of the 18th century. Her works of romantic fiction earned her a place as one of the most widely read writers in English literature.",
            )
        )

        val orwell = authorRepository.save(
            Author(
                name = "George Orwell",
                birthdate = LocalDate.of(1903, 6, 25),
                origin = "United Kingdom",
                biography = "Eric Arthur Blair, known by his pen name George Orwell, was an English novelist, essayist, journalist, and critic. His work is characterised by lucid prose, social criticism, opposition to totalitarianism, and support of democratic socialism.",
            )
        )

        val kafka = authorRepository.save(
            Author(
                name = "Franz Kafka",
                birthdate = LocalDate.of(1883, 7, 3),
                origin = "Czech Republic",
                biography = "Franz Kafka was a German-language novelist and short-story writer, widely regarded as one of the major figures of 20th-century literature. His work fuses elements of realism and the fantastic to address themes of alienation, existential anxiety, guilt, and absurdity.",
            )
        )

        val murakami = authorRepository.save(
            Author(
                name = "Haruki Murakami",
                birthdate = LocalDate.of(1949, 1, 12),
                origin = "Japan",
                biography = "Haruki Murakami is a Japanese novelist, short-story writer, and essayist. His work blends the mundane with the surreal and draws heavily on Western influences. His novels have been bestsellers in Japan and internationally, translated into more than 50 languages.",
            )
        )

        val marquez = authorRepository.save(
            Author(
                name = "Gabriel García Márquez",
                birthdate = LocalDate.of(1927, 3, 6),
                origin = "Colombia",
                biography = "Gabriel García Márquez was a Colombian novelist and Nobel Prize laureate best known for pioneering magical realism — a style that weaves fantastical elements seamlessly into realistic narratives. His masterwork, One Hundred Years of Solitude, is one of the most widely read Spanish-language novels ever written.",
            )
        )

        fun bookOf(title: String, isbn: String, price: Double, publisher: String, vararg linkedAuthors: Author): Book {
            val book = Book(
                title = title,
                author = linkedAuthors.first().name,
                isbn = isbn,
                price = price,
                publisher = publisher,
            )
            book.authors = linkedAuthors.toMutableSet()
            return book
        }

        bookRepository.saveAll(
            listOf(
                bookOf("Pride and Prejudice",          "978-0-14-143951-8", 8.99,  "Penguin Classics",        austen),
                bookOf("Sense and Sensibility",        "978-0-14-143952-5", 8.99,  "Penguin Classics",        austen),
                bookOf("Emma",                         "978-0-14-143955-6", 9.99,  "Penguin Classics",        austen),
                bookOf("Nineteen Eighty-Four",         "978-0-14-103614-4", 9.99,  "Penguin Books",           orwell),
                bookOf("Animal Farm",                  "978-0-14-118776-1", 7.99,  "Penguin Modern Classics", orwell),
                bookOf("Homage to Catalonia",          "978-0-14-118394-7", 10.99, "Penguin Modern Classics", orwell),
                bookOf("The Metamorphosis",            "978-0-14-181845-4", 7.99,  "Penguin Classics",        kafka),
                bookOf("The Trial",                    "978-0-14-118741-9", 9.99,  "Penguin Modern Classics", kafka),
                bookOf("Norwegian Wood",               "978-0-09-945647-5", 10.99, "Vintage",                 murakami),
                bookOf("Kafka on the Shore",           "978-0-09-945951-3", 11.99, "Vintage",                 murakami),
                bookOf("One Hundred Years of Solitude","978-0-14-303943-3", 11.99, "Penguin Modern Classics", marquez),
                bookOf("Love in the Time of Cholera", "978-0-14-303944-0", 10.99, "Penguin Modern Classics", marquez),
            )
        )
    }
}
