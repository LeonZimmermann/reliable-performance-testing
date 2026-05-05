package dev.leon.zimmermann.rpt.gatling.domain

data class Name(val firstName: String, val lastName: String) {
    val fullName: String get() = "$firstName $lastName"

    companion object {
        private val FIRST_NAMES = listOf(
            "Alice", "Bob", "Clara", "David", "Elena", "Frank", "Grace",
            "Henry", "Iris", "James", "Karen", "Liam", "Maya", "Noah",
        )
        private val LAST_NAMES = listOf(
            "Ashford", "Blake", "Chen", "Drake", "Evans", "Fischer", "Grant",
            "Hayes", "Irons", "Jung", "Klein", "Lowe", "Marsh", "Nash",
        )

        fun generate(): Name = Name(FIRST_NAMES.random(), LAST_NAMES.random())
    }
}
