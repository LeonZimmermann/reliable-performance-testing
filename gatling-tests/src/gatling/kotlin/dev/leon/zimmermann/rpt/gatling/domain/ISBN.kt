package dev.leon.zimmermann.rpt.gatling.domain

import kotlin.random.Random

@JvmInline
value class ISBN(val value: String) {
    companion object {

        fun generate(): ISBN {
            val prefix = "978"
            val registrant = Random.nextInt(0, 10)
            val publication = Random.nextInt(100, 1000)
            val title = Random.nextInt(10000, 100000)
            val checkDigit = calculateCheckDigit("$prefix$registrant$publication$title")
            return ISBN("978-$registrant-$publication-$title-$checkDigit")
        }

        private fun calculateCheckDigit(digits: String): Int {
            require(digits.length == 12) { "ISBN-13 Basis muss 12 Stellen haben" }

            val sum = digits.mapIndexed { index, c ->
                val digit = c.digitToInt()
                if (index % 2 == 0) digit else digit * 3
            }.sum()

            val remainder = sum % 10
            return if (remainder == 0) 0 else 10 - remainder
        }
    }
}
