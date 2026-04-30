package dev.leon.zimmermann.rpt.gatling.domain

import kotlin.random.Random

@JvmInline
value class ISBN(val value: String) {
    companion object {
        fun generate(): ISBN {
            val registrant = Random.nextInt(0, 10)
            val publication = Random.nextInt(100, 999)
            val title = Random.nextInt(10000, 99999)
            val checkDigit = Random.nextInt(0, 10)
            return ISBN("978-$registrant-$publication-$title-$checkDigit")
        }
    }
}
