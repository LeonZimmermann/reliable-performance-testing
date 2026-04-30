package dev.leon.zimmermann.rpt.gatling.feeders

object DomainFeeder {
    fun <T> of(generator: TestDataGenerator<T>): Iterator<Map<String, Any>> =
        generateSequence { generator.toSessionMap(generator.generate()) }.iterator()
}
