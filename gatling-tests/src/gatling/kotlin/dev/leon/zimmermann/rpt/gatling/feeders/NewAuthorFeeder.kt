package dev.leon.zimmermann.rpt.gatling.feeders

import dev.leon.zimmermann.rpt.gatling.domain.NewAuthor
import kotlin.random.Random

object NewAuthorFeeder : TestDataGenerator<NewAuthor> {

    private val ORIGINS = listOf(
        "Germany", "France", "United States", "United Kingdom", "Italy",
        "Spain", "Japan", "Brazil", "Canada", "Australia",
    )
    private val BIOGRAPHY_SUBJECTS = listOf(
        "debut novel", "acclaimed short stories", "poetry collection",
        "historical fiction", "award-winning essays", "travel writing",
    )
    private val BIOGRAPHY_VERBS = listOf(
        "captivated readers with", "gained recognition for", "is best known for",
        "rose to prominence through", "established a reputation with",
    )

    override fun generate(): NewAuthor = NewAuthor(
        name = PersonNameFeeder.generate().let { "${it.firstName} ${it.lastName}" },
        birthdate = if (Random.nextBoolean()) generateBirthdate() else null,
        origin = if (Random.nextBoolean()) ORIGINS.random() else null,
        biography = if (Random.nextBoolean()) generateBiography() else null,
    )

    override fun toSessionMap(value: NewAuthor): Map<String, Any> = buildMap {
        put("name", value.name)
        value.birthdate?.let { put("birthdate", it) }
        value.origin?.let { put("origin", it) }
        value.biography?.let { put("biography", it) }
    }

    private fun generateBirthdate(): String {
        val year = Random.nextInt(1850, 2000)
        val month = Random.nextInt(1, 13)
        val day = Random.nextInt(1, 29)
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    private fun generateBiography(): String {
        val name = PersonNameFeeder.generate()
        return "${name.firstName} ${BIOGRAPHY_VERBS.random()} their ${BIOGRAPHY_SUBJECTS.random()}."
    }
}
