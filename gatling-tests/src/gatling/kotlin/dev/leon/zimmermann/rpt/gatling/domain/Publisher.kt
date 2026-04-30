package dev.leon.zimmermann.rpt.gatling.domain

@JvmInline
value class Publisher(val value: String) {
    companion object {
        private val PUBLISHERS = listOf(
            "Horizon Books", "Page Turner Press", "Novel House", "Ink & Story",
            "The Written Word", "Lighthouse Publishing", "Ember Press",
        )

        fun generate(): Publisher = Publisher(PUBLISHERS.random())
    }
}
