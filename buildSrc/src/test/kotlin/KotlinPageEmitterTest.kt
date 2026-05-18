import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for KotlinPageEmitter — the backend that walks IR nodes and emits Kotlin source.
 *
 * All tests construct PageObject IR directly (no YAML, no OpenAPI library) so they are fast,
 * isolated from the parser, and give precise coverage of every emitter code path.
 */
class KotlinPageEmitterTest {

    private fun emit(
        tag: String = "Items",
        packageName: String = "com.example",
        vararg ops: PageOperation,
    ): String = KotlinPageEmitter.emit(PageObject(packageName, tag, ops.toList()))

    /** Minimal GET with no params — the smallest possible operation. */
    private fun get(
        id: String = "getItems",
        path: String = "/items",
        pathParams: List<Param> = emptyList(),
        queryParams: List<Param> = emptyList(),
        response: ResponseSpec = ResponseSpec.None,
        successStatus: Int = 200,
        authenticated: Boolean = true,
        summary: String? = null,
    ) = PageOperation(
        id = id, summary = summary, verb = "get", path = path,
        pathParams = pathParams, queryParams = queryParams, bodyFields = emptyList(),
        successStatus = successStatus, response = response, authenticated = authenticated,
    )

    private fun post(
        id: String = "createItem",
        vararg fields: BodyField,
        successStatus: Int = 201,
        response: ResponseSpec = ResponseSpec.None,
        authenticated: Boolean = true,
    ) = PageOperation(
        id = id, summary = null, verb = "post", path = "/items",
        pathParams = emptyList(), queryParams = emptyList(), bodyFields = fields.toList(),
        successStatus = successStatus, response = response, authenticated = authenticated,
    )

    private fun delete(
        id: String = "deleteItem",
        path: String = "/items/{id}",
        pathParam: Param = Param("id", KotlinType.LONG, true),
        authenticated: Boolean = true,
    ) = PageOperation(
        id = id, summary = null, verb = "delete", path = path,
        pathParams = listOf(pathParam), queryParams = emptyList(), bodyFields = emptyList(),
        successStatus = 204, response = ResponseSpec.None, authenticated = authenticated,
    )

    @Test
    fun `generated file starts with package declaration`() {
        val code = emit(packageName = "my.pkg")
        assertTrue(code.startsWith("package my.pkg\n"))
    }

    @Test
    fun `generated file contains do-not-edit comment`() {
        val code = emit()
        assertTrue(code.contains("Generated from OpenAPI specification"))
    }

    @Test
    fun `generated file contains all required imports`() {
        val code = emit()
        assertTrue(code.contains("import io.gatling.javaapi.core.ChainBuilder"))
        assertTrue(code.contains("import io.gatling.javaapi.core.CoreDsl.*"))
        assertTrue(code.contains("import io.gatling.javaapi.http.HttpDsl.*"))
        assertFalse(code.contains("import dev.leon.zimmermann.rpt.gatling.utils.Authentication"))
    }

    @Test
    fun `object is named tag + Page with no superclass`() {
        val code = emit(tag = "Books")
        assertTrue(code.contains("object BooksPage {"))
        assertFalse(code.contains("BasePage"))
    }

    @Test
    fun `GET with no params generates no session variant`() {
        val code = emit(ops = arrayOf(get()))
        assertFalse(code.contains("FromSession"))
    }

    @Test
    fun `GET with optional query param generates session variant`() {
        val op = get(queryParams = listOf(Param("page", KotlinType.INT, false)))
        val code = emit(ops = arrayOf(op))
        assertTrue(code.contains("fun getItemsFromSession()"))
    }

    @Test
    fun `GET with path param generates session variant`() {
        val op = get(
            id = "getItemById", path = "/items/{id}",
            pathParams = listOf(Param("id", KotlinType.LONG, true)),
        )
        val code = emit(ops = arrayOf(op))
        assertTrue(code.contains("fun getItemByIdFromSession()"))
    }

    @Test
    fun `POST with body fields generates session variant`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("name", KotlinType.STRING, true)))))
        assertTrue(code.contains("fun createItemFromSession()"))
    }

    @Test
    fun `DELETE with path param generates session variant`() {
        val code = emit(ops = arrayOf(delete()))
        assertTrue(code.contains("fun deleteItemFromSession()"))
    }

    @Test
    fun `direct method substitutes path params as Kotlin template expressions`() {
        val op = get(
            id = "getItemById", path = "/items/{id}",
            pathParams = listOf(Param("id", KotlinType.LONG, true)),
        )
        val code = emit(ops = arrayOf(op))
        assertTrue(code.contains("/items/\${id}"))
    }

    @Test
    fun `session method substitutes path params as Gatling EL tokens`() {
        val op = get(
            id = "getItemById", path = "/items/{id}",
            pathParams = listOf(Param("id", KotlinType.LONG, true)),
        )
        val code = emit(ops = arrayOf(op))
        assertTrue(code.contains("/items/#{id}"))
    }

    @Test
    fun `multiple path params are all substituted in direct and session methods`() {
        val op = PageOperation(
            id = "getComment", summary = null, verb = "get",
            path = "/posts/{postId}/comments/{commentId}",
            pathParams = listOf(
                Param("postId", KotlinType.LONG, true),
                Param("commentId", KotlinType.LONG, true),
            ),
            queryParams = emptyList(), bodyFields = emptyList(),
            successStatus = 200, response = ResponseSpec.None,
        )
        val code = emit(ops = arrayOf(op))
        assertTrue(code.contains("/posts/\${postId}/comments/\${commentId}"))
        assertTrue(code.contains("/posts/#{postId}/comments/#{commentId}"))
    }

    @Test
    fun `path with no params produces plain path`() {
        val code = emit(ops = arrayOf(get()))
        assertTrue(code.contains(".get(\"/items\")"))
    }

    @Test
    fun `optional query param is guarded with null check in direct method`() {
        val op = get(queryParams = listOf(Param("page", KotlinType.INT, false)))
        val code = emit(ops = arrayOf(op))
        assertTrue(code.contains("if (page != null) req = req.queryParam(\"page\", page)"))
    }

    @Test
    fun `required query param has no null check in direct method`() {
        val op = get(queryParams = listOf(Param("filter", KotlinType.STRING, true)))
        val code = emit(ops = arrayOf(op))
        assertFalse(code.contains("if (filter != null)"))
        assertTrue(code.contains("req = req.queryParam(\"filter\", filter)"))
    }

    @Test
    fun `session method query params use Gatling EL syntax regardless of required flag`() {
        val op = get(queryParams = listOf(
            Param("page", KotlinType.INT, false),
            Param("size", KotlinType.INT, false),
        ))
        val code = emit(ops = arrayOf(op))
        assertTrue(code.contains("req.queryParam(\"page\", \"#{page}\")"))
        assertTrue(code.contains("req.queryParam(\"size\", \"#{size}\")"))
    }

    @Test
    fun `multiple optional query params each get their own null guard`() {
        val op = get(queryParams = listOf(
            Param("from", KotlinType.STRING, false),
            Param("to", KotlinType.STRING, false),
        ))
        val code = emit(ops = arrayOf(op))
        assertTrue(code.contains("if (from != null) req = req.queryParam(\"from\", from)"))
        assertTrue(code.contains("if (to != null) req = req.queryParam(\"to\", to)"))
    }

    @Test
    fun `POST with body fields emits Content-Type header`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("name", KotlinType.STRING, true)))))
        assertTrue(code.contains(".header(\"Content-Type\", \"application/json\")"))
    }

    @Test
    fun `required String body field is always added with JSON quotes around value`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("name", KotlinType.STRING, true)))))
        assertTrue(code.contains("\"\\\"name\\\":\\\""))
        assertFalse(code.contains("if (name != null)"))
    }

    @Test
    fun `optional String body field is null-guarded`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("description", KotlinType.STRING, false)))))
        assertTrue(code.contains("if (description != null) bodyParts.add"))
    }

    @Test
    fun `required Double body field is added without JSON quotes around value`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("price", KotlinType.DOUBLE, true)))))
        assertTrue(code.contains("\"\\\"price\\\":"))
        assertFalse(code.contains("\"\\\"price\\\":\\\""))
        assertFalse(code.contains("if (price != null)"))
    }

    @Test
    fun `required Int body field is added without JSON quotes`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("stock", KotlinType.INT, true)))))
        assertTrue(code.contains("\"\\\"stock\\\":"))
        assertFalse(code.contains("\"\\\"stock\\\":\\\""))
    }

    @Test
    fun `required Long body field is added without JSON quotes`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("authorId", KotlinType.LONG, true)))))
        assertFalse(code.contains("\"\\\"authorId\\\":\\\""))
    }

    @Test
    fun `required Boolean body field is added without JSON quotes`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("active", KotlinType.BOOLEAN, true)))))
        assertFalse(code.contains("\"\\\"active\\\":\\\""))
    }

    @Test
    fun `List body field is treated as quoted like String`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("tags", KotlinType.LIST, true)))))
        assertTrue(code.contains("\"\\\"tags\\\":\\\""))
    }

    @Test
    fun `optional numeric body field is null-guarded`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("price", KotlinType.DOUBLE, false)))))
        assertTrue(code.contains("if (price != null) bodyParts.add"))
    }

    @Test
    fun `mixed required and optional fields each behave correctly`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(
            BodyField("title",     KotlinType.STRING, true),
            BodyField("isbn",      KotlinType.STRING, true),
            BodyField("price",     KotlinType.DOUBLE, false),
            BodyField("publisher", KotlinType.STRING, false),
        ))))
        assertFalse(code.contains("if (title != null)"))
        assertFalse(code.contains("if (isbn != null)"))
        assertTrue(code.contains("if (price != null) bodyParts.add"))
        assertTrue(code.contains("if (publisher != null) bodyParts.add"))
    }

    @Test
    fun `session method only includes required body fields`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(
            BodyField("name",        KotlinType.STRING, true),
            BodyField("description", KotlinType.STRING, false),
        ))))
        assertTrue(code.contains("#{name}"))
        assertFalse(code.contains("#{description}"))
    }

    @Test
    fun `session method String field uses Gatling EL with JSON quotes`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("name", KotlinType.STRING, true)))))
        assertTrue(code.contains(""""name":"#{name}""""))
    }

    @Test
    fun `session method Double field uses Gatling EL without JSON quotes`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("price", KotlinType.DOUBLE, true)))))
        assertTrue(code.contains(""""price":#{price}"""))
        assertFalse(code.contains(""""price":"#{price}""""))
    }

    @Test
    fun `session method Boolean field uses Gatling EL without JSON quotes`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("active", KotlinType.BOOLEAN, true)))))
        assertTrue(code.contains(""""active":#{active}"""))
    }

    @Test
    fun `session method with multiple required fields joins them with commas`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(
            BodyField("title", KotlinType.STRING, true),
            BodyField("price", KotlinType.DOUBLE, true),
        ))))
        assertTrue(code.contains(""""title":"#{title}","price":#{price}"""))
    }

    @Test
    fun `session method wraps body in triple-quoted StringBody`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(BodyField("name", KotlinType.STRING, true)))))
        assertTrue(code.contains(".body(StringBody(\"\"\""))
    }

    @Test
    fun `None response adds no jsonPath checks`() {
        val code = emit(ops = arrayOf(get(response = ResponseSpec.None)))
        assertFalse(code.contains("jsonPath"))
    }

    @Test
    fun `ArrayBody response adds jsonPath saveAs list check`() {
        val code = emit(ops = arrayOf(get(response = ResponseSpec.ArrayBody("itemsList"))))
        assertTrue(code.contains("""jsonPath("$[*]").ofList().saveAs("itemsList")"""))
    }

    @Test
    fun `ArrayOfObjects saves whole list and one random pick per field`() {
        val code = emit(ops = arrayOf(get(response = ResponseSpec.ArrayOfObjects(
            listSessionKey = "booksList",
            itemFields = listOf("id", "title"),
        ))))
        assertTrue(code.contains("""jsonPath("$[*]").ofList().saveAs("booksList")"""))
        assertTrue(code.contains("""jsonPath("$[*].id").ofList().findRandom().saveAs("id")"""))
        assertTrue(code.contains("""jsonPath("$[*].title").ofList().findRandom().saveAs("title")"""))
    }

    @Test
    fun `ArrayOfObjects with no fields still saves whole list`() {
        val code = emit(ops = arrayOf(get(response = ResponseSpec.ArrayOfObjects(
            listSessionKey = "booksList",
            itemFields = emptyList(),
        ))))
        assertTrue(code.contains("""jsonPath("$[*]").ofList().saveAs("booksList")"""))
        assertFalse(code.contains("findRandom"))
    }

    @Test
    fun `ObjectFields response adds one jsonPath check per field`() {
        val code = emit(ops = arrayOf(get(
            response = ResponseSpec.ObjectFields(listOf("id", "name", "price"))
        )))
        assertTrue(code.contains("""jsonPath("$.id").saveAs("id")"""))
        assertTrue(code.contains("""jsonPath("$.name").saveAs("name")"""))
        assertTrue(code.contains("""jsonPath("$.price").saveAs("price")"""))
    }

    @Test
    fun `ObjectFields with single field emits exactly one jsonPath check`() {
        val code = emit(ops = arrayOf(get(response = ResponseSpec.ObjectFields(listOf("token")))))
        assertEquals(1, Regex("""jsonPath\(""").findAll(code).count())
        assertTrue(code.contains("""jsonPath("$.token").saveAs("token")"""))
    }

    @Test
    fun `response checks appear in both direct and session method`() {
        val op = get(
            queryParams = listOf(Param("page", KotlinType.INT, false)),
            response = ResponseSpec.ObjectFields(listOf("id")),
        )
        val code = emit(ops = arrayOf(op))
        assertEquals(2, Regex("""jsonPath\(""").findAll(code).count())
    }

    @Test
    fun `correct status code is emitted in status check — 200`() {
        val code = emit(ops = arrayOf(get(successStatus = 200)))
        assertTrue(code.contains(".check(status().`is`(200))"))
    }

    @Test
    fun `correct status code is emitted in status check — 201`() {
        val code = emit(ops = arrayOf(post(successStatus = 201)))
        assertTrue(code.contains(".check(status().`is`(201))"))
    }

    @Test
    fun `correct status code is emitted in status check — 204`() {
        val code = emit(ops = arrayOf(delete()))
        assertTrue(code.contains(".check(status().`is`(204))"))
    }

    @Test
    fun `generated page objects never reference Authentication`() {
        val code = emit(ops = arrayOf(get(authenticated = true)))
        assertFalse(code.contains("Authentication"))
    }

    @Test
    fun `unauthenticated endpoint omits ensureValidToken entirely`() {
        val code = emit(ops = arrayOf(get(authenticated = false)))
        assertFalse(code.contains("Authentication.ensureValidToken"))
    }

    @Test
    fun `unauthenticated endpoint still emits status check`() {
        val code = emit(ops = arrayOf(get(authenticated = false, successStatus = 200)))
        assertTrue(code.contains(".check(status().`is`(200))"))
    }

    @Test
    fun `unauthenticated endpoint with array response still emits jsonPath check`() {
        val code = emit(ops = arrayOf(get(
            authenticated = false,
            response = ResponseSpec.ArrayBody("itemsList"),
        )))
        assertFalse(code.contains("Authentication.ensureValidToken"))
        assertTrue(code.contains("""jsonPath("$[*]").ofList().saveAs("itemsList")"""))
    }

    @Test
    fun `operation with summary emits uppercase verb, path, and summary as comment`() {
        val op = get(id = "getItems", summary = "Fetch all items")
        val code = emit(ops = arrayOf(op))
        assertTrue(code.contains("// GET /items — Fetch all items"))
    }

    @Test
    fun `operation without summary emits no per-operation comment`() {
        val code = emit(ops = arrayOf(get(summary = null)))
        assertFalse(code.contains("// GET"))
    }

    @Test
    fun `POST operation summary uses POST verb in comment`() {
        val op = PageOperation(
            id = "createItem", summary = "Create item", verb = "post", path = "/items",
            pathParams = emptyList(), queryParams = emptyList(), bodyFields = emptyList(),
            successStatus = 201, response = ResponseSpec.None,
        )
        val code = emit(ops = arrayOf(op))
        assertTrue(code.contains("// POST /items — Create item"))
    }

    @Test
    fun `direct signature lists path params then body fields then query params`() {
        val op = PageOperation(
            id = "updateItem", summary = null, verb = "put", path = "/items/{id}",
            pathParams  = listOf(Param("id", KotlinType.LONG, true)),
            bodyFields  = listOf(BodyField("name", KotlinType.STRING, true)),
            queryParams = listOf(Param("dryRun", KotlinType.BOOLEAN, false)),
            successStatus = 200, response = ResponseSpec.None,
        )
        val code = emit(ops = arrayOf(op))
        val sigLine   = code.lines().first { it.contains("fun updateItem(") }
        val idPos     = sigLine.indexOf("id:")
        val namePos   = sigLine.indexOf("name:")
        val dryRunPos = sigLine.indexOf("dryRun:")
        assertTrue(idPos < namePos,     "path params come before body fields")
        assertTrue(namePos < dryRunPos, "body fields come before query params")
    }

    @Test
    fun `required param has no default value in signature`() {
        val op = get(pathParams = listOf(Param("id", KotlinType.LONG, true)))
        val code = emit(ops = arrayOf(op))
        val sigLine = code.lines().first { it.contains("fun getItems(") }
        assertTrue(sigLine.contains("id: Long)"))
        assertFalse(sigLine.contains("id: Long?"))
    }

    @Test
    fun `optional query param has nullable type with null default in signature`() {
        val op = get(queryParams = listOf(Param("page", KotlinType.INT, false)))
        val code = emit(ops = arrayOf(op))
        val sigLine = code.lines().first { it.contains("fun getItems(") }
        assertTrue(sigLine.contains("page: Int? = null"))
    }

    @Test
    fun `optional body field has nullable type with null default in signature`() {
        val code = emit(ops = arrayOf(post(fields = arrayOf(
            BodyField("title",     KotlinType.STRING, true),
            BodyField("publisher", KotlinType.STRING, false),
        ))))
        val sigLine = code.lines().first { it.contains("fun createItem(") }
        assertTrue(sigLine.contains("title: String"))
        assertTrue(sigLine.contains("publisher: String? = null"))
    }

    @Test
    fun `page with multiple operations emits all of them`() {
        val code = emit(ops = arrayOf(
            get(id = "listItems"),
            get(id = "getItemById", path = "/items/{id}", pathParams = listOf(Param("id", KotlinType.LONG, true))),
            post(id = "createItem", fields = arrayOf(BodyField("name", KotlinType.STRING, true))),
            delete(id = "deleteItem"),
        ))
        assertTrue(code.contains("fun listItems("))
        assertTrue(code.contains("fun getItemById("))
        assertTrue(code.contains("fun getItemByIdFromSession("))
        assertTrue(code.contains("fun createItem("))
        assertTrue(code.contains("fun createItemFromSession("))
        assertTrue(code.contains("fun deleteItem("))
        assertTrue(code.contains("fun deleteItemFromSession("))
    }

    @Test
    fun `GET returning array followed by POST with body both render correctly`() {
        val code = emit(ops = arrayOf(
            get(id = "listItems", response = ResponseSpec.ArrayBody("listItemsList")),
            post(id = "createItem",
                fields = arrayOf(BodyField("name", KotlinType.STRING, true)),
                response = ResponseSpec.ObjectFields(listOf("id", "name"))),
        ))
        assertTrue(code.contains("""jsonPath("$[*]").ofList().saveAs("listItemsList")"""))
        assertTrue(code.contains("""jsonPath("$.id").saveAs("id")"""))
        assertTrue(code.contains("""jsonPath("$.name").saveAs("name")"""))
    }

    @Test
    fun `DELETE with 204 emits no jsonPath checks`() {
        val code = emit(ops = arrayOf(delete()))
        assertFalse(code.contains("jsonPath"))
        assertTrue(code.contains(".check(status().`is`(204))"))
    }

    @Test
    fun `unauthenticated DELETE still checks status code`() {
        val code = emit(ops = arrayOf(delete(authenticated = false)))
        assertFalse(code.contains("Authentication.ensureValidToken"))
        assertTrue(code.contains(".check(status().`is`(204))"))
    }

    @Test
    fun `PUT with path param and required body field emits both`() {
        val op = PageOperation(
            id = "updateItem", summary = null, verb = "put", path = "/items/{id}",
            pathParams  = listOf(Param("id", KotlinType.LONG, true)),
            queryParams = emptyList(),
            bodyFields  = listOf(
                BodyField("name",  KotlinType.STRING, true),
                BodyField("price", KotlinType.DOUBLE, false),
            ),
            successStatus = 200, response = ResponseSpec.ObjectFields(listOf("id", "name")),
        )
        val code = emit(ops = arrayOf(op))
        assertTrue(code.contains("/items/\${id}"))
        assertFalse(code.contains("if (name != null)"))
        assertTrue(code.contains("if (price != null) bodyParts.add"))
        assertTrue(code.contains("""jsonPath("$.id")"""))
    }
}
