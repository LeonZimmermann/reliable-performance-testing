package dev.leon.zimmermann.rpt.gatling.feeders

import dev.leon.zimmermann.rpt.gatling.domain.ISBN
import kotlin.random.Random

object ISBNFeeder : TestDataGenerator<ISBN> {

    override fun generate(): ISBN {
        val registrant = Random.nextInt(0, 10)
        val publication = Random.nextInt(100, 1000)
        val title = Random.nextInt(10000, 100000)
        val base = "978$registrant$publication$title"
        val checkDigit = calculateCheckDigit(base)
        return ISBN("978-$registrant-$publication-$title-$checkDigit")
    }

    override fun toSessionMap(value: ISBN): Map<String, Any> = mapOf("isbn" to value.value)

    private fun calculateCheckDigit(digits: String): Int {
        require(digits.length == 12) { "ISBN-13 base must be 12 digits" }
        val sum = digits.mapIndexed { i, c -> c.digitToInt() * if (i % 2 == 0) 1 else 3 }.sum()
        val remainder = sum % 10
        return if (remainder == 0) 0 else 10 - remainder
    }
}
