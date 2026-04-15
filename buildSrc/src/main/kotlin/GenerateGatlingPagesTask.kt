import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation as OasOperation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.parser.OpenAPIV3Parser
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

// ═══════════════════════════════════════════════════════════════════════════════
// IR — Intermediate Representation
//
// The compiler frontend (OpenApiParser) lowers an OpenAPI spec to these nodes.
// The compiler backend (KotlinPageEmitter) walks them to produce Kotlin source.
// No OpenAPI types or emission strings appear here.
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Kotlin type for a generated parameter or body field, carrying whether its JSON
 * encoding must be surrounded by quotes (strings/lists) or written bare (numbers/booleans).
 */
enum class KotlinType(val typeName: String, val requiresJsonQuotes: Boolean) {
    STRING("String", true),
    INT("Int", false),
    LONG("Long", false),
    DOUBLE("Double", false),
    BOOLEAN("Boolean", false),
    LIST("List<Any>", true),
}

data class Param(val name: String, val type: KotlinType, val required: Boolean)

data class BodyField(val name: String, val type: KotlinType, val required: Boolean)

sealed class ResponseSpec {
    /** Operation returns no body (e.g. 204 No Content). */
    object None : ResponseSpec()

    /** Operation returns a JSON object; these top-level fields are saved to the session. */
    data class ObjectFields(val fieldNames: List<String>) : ResponseSpec()

    /** Operation returns a JSON array; the whole list is saved under [sessionKey]. */
    data class ArrayBody(val sessionKey: String) : ResponseSpec()
}

data class PageOperation(
    val id: String,
    val summary: String?,
    val verb: String,           // lowercase HTTP method: get, post, put, delete, …
    val path: String,           // OAS path template, e.g. "/books/{id}"
    val pathParams: List<Param>,
    val queryParams: List<Param>,
    val bodyFields: List<BodyField>,
    val successStatus: Int,
    val response: ResponseSpec,
) {
    val hasSessionVariant: Boolean
        get() = pathParams.isNotEmpty() || queryParams.isNotEmpty() || bodyFields.isNotEmpty()
}

data class PageObject(
    val packageName: String,
    val tag: String,
    val operations: List<PageOperation>,
)

// ═══════════════════════════════════════════════════════════════════════════════
// Frontend — OpenApiParser
//
// Reads an OpenAPI model and lowers it to PageObject IR nodes.
// All OpenAPI-specific knowledge is confined here.
// ═══════════════════════════════════════════════════════════════════════════════

private object OpenApiParser {

    // ── OAS spec string constants ─────────────────────────────────────────────
    private const val CONTENT_TYPE_JSON = "application/json"
    private const val PARAM_IN_PATH = "path"
    private const val PARAM_IN_QUERY = "query"
    private const val DEFAULT_TAG = "Default"
    private const val SUCCESS_CODE_PREFIX = "2"
    private const val DEFAULT_SUCCESS_STATUS = 200
    private const val ARRAY_SESSION_KEY_SUFFIX = "List"

    // OAS schema type and format identifiers
    private const val OAS_TYPE_STRING = "string"
    private const val OAS_TYPE_INTEGER = "integer"
    private const val OAS_TYPE_NUMBER = "number"
    private const val OAS_TYPE_BOOLEAN = "boolean"
    private const val OAS_TYPE_ARRAY = "array"
    private const val OAS_FORMAT_INT64 = "int64"

    fun parse(api: OpenAPI, packageName: String): List<PageObject> {
        val byTag = linkedMapOf<String, MutableList<PageOperation>>()
        api.paths?.forEach { (path, item) ->
            item.readOperationsMap()?.forEach { (method, op) ->
                val tag = op.tags?.firstOrNull() ?: DEFAULT_TAG
                byTag.getOrPut(tag) { mutableListOf() }
                    .add(lowerOperation(path, method, op, api))
            }
        }
        return byTag.map { (tag, ops) -> PageObject(packageName, tag, ops) }
    }

    private fun lowerOperation(
        path: String,
        method: PathItem.HttpMethod,
        op: OasOperation,
        api: OpenAPI,
    ): PageOperation {
        val pathParams = op.parameters
            ?.filter { it.`in` == PARAM_IN_PATH }
            ?.map { lowerParam(it) }
            ?: emptyList()
        val queryParams = op.parameters
            ?.filter { it.`in` == PARAM_IN_QUERY }
            ?.map { lowerParam(it) }
            ?: emptyList()
        val bodyFields = op.requestBody
            ?.content?.get(CONTENT_TYPE_JSON)?.schema
            ?.let { lowerBodyFields(resolveRef(it, api), api) }
            ?: emptyList()
        val (status, responseSpec) = lowerResponse(op, api)

        return PageOperation(
            id = op.operationId ?: "${method.name.lowercase()}_${path.replace("/", "_")}",
            summary = op.summary,
            verb = method.name.lowercase(),
            path = path,
            pathParams = pathParams,
            queryParams = queryParams,
            bodyFields = bodyFields,
            successStatus = status,
            response = responseSpec,
        )
    }

    private fun lowerParam(p: Parameter) =
        Param(p.name, mapType(p.schema), p.required == true)

    private fun lowerBodyFields(schema: Schema<*>, api: OpenAPI): List<BodyField> {
        val props = collectProperties(schema, api)
        val required = collectRequired(schema, api)
        return props.map { (name, propSchema) -> BodyField(name, mapType(propSchema), name in required) }
    }

    private fun lowerResponse(op: OasOperation, api: OpenAPI): Pair<Int, ResponseSpec> {
        val successEntry = op.responses
            ?.entries
            ?.firstOrNull { it.key.startsWith(SUCCESS_CODE_PREFIX) }
            ?: return DEFAULT_SUCCESS_STATUS to ResponseSpec.None

        val status = successEntry.key.toIntOrNull() ?: DEFAULT_SUCCESS_STATUS
        val schema = successEntry.value.content
            ?.get(CONTENT_TYPE_JSON)?.schema
            ?.let { resolveRef(it, api) }
            ?: return status to ResponseSpec.None

        return if (schema.type == OAS_TYPE_ARRAY || schema is ArraySchema) {
            val sessionKey = (op.operationId ?: "response") + ARRAY_SESSION_KEY_SUFFIX
            status to ResponseSpec.ArrayBody(sessionKey)
        } else {
            status to ResponseSpec.ObjectFields(collectProperties(schema, api).keys.toList())
        }
    }

    private fun resolveRef(schema: Schema<*>, api: OpenAPI): Schema<*> {
        val ref = schema.`$ref` ?: return schema
        val name = ref.substringAfterLast("/")
        return api.components?.schemas?.get(name) ?: schema
    }

    @Suppress("UNCHECKED_CAST")
    private fun collectProperties(schema: Schema<*>, api: OpenAPI): Map<String, Schema<*>> {
        val result = linkedMapOf<String, Schema<*>>()
        schema.allOf?.forEach { sub -> result.putAll(collectProperties(resolveRef(sub, api), api)) }
        schema.properties?.forEach { (k, v) -> result[k] = v as Schema<*> }
        return result
    }

    private fun collectRequired(schema: Schema<*>, api: OpenAPI): Set<String> {
        val result = mutableSetOf<String>()
        schema.allOf?.forEach { sub -> result.addAll(collectRequired(resolveRef(sub, api), api)) }
        schema.required?.let { result.addAll(it) }
        return result
    }

    private fun mapType(schema: Schema<*>?): KotlinType = when {
        schema == null -> KotlinType.STRING
        schema.type == OAS_TYPE_STRING -> KotlinType.STRING
        schema.type == OAS_TYPE_NUMBER -> KotlinType.DOUBLE
        schema.type == OAS_TYPE_INTEGER && schema.format == OAS_FORMAT_INT64 -> KotlinType.LONG
        schema.type == OAS_TYPE_INTEGER -> KotlinType.INT
        schema.type == OAS_TYPE_BOOLEAN -> KotlinType.BOOLEAN
        schema.type == OAS_TYPE_ARRAY -> KotlinType.LIST
        else -> KotlinType.STRING
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Backend — KotlinPageEmitter
//
// Walks the IR and emits Kotlin source. No OpenAPI types appear here.
// ═══════════════════════════════════════════════════════════════════════════════

private object KotlinPageEmitter {

    // ── Generated file header ─────────────────────────────────────────────────
    private const val GENERATED_FILE_COMMENT =
        "// Generated from OpenAPI specification — do not edit manually."
    private val REQUIRED_IMPORTS = listOf(
        "io.gatling.javaapi.core.ChainBuilder",
        "io.gatling.javaapi.core.CoreDsl.*",
        "io.gatling.javaapi.http.HttpDsl.*",
    )

    // ── Target language / framework identifiers ───────────────────────────────
    private const val BASE_CLASS = "BasePage"
    private const val CHAIN_BUILDER_TYPE = "ChainBuilder"
    private const val BASE_URL_PROPERTY = "baseUrl"
    private const val CONTENT_TYPE_HEADER = "Content-Type"
    private const val CONTENT_TYPE_JSON = "application/json"

    // ── Naming conventions for generated methods ──────────────────────────────
    private const val SESSION_METHOD_SUFFIX = "FromSession"
    private const val SESSION_REQUEST_LABEL_SUFFIX = " (session)"

    /**
     * Emitted as a literal `$` inside generated Kotlin string templates.
     * Using a named constant avoids `${'$'}` escape noise throughout the emitter.
     */
    private val DOLLAR = "\$"

    // ─────────────────────────────────────────────────────────────────────────

    fun emit(page: PageObject): String = buildString {
        appendLine("package ${page.packageName}")
        appendLine()
        appendLine(GENERATED_FILE_COMMENT)
        appendLine()
        REQUIRED_IMPORTS.forEach { appendLine("import $it") }
        appendLine()
        appendLine("object ${page.tag}Page : $BASE_CLASS() {")
        appendLine()
        page.operations.forEach { op ->
            appendDirectMethod(op)
            if (op.hasSessionVariant) appendSessionMethod(op)
        }
        appendLine("}")
    }

    // ── Direct-call method ────────────────────────────────────────────────────

    private fun StringBuilder.appendDirectMethod(op: PageOperation) {
        op.summary?.let { appendLine("    // ${op.verb.uppercase()} ${op.path} — $it") }
        appendLine("    fun ${op.id}(${directSignature(op)}): $CHAIN_BUILDER_TYPE {")
        appendLine("        var req = http(\"${op.id}\").${op.verb}(\"${directUrl(op)}\")")

        for (p in op.queryParams) {
            if (p.required)
                appendLine("        req = req.queryParam(\"${p.name}\", ${p.name})")
            else
                appendLine("        if (${p.name} != null) req = req.queryParam(\"${p.name}\", ${p.name})")
        }

        if (op.bodyFields.isNotEmpty()) {
            appendLine("        val bodyParts = mutableListOf<String>()")
            for (f in op.bodyFields) {
                val value = if (f.type.requiresJsonQuotes) "\\\"$DOLLAR${f.name}\\\"" else "$DOLLAR${f.name}"
                if (f.required)
                    appendLine("        bodyParts.add(\"\\\"${f.name}\\\":$value\")")
                else
                    appendLine("        if (${f.name} != null) bodyParts.add(\"\\\"${f.name}\\\":$value\")")
            }
            appendLine("        req = req.header(\"$CONTENT_TYPE_HEADER\", \"$CONTENT_TYPE_JSON\")")
            appendLine("            .body(StringBody(\"{${DOLLAR}{bodyParts.joinToString(\",\")}}\"))")
        }

        appendExecWithChecks(op)
        appendLine("    }")
        appendLine()
    }

    // ── Session method ────────────────────────────────────────────────────────

    private fun StringBuilder.appendSessionMethod(op: PageOperation) {
        appendLine("    fun ${op.id}$SESSION_METHOD_SUFFIX(): $CHAIN_BUILDER_TYPE {")
        appendLine("        var req = http(\"${op.id}$SESSION_REQUEST_LABEL_SUFFIX\").${op.verb}(\"${sessionUrl(op)}\")")

        for (p in op.queryParams) {
            appendLine("        req = req.queryParam(\"${p.name}\", \"#{${p.name}}\")")
        }

        val requiredBodyFields = op.bodyFields.filter { it.required }
        if (requiredBodyFields.isNotEmpty()) {
            val bodyLiteral = requiredBodyFields.joinToString(",") { f ->
                if (f.type.requiresJsonQuotes) "\"${f.name}\":\"#{${f.name}}\""
                else "\"${f.name}\":#{${f.name}}"
            }
            appendLine("        req = req.header(\"$CONTENT_TYPE_HEADER\", \"$CONTENT_TYPE_JSON\")")
            appendLine("            .body(StringBody(\"\"\"{$bodyLiteral}\"\"\"))")
        }

        appendExecWithChecks(op)
        appendLine("    }")
        appendLine()
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private fun StringBuilder.appendExecWithChecks(op: PageOperation) {
        appendLine("        return exec(req")
        appendLine("            .check(statusIs(${op.successStatus}))")
        when (val r = op.response) {
            is ResponseSpec.None -> {}
            is ResponseSpec.ArrayBody ->
                appendLine("            .check(jsonPath(\"$DOLLAR[*]\").ofList().saveAs(\"${r.sessionKey}\"))")
            is ResponseSpec.ObjectFields ->
                r.fieldNames.forEach { field ->
                    appendLine("            .check(jsonPath(\"$DOLLAR.$field\").saveAs(\"$field\"))")
                }
        }
        appendLine("        )")
    }

    private fun directSignature(op: PageOperation): String {
        val parts = mutableListOf<String>()
        op.pathParams.forEach { p -> parts.add("${p.name}: ${p.type.typeName}") }
        op.bodyFields.forEach { f ->
            if (f.required) parts.add("${f.name}: ${f.type.typeName}")
            else parts.add("${f.name}: ${f.type.typeName}? = null")
        }
        op.queryParams.forEach { p ->
            if (p.required) parts.add("${p.name}: ${p.type.typeName}")
            else parts.add("${p.name}: ${p.type.typeName}? = null")
        }
        return parts.joinToString(", ")
    }

    /**
     * OAS path "/books/{id}" → `"$baseUrl/books/${id}"` (Kotlin string template).
     * Path parameter placeholders are replaced with Kotlin template expressions.
     */
    private fun directUrl(op: PageOperation): String {
        val withKotlinVars = op.pathParams.fold(op.path) { acc, p ->
            acc.replace("{${p.name}}", "$DOLLAR{${p.name}}")
        }
        return "$DOLLAR$BASE_URL_PROPERTY$withKotlinVars"
    }

    /**
     * OAS path "/books/{id}" → `"$baseUrl/books/#{id}"` (Gatling EL).
     * Path parameter placeholders are replaced with Gatling Expression Language tokens.
     */
    private fun sessionUrl(op: PageOperation): String {
        val withEL = op.pathParams.fold(op.path) { acc, p ->
            acc.replace("{${p.name}}", "#{${p.name}}")
        }
        return "$DOLLAR$BASE_URL_PROPERTY$withEL"
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Gradle Task — orchestrates the frontend → backend pipeline
// ═══════════════════════════════════════════════════════════════════════════════

abstract class GenerateGatlingPagesTask : DefaultTask() {

    @get:InputFile
    abstract val specFile: RegularFileProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val api = OpenAPIV3Parser().read(specFile.get().asFile.absolutePath)
            ?: error("Failed to parse OpenAPI spec at ${specFile.get().asFile.absolutePath}")

        val dir = outputDir.get().asFile.also { it.deleteRecursively(); it.mkdirs() }

        val pages = OpenApiParser.parse(api, packageName.get())
        pages.forEach { page ->
            File(dir, "${page.tag}Page.kt").writeText(KotlinPageEmitter.emit(page))
        }
    }
}
