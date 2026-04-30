package dev.leon.zimmermann.rpt.gatling.feeders

interface TestDataGenerator<T> {
    fun generate(): T
    fun toSessionMap(value: T): Map<String, Any>
}
