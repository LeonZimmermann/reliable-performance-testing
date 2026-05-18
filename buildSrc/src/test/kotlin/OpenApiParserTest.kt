import io.swagger.v3.parser.OpenAPIV3Parser  // used inside the private parse() helper only
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for OpenApiParser — the frontend that lowers an OpenAPI model to PageObject IR.
 *
 * Every test is self-contained: the YAML spec is declared inline inside the test method itself
 * via a triple-quoted string literal. No external files are read; no shared state is used.
 * readContents() parses from a String, not from the filesystem.
 */
class OpenApiParserTest {

    /** Parses an inline YAML string (not a file path) and runs it through OpenApiParser. */
    private fun parse(yaml: String): List<PageObject> {
        val api = OpenAPIV3Parser().readContents(yaml.trimIndent(), null, null).openAPI
            ?: error("Swagger parser returned null — check indentation in the inline YAML")
        return OpenApiParser.parse(api, "com.example")
    }

    private fun singleOp(yaml: String): PageOperation = parse(yaml).single().operations.single()

    @Test
    fun `GET with no params and array response maps to ArrayBody`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                get:
                  operationId: listItems
                  tags: [Items]
                  responses:
                    '200':
                      content:
                        application/json:
                          schema:
                            type: array
                            items:
                              type: string
        """)

        assertEquals("listItems", op.id)
        assertEquals("get", op.verb)
        assertEquals("/items", op.path)
        assertTrue(op.pathParams.isEmpty())
        assertTrue(op.queryParams.isEmpty())
        assertTrue(op.bodyFields.isEmpty())
        assertEquals(200, op.successStatus)
        assertEquals(ResponseSpec.ArrayBody("listItemsList"), op.response)
        assertFalse(op.hasSessionVariant)
    }

    @Test
    fun `GET with optional query params builds correct Params`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                get:
                  operationId: getItems
                  tags: [Items]
                  parameters:
                    - in: query
                      name: page
                      schema:
                        type: integer
                      required: false
                    - in: query
                      name: size
                      schema:
                        type: integer
                      required: false
                  responses:
                    '200':
                      content:
                        application/json:
                          schema:
                            type: object
                            properties:
                              content:
                                type: array
                                items:
                                  type: string
        """)

        assertEquals(2, op.queryParams.size)

        val page = op.queryParams[0]
        assertEquals("page", page.name)
        assertEquals(KotlinType.INT, page.type)
        assertFalse(page.required)

        val size = op.queryParams[1]
        assertEquals("size", size.name)
        assertEquals(KotlinType.INT, size.type)
        assertFalse(size.required)

        assertTrue(op.hasSessionVariant)
    }

    @Test
    fun `GET with required query param marks it as required`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                get:
                  operationId: searchItems
                  tags: [Items]
                  parameters:
                    - in: query
                      name: q
                      schema:
                        type: string
                      required: true
                  responses:
                    '200':
                      description: OK
        """)

        val q = op.queryParams.single()
        assertEquals("q", q.name)
        assertTrue(q.required)
        assertEquals(KotlinType.STRING, q.type)
    }

    @Test
    fun `GET with path param of type Long and object response`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items/{id}:
                get:
                  operationId: getItemById
                  tags: [Items]
                  parameters:
                    - in: path
                      name: id
                      schema:
                        type: integer
                        format: int64
                      required: true
                  responses:
                    '200':
                      content:
                        application/json:
                          schema:
                            type: object
                            properties:
                              name:
                                type: string
                              count:
                                type: integer
        """)

        assertEquals(1, op.pathParams.size)
        val id = op.pathParams[0]
        assertEquals("id", id.name)
        assertEquals(KotlinType.LONG, id.type)
        assertTrue(id.required)

        assertEquals(ResponseSpec.ObjectFields(listOf("name", "count")), op.response)
        assertTrue(op.hasSessionVariant)
    }

    @Test
    fun `GET with both path param and query param`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /users/{userId}/items:
                get:
                  operationId: getUserItems
                  tags: [Items]
                  parameters:
                    - in: path
                      name: userId
                      schema:
                        type: integer
                        format: int64
                      required: true
                    - in: query
                      name: active
                      schema:
                        type: boolean
                      required: false
                  responses:
                    '200':
                      description: OK
        """)

        assertEquals(1, op.pathParams.size)
        assertEquals("userId", op.pathParams[0].name)
        assertEquals(KotlinType.LONG, op.pathParams[0].type)

        assertEquals(1, op.queryParams.size)
        assertEquals("active", op.queryParams[0].name)
        assertEquals(KotlinType.BOOLEAN, op.queryParams[0].type)
        assertFalse(op.queryParams[0].required)
    }

    @Test
    fun `POST with required and optional body fields of all types`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                post:
                  operationId: createItem
                  tags: [Items]
                  requestBody:
                    required: true
                    content:
                      application/json:
                        schema:
                          type: object
                          properties:
                            name:
                              type: string
                            price:
                              type: number
                            stock:
                              type: integer
                            weight:
                              type: integer
                              format: int64
                            active:
                              type: boolean
                            tags:
                              type: array
                              items:
                                type: string
                            description:
                              type: string
                          required: [name, price]
                  responses:
                    '201':
                      content:
                        application/json:
                          schema:
                            type: object
                            properties:
                              id:
                                type: integer
                                format: int64
                              name:
                                type: string
        """)

        assertEquals("post", op.verb)
        assertEquals(201, op.successStatus)
        assertEquals(7, op.bodyFields.size)

        val byName = op.bodyFields.associateBy { it.name }

        assertEquals(KotlinType.STRING,  byName["name"]!!.type);   assertTrue(byName["name"]!!.required)
        assertEquals(KotlinType.DOUBLE,  byName["price"]!!.type);  assertTrue(byName["price"]!!.required)
        assertEquals(KotlinType.INT,     byName["stock"]!!.type);  assertFalse(byName["stock"]!!.required)
        assertEquals(KotlinType.LONG,    byName["weight"]!!.type); assertFalse(byName["weight"]!!.required)
        assertEquals(KotlinType.BOOLEAN, byName["active"]!!.type); assertFalse(byName["active"]!!.required)
        assertEquals(KotlinType.LIST,    byName["tags"]!!.type);   assertFalse(byName["tags"]!!.required)
        assertEquals(KotlinType.STRING,  byName["description"]!!.type); assertFalse(byName["description"]!!.required)

        assertEquals(ResponseSpec.ObjectFields(listOf("id", "name")), op.response)
        assertTrue(op.hasSessionVariant)
    }

    @Test
    fun `POST with all-required body fields`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                post:
                  operationId: createItem
                  tags: [Items]
                  requestBody:
                    required: true
                    content:
                      application/json:
                        schema:
                          type: object
                          properties:
                            title:
                              type: string
                            isbn:
                              type: string
                          required: [title, isbn]
                  responses:
                    '201':
                      description: Created
        """)

        assertTrue(op.bodyFields.all { it.required })
    }

    @Test
    fun `POST with no body returns correct 201 status and None response when no content`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items/trigger:
                post:
                  operationId: triggerAction
                  tags: [Items]
                  responses:
                    '201':
                      description: Accepted
        """)

        assertEquals("post", op.verb)
        assertEquals(201, op.successStatus)
        assertEquals(ResponseSpec.None, op.response)
        assertTrue(op.bodyFields.isEmpty())
        assertFalse(op.hasSessionVariant)
    }

    @Test
    fun `PUT with path param and body fields`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items/{id}:
                put:
                  operationId: updateItem
                  tags: [Items]
                  parameters:
                    - in: path
                      name: id
                      schema:
                        type: integer
                        format: int64
                      required: true
                  requestBody:
                    required: true
                    content:
                      application/json:
                        schema:
                          type: object
                          properties:
                            name:
                              type: string
                            price:
                              type: number
                          required: [name]
                  responses:
                    '200':
                      content:
                        application/json:
                          schema:
                            type: object
                            properties:
                              id:
                                type: integer
                                format: int64
                              name:
                                type: string
        """)

        assertEquals("put", op.verb)
        assertEquals(1, op.pathParams.size)
        assertEquals("id", op.pathParams[0].name)
        assertEquals(KotlinType.LONG, op.pathParams[0].type)

        val byName = op.bodyFields.associateBy { it.name }
        assertEquals(2, byName.size)
        assertTrue(byName["name"]!!.required)
        assertFalse(byName["price"]!!.required)

        assertEquals(ResponseSpec.ObjectFields(listOf("id", "name")), op.response)
    }

    @Test
    fun `DELETE with path param and 204 maps to None response`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items/{id}:
                delete:
                  operationId: deleteItem
                  tags: [Items]
                  parameters:
                    - in: path
                      name: id
                      schema:
                        type: integer
                        format: int64
                      required: true
                  responses:
                    '204':
                      description: No Content
        """)

        assertEquals("delete", op.verb)
        assertEquals(204, op.successStatus)
        assertEquals(ResponseSpec.None, op.response)
        assertEquals(1, op.pathParams.size)
        assertTrue(op.bodyFields.isEmpty())
        assertTrue(op.queryParams.isEmpty())
        assertTrue(op.hasSessionVariant)
    }

    @Test
    fun `DELETE with no path param and 204 has no session variant`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /cache:
                delete:
                  operationId: clearCache
                  tags: [Admin]
                  responses:
                    '204':
                      description: No Content
        """)

        assertEquals(204, op.successStatus)
        assertEquals(ResponseSpec.None, op.response)
        assertFalse(op.hasSessionVariant)
    }

    @Test
    fun `array of objects via inline schema maps to ArrayOfObjects`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                get:
                  operationId: listItems
                  tags: [Items]
                  responses:
                    '200':
                      content:
                        application/json:
                          schema:
                            type: array
                            items:
                              type: object
                              properties:
                                id:
                                  type: integer
                                  format: int64
                                name:
                                  type: string
        """)

        assertEquals(
            ResponseSpec.ArrayOfObjects("listItemsList", listOf("id", "name")),
            op.response,
        )
    }

    @Test
    fun `array of objects via component ref maps to ArrayOfObjects with referenced fields`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /books:
                get:
                  operationId: getAllBooks
                  tags: [Books]
                  responses:
                    '200':
                      content:
                        application/json:
                          schema:
                            type: array
                            items:
                              ${'$'}ref: '#/components/schemas/Book'
            components:
              schemas:
                Book:
                  type: object
                  properties:
                    id:
                      type: integer
                      format: int64
                    title:
                      type: string
        """)

        assertEquals(
            ResponseSpec.ArrayOfObjects("getAllBooksList", listOf("id", "title")),
            op.response,
        )
    }

    @Test
    fun `array response session key is operationId + List suffix`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /resources:
                get:
                  operationId: getAllResources
                  tags: [Resources]
                  responses:
                    '200':
                      content:
                        application/json:
                          schema:
                            type: array
                            items:
                              type: string
        """)

        assertEquals(ResponseSpec.ArrayBody("getAllResourcesList"), op.response)
    }

    @Test
    fun `object response field names come from schema properties`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /profile:
                get:
                  operationId: getProfile
                  tags: [User]
                  responses:
                    '200':
                      content:
                        application/json:
                          schema:
                            type: object
                            properties:
                              username:
                                type: string
                              email:
                                type: string
                              age:
                                type: integer
        """)

        assertEquals(ResponseSpec.ObjectFields(listOf("username", "email", "age")), op.response)
    }

    @Test
    fun `response with no content body maps to None`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items/{id}:
                delete:
                  operationId: deleteItem
                  tags: [Items]
                  parameters:
                    - in: path
                      name: id
                      schema:
                        type: integer
                      required: true
                  responses:
                    '204':
                      description: No Content
        """)

        assertEquals(ResponseSpec.None, op.response)
    }

    @Test
    fun `allOf inherits properties and required from all parents`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                post:
                  operationId: createItem
                  tags: [Items]
                  requestBody:
                    required: true
                    content:
                      application/json:
                        schema:
                          ${'$'}ref: '#/components/schemas/NewItem'
                  responses:
                    '201':
                      description: Created
            components:
              schemas:
                BaseItem:
                  type: object
                  properties:
                    name:
                      type: string
                    price:
                      type: number
                  required: [name]
                NewItem:
                  allOf:
                    - ${'$'}ref: '#/components/schemas/BaseItem'
                    - type: object
                      properties:
                        description:
                          type: string
                      required: [price]
        """)

        val byName = op.bodyFields.associateBy { it.name }
        assertEquals(3, byName.size)
        assertTrue(byName["name"]!!.required)
        assertTrue(byName["price"]!!.required)
        assertFalse(byName["description"]!!.required)
    }

    @Test
    fun `allOf three-level chain collects all properties and required`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                post:
                  operationId: createItem
                  tags: [Items]
                  requestBody:
                    required: true
                    content:
                      application/json:
                        schema:
                          ${'$'}ref: '#/components/schemas/FullItem'
                  responses:
                    '201':
                      description: Created
            components:
              schemas:
                RootItem:
                  type: object
                  properties:
                    id:
                      type: integer
                      format: int64
                  required: [id]
                BaseItem:
                  allOf:
                    - ${'$'}ref: '#/components/schemas/RootItem'
                    - type: object
                      properties:
                        name:
                          type: string
                      required: [name]
                FullItem:
                  allOf:
                    - ${'$'}ref: '#/components/schemas/BaseItem'
                    - type: object
                      properties:
                        description:
                          type: string
        """)

        val byName = op.bodyFields.associateBy { it.name }
        assertEquals(3, byName.size)
        assertTrue(byName["id"]!!.required)
        assertTrue(byName["name"]!!.required)
        assertFalse(byName["description"]!!.required)
    }

    @Test
    fun `schema ref in response is correctly resolved to ObjectFields`() {
        val pages = parse("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /widgets:
                post:
                  operationId: createWidget
                  tags: [Widgets]
                  requestBody:
                    required: true
                    content:
                      application/json:
                        schema:
                          type: object
                          properties:
                            label:
                              type: string
                          required: [label]
                  responses:
                    '201':
                      content:
                        application/json:
                          schema:
                            ${'$'}ref: '#/components/schemas/Widget'
            components:
              schemas:
                Widget:
                  type: object
                  properties:
                    id:
                      type: integer
                      format: int64
                    label:
                      type: string
                    color:
                      type: string
        """)

        val op = pages.single().operations.single()
        assertEquals(ResponseSpec.ObjectFields(listOf("id", "label", "color")), op.response)
    }

    @Test
    fun `type mapping covers all OAS types`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                post:
                  operationId: createItem
                  tags: [Items]
                  requestBody:
                    required: true
                    content:
                      application/json:
                        schema:
                          type: object
                          properties:
                            strField:
                              type: string
                            intField:
                              type: integer
                            longField:
                              type: integer
                              format: int64
                            doubleField:
                              type: number
                            boolField:
                              type: boolean
                            listField:
                              type: array
                              items:
                                type: string
                  responses:
                    '201':
                      description: Created
        """)

        val byName = op.bodyFields.associateBy { it.name }
        assertEquals(KotlinType.STRING,  byName["strField"]!!.type)
        assertEquals(KotlinType.INT,     byName["intField"]!!.type)
        assertEquals(KotlinType.LONG,    byName["longField"]!!.type)
        assertEquals(KotlinType.DOUBLE,  byName["doubleField"]!!.type)
        assertEquals(KotlinType.BOOLEAN, byName["boolField"]!!.type)
        assertEquals(KotlinType.LIST,    byName["listField"]!!.type)
    }

    @Test
    fun `operations from different tags produce separate page objects`() {
        val pages = parse("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /widgets:
                get:
                  operationId: getWidgets
                  tags: [Widgets]
                  responses:
                    '200':
                      description: OK
              /gadgets:
                get:
                  operationId: getGadgets
                  tags: [Gadgets]
                  responses:
                    '200':
                      description: OK
        """)

        assertEquals(2, pages.size)
        assertEquals(setOf("Widgets", "Gadgets"), pages.map { it.tag }.toSet())
        pages.forEach { assertEquals(1, it.operations.size) }
    }

    @Test
    fun `multiple operations under one tag are grouped in one page object`() {
        val pages = parse("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                get:
                  operationId: listItems
                  tags: [Items]
                  responses:
                    '200':
                      description: OK
                post:
                  operationId: createItem
                  tags: [Items]
                  responses:
                    '201':
                      description: Created
              /items/{id}:
                get:
                  operationId: getItem
                  tags: [Items]
                  parameters:
                    - in: path
                      name: id
                      schema:
                        type: integer
                      required: true
                  responses:
                    '200':
                      description: OK
                delete:
                  operationId: deleteItem
                  tags: [Items]
                  parameters:
                    - in: path
                      name: id
                      schema:
                        type: integer
                      required: true
                  responses:
                    '204':
                      description: No Content
        """)

        assertEquals(1, pages.size)
        assertEquals("Items", pages[0].tag)
        assertEquals(4, pages[0].operations.size)
    }

    @Test
    fun `operation with no tag falls back to Default`() {
        val pages = parse("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /health:
                get:
                  operationId: healthCheck
                  responses:
                    '200':
                      description: OK
        """)

        assertEquals(1, pages.size)
        assertEquals("Default", pages[0].tag)
    }

    @Test
    fun `missing operationId falls back to method_path format`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                get:
                  tags: [Items]
                  responses:
                    '200':
                      description: OK
        """)

        // method "get" + "_" + path "/items" with "/" replaced by "_" → "get__items"
        assertEquals("get__items", op.id)
    }

    @Test
    fun `package name is propagated to all page objects`() {
        val pages = parse("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /a:
                get:
                  tags: [A]
                  responses:
                    '200':
                      description: OK
              /b:
                get:
                  tags: [B]
                  responses:
                    '200':
                      description: OK
        """)
        assertTrue(pages.all { it.packageName == "com.example" })
    }

    @Test
    fun `summary is captured on the operation`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                get:
                  operationId: listItems
                  summary: Fetch all items with pagination
                  tags: [Items]
                  responses:
                    '200':
                      description: OK
        """)

        assertEquals("Fetch all items with pagination", op.summary)
    }

    @Test
    fun `operation without summary has null summary`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                get:
                  operationId: listItems
                  tags: [Items]
                  responses:
                    '200':
                      description: OK
        """)

        assertNull(op.summary)
    }

    @Test
    fun `endpoint without security override is authenticated by default`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                get:
                  operationId: listItems
                  tags: [Items]
                  responses:
                    '200':
                      description: OK
        """)

        assertTrue(op.authenticated)
    }

    @Test
    fun `endpoint with security set to empty array is unauthenticated`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /public:
                get:
                  operationId: getPublic
                  security: []
                  tags: [Public]
                  responses:
                    '200':
                      description: OK
        """)

        assertFalse(op.authenticated)
    }

    @Test
    fun `endpoint with explicit non-empty security is authenticated`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /items:
                get:
                  operationId: listItems
                  tags: [Items]
                  security:
                    - bearerAuth: []
                  responses:
                    '200':
                      description: OK
            components:
              securitySchemes:
                bearerAuth:
                  type: http
                  scheme: bearer
        """)

        assertTrue(op.authenticated)
    }

    @Test
    fun `mixed authenticated and unauthenticated endpoints in same spec`() {
        val pages = parse("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /secured:
                get:
                  operationId: getSecured
                  tags: [Api]
                  responses:
                    '200':
                      description: OK
              /public:
                get:
                  operationId: getPublic
                  security: []
                  tags: [Api]
                  responses:
                    '200':
                      description: OK
        """)

        val ops = pages.single().operations.associateBy { it.id }
        assertTrue(ops["getSecured"]!!.authenticated)
        assertFalse(ops["getPublic"]!!.authenticated)
    }

    @Test
    fun `DELETE on public endpoint is unauthenticated`() {
        val op = singleOp("""
            openapi: 3.0.3
            info:
              title: T
              version: 1.0.0
            paths:
              /cache/{key}:
                delete:
                  operationId: evictCacheKey
                  security: []
                  tags: [Cache]
                  parameters:
                    - in: path
                      name: key
                      schema:
                        type: string
                      required: true
                  responses:
                    '204':
                      description: No Content
        """)

        assertFalse(op.authenticated)
        assertEquals("delete", op.verb)
        assertEquals(204, op.successStatus)
    }
}
