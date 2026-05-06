package dev.leon.zimmermann.rpt.gatling.feeders

import dev.leon.zimmermann.rpt.gatling.domain.PersonName

object PersonNameFeeder : TestDataGenerator<PersonName> {
    private val FIRST_NAMES = listOf(
        "Alice", "Bob", "Clara", "David", "Elena", "Frank", "Grace",
        "Henry", "Iris", "James", "Karen", "Liam", "Maya", "Noah",
    )
    private val LAST_NAMES = listOf(
        "Ashford", "Blake", "Chen", "Drake", "Evans", "Fischer", "Grant",
        "Hayes", "Irons", "Jung", "Klein", "Lowe", "Marsh", "Nash",
    )

    override fun generate(): PersonName = PersonName(
        firstName = FIRST_NAMES.random(),
        lastName = LAST_NAMES.random()
    )

    override fun toSessionMap(value: PersonName): Map<String, Any> = buildMap {
        put("firstName", value.firstName)
        put("lastName", value.lastName)
    }
}
