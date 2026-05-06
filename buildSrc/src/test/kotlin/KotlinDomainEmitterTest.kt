import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KotlinDomainEmitterTest {

    private fun emit(
        name: String = "Book",
        packageName: String = "com.example.domain",
        vararg fields: DomainField,
    ): String = KotlinDomainEmitter.emit(DomainObject(packageName, name, fields.toList()))

    @Test
    fun `generated file starts with package declaration`() {
        val code = emit(packageName = "my.pkg")
        assertTrue(code.startsWith("package my.pkg\n"))
    }

    @Test
    fun `generated file contains do-not-edit comment`() {
        assertTrue(emit().contains("Generated from OpenAPI specification"))
    }

    @Test
    fun `schema with no fields emits plain class`() {
        val code = emit()
        assertTrue(code.contains("class Book"))
        assertFalse(code.contains("data class"))
    }

    @Test
    fun `schema with fields emits data class`() {
        val code = emit(fields = arrayOf(DomainField("id", "Long", true)))
        assertTrue(code.contains("data class Book("))
    }

    @Test
    fun `required field has no default value`() {
        val code = emit(fields = arrayOf(DomainField("title", "String", true)))
        assertTrue(code.contains("val title: String"))
        assertFalse(code.contains("val title: String?"))
    }

    @Test
    fun `optional field has nullable type with null default`() {
        val code = emit(fields = arrayOf(DomainField("publisher", "String", false)))
        assertTrue(code.contains("val publisher: String? = null"))
    }

    @Test
    fun `required fields come before optional fields`() {
        val code = emit(fields = arrayOf(
            DomainField("publisher", "String", false),
            DomainField("title", "String", true),
        ))
        val titlePos = code.indexOf("val title:")
        val publisherPos = code.indexOf("val publisher:")
        assertTrue(titlePos < publisherPos, "required field 'title' must precede optional 'publisher'")
    }

    @Test
    fun `list field with object item type is emitted correctly`() {
        val code = emit(fields = arrayOf(DomainField("authors", "List<AuthorSummary>", true)))
        assertTrue(code.contains("val authors: List<AuthorSummary>"))
    }

    @Test
    fun `list field with primitive item type is emitted correctly`() {
        val code = emit(fields = arrayOf(DomainField("authorIds", "List<Long>", false)))
        assertTrue(code.contains("val authorIds: List<Long>? = null"))
    }

    @Test
    fun `multiple required fields all appear without defaults`() {
        val code = emit(fields = arrayOf(
            DomainField("title", "String", true),
            DomainField("isbn", "String", true),
            DomainField("price", "Double", true),
        ))
        assertTrue(code.contains("val title: String"))
        assertTrue(code.contains("val isbn: String"))
        assertTrue(code.contains("val price: Double"))
        assertFalse(code.contains("= null"))
    }

    @Test
    fun `all but last field have trailing comma`() {
        val code = emit(fields = arrayOf(
            DomainField("title", "String", true),
            DomainField("price", "Double", true),
        ))
        val lines = code.lines()
        val titleLine = lines.first { it.contains("val title:") }
        val priceLine = lines.first { it.contains("val price:") }
        assertTrue(titleLine.trimEnd().endsWith(","))
        assertFalse(priceLine.trimEnd().endsWith(","))
    }

    @Test
    fun `Long field type is preserved exactly`() {
        val code = emit(fields = arrayOf(DomainField("id", "Long", true)))
        assertTrue(code.contains("val id: Long"))
    }

    @Test
    fun `Double field type is preserved exactly`() {
        val code = emit(fields = arrayOf(DomainField("price", "Double", true)))
        assertTrue(code.contains("val price: Double"))
    }

    @Test
    fun `class name matches domain object name`() {
        val code = emit(name = "NewAuthor")
        assertTrue(code.contains("data class NewAuthor") || code.contains("class NewAuthor"))
    }

    @Test
    fun `generated file contains no gatling imports`() {
        val code = emit(fields = arrayOf(DomainField("id", "Long", true)))
        assertFalse(code.contains("import io.gatling"))
    }
}
