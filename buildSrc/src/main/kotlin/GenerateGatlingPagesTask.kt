import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
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

abstract class GenerateGatlingPagesTask : DefaultTask() {

    @get:InputFile
    abstract val specFile: RegularFileProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    /** Used to emit a literal "$" into generated Kotlin source without triggering interpolation. */
    private val D = "\$"

    @TaskAction
    fun generate() {
        val api = OpenAPIV3Parser().read(specFile.get().asFile.absolutePath)
            ?: error("Failed to parse OpenAPI spec at ${specFile.get().asFile.absolutePath}")

        val pkg = packageName.get()
        val dir = outputDir.get().asFile.also { it.deleteRecursively(); it.mkdirs() }

        val byTag = linkedMapOf<String, MutableList<Triple<String, PathItem.HttpMethod, Operation>>>()
        api.paths?.forEach { (path, item) ->
            item.readOperationsMap()?.forEach { (method, op) ->
                val tag = op.tags?.firstOrNull() ?: "Default"
                byTag.getOrPut(tag) { mutableListOf() }.add(Triple(path, method, op))
            }
        }

        byTag.forEach { (tag, ops) ->
            File(dir, "${tag}Page.kt").writeText(buildPageObject(pkg, tag, ops, api))
        }
    }

    // ─── Page object ─────────────────────────────────────────────────────────────

    private fun buildPageObject(
        pkg: String,
        tag: String,
        ops: List<Triple<String, PathItem.HttpMethod, Operation>>,
        api: OpenAPI,
    ) = buildString {
        appendLine("package $pkg")
        appendLine()
        appendLine("// Generated from OpenAPI specification — do not edit manually.")
        appendLine()
        appendLine("import io.gatling.javaapi.core.ChainBuilder")
        appendLine("import io.gatling.javaapi.core.CoreDsl.*")
        appendLine("import io.gatling.javaapi.http.HttpDsl.*")
        appendLine()
        appendLine("object ${tag}Page : BasePage() {")
        appendLine()
        ops.forEach { (path, method, op) ->
            append(buildOperation(path, method, op, api))
        }
        appendLine("}")
    }

    // ─── Operation (static + session methods) ────────────────────────────────────

    private fun buildOperation(
        path: String,
        method: PathItem.HttpMethod,
        op: Operation,
        api: OpenAPI,
    ) = buildString {
        val id = op.operationId ?: "${method.name.lowercase()}_${path.replace("/", "_")}"
        val httpVerb = method.name.lowercase()

        val pathParams = op.parameters?.filter { it.`in` == "path" } ?: emptyList()
        val queryParams = op.parameters?.filter { it.`in` == "query" } ?: emptyList()

        val bodySchema = op.requestBody?.content?.get("application/json")?.schema
            ?.let { resolveRef(it, api) }

        val (successCode, respSchema) = op.responses
            ?.entries
            ?.firstOrNull { it.key.startsWith("2") }
            ?.let { (code, resp) ->
                val schema = resp.content?.get("application/json")?.schema?.let { resolveRef(it, api) }
                (code.toIntOrNull() ?: 200) to schema
            } ?: (200 to null)

        op.summary?.let { appendLine("    // ${method.name} $path — $it") }

        // ── Static method ──────────────────────────────────────────────────────
        val sig = buildParamList(pathParams, queryParams, bodySchema, api)
        appendLine("    fun $id(${sig.joinToString(", ")}): ChainBuilder {")

        val staticUrl = buildStaticUrl(path, pathParams)
        appendLine("        var req = http(\"$id\").$httpVerb(\"$staticUrl\")")

        queryParams.forEach { p ->
            if (p.required == true)
                appendLine("        req = req.queryParam(\"${p.name}\", ${p.name})")
            else
                appendLine("        if (${p.name} != null) req = req.queryParam(\"${p.name}\", ${p.name})")
        }

        if (bodySchema != null) {
            appendLine("        val bodyParts = mutableListOf<String>()")
            val props = effectiveProps(bodySchema, api)
            val reqFields = effectiveRequired(bodySchema, api)
            props.forEach { (name, propSchema) ->
                val valueExpr = if (isNumericOrBool(propSchema)) "$D$name" else "\\\"$D$name\\\""
                if (name in reqFields)
                    appendLine("        bodyParts.add(\"\\\"$name\\\":$valueExpr\")")
                else
                    appendLine("        if ($name != null) bodyParts.add(\"\\\"$name\\\":$valueExpr\")")
            }
            appendLine("        req = req.header(\"Content-Type\", \"application/json\")")
            appendLine("            .body(StringBody(\"{${D}{bodyParts.joinToString(\",\")}}\"))")
        }

        appendLine("        return exec(req")
        appendLine("            .check(statusIs($successCode))")
        addResponseChecks(this, id, respSchema, api)
        appendLine("        )")
        appendLine("    }")
        appendLine()

        // ── Session method (only when there are params or a body) ──────────────
        val hasSessionVariants = queryParams.isNotEmpty() || bodySchema != null || pathParams.isNotEmpty()
        if (hasSessionVariants) {
            appendLine("    fun ${id}FromSession(): ChainBuilder {")

            val sessionUrl = buildSessionUrl(path, pathParams)
            appendLine("        var req = http(\"$id (session)\").$httpVerb(\"$sessionUrl\")")

            queryParams.forEach { p ->
                appendLine("        req = req.queryParam(\"${p.name}\", \"#{${p.name}}\")")
            }

            if (bodySchema != null) {
                val props = effectiveProps(bodySchema, api)
                val reqFields = effectiveRequired(bodySchema, api)
                val bodyStr = reqFields
                    .filter { props.containsKey(it) }
                    .joinToString(",") { name ->
                        val schema = props[name]!!
                        if (isNumericOrBool(schema)) "\"$name\":#{$name}" else "\"$name\":\"#{$name}\""
                    }
                val tq = "\"\"\""
                appendLine("        req = req.header(\"Content-Type\", \"application/json\")")
                appendLine("            .body(StringBody($tq{$bodyStr}$tq))")
            }

            appendLine("        return exec(req")
            appendLine("            .check(statusIs($successCode))")
            addResponseChecks(this, id, respSchema, api)
            appendLine("        )")
            appendLine("    }")
            appendLine()
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private fun buildParamList(
        pathParams: List<Parameter>,
        queryParams: List<Parameter>,
        bodySchema: Schema<*>?,
        api: OpenAPI,
    ): List<String> {
        val params = mutableListOf<String>()
        pathParams.forEach { p -> params.add("${p.name}: ${toKotlinType(p.schema)}") }
        bodySchema?.let { schema ->
            val props = effectiveProps(schema, api)
            val req = effectiveRequired(schema, api)
            props.forEach { (name, propSchema) ->
                if (name in req) params.add("$name: ${toKotlinType(propSchema)}")
                else params.add("$name: ${toKotlinType(propSchema)}? = null")
            }
        }
        queryParams.forEach { p ->
            if (p.required == true) params.add("${p.name}: ${toKotlinType(p.schema)}")
            else params.add("${p.name}: ${toKotlinType(p.schema)}? = null")
        }
        return params
    }

    private fun buildStaticUrl(path: String, pathParams: List<Parameter>): String {
        val interpolated = pathParams.fold(path) { acc, p ->
            acc.replace("{${p.name}}", "$D{${p.name}}")
        }
        return "${D}baseUrl$interpolated"
    }

    private fun buildSessionUrl(path: String, pathParams: List<Parameter>): String {
        val interpolated = pathParams.fold(path) { acc, p ->
            acc.replace("{${p.name}}", "#{${p.name}}")
        }
        return "${D}baseUrl$interpolated"
    }

    private fun addResponseChecks(sb: StringBuilder, opId: String, schema: Schema<*>?, api: OpenAPI) {
        schema ?: return
        @Suppress("UNCHECKED_CAST")
        if (schema.type == "array" || schema is ArraySchema) {
            sb.appendLine("            .check(jsonPath(\"${D}[*]\").ofList().saveAs(\"${opId}List\"))")
        } else {
            effectiveProps(schema, api).keys.forEach { fieldName ->
                sb.appendLine("            .check(jsonPath(\"${D}.$fieldName\").saveAs(\"$fieldName\"))")
            }
        }
    }

    private fun resolveRef(schema: Schema<*>, api: OpenAPI): Schema<*> {
        val ref = schema.`$ref` ?: return schema
        val name = ref.substringAfterLast("/")
        return api.components?.schemas?.get(name) ?: schema
    }

    @Suppress("UNCHECKED_CAST")
    private fun effectiveProps(schema: Schema<*>, api: OpenAPI): Map<String, Schema<*>> {
        val result = linkedMapOf<String, Schema<*>>()
        schema.allOf?.forEach { sub -> result.putAll(effectiveProps(resolveRef(sub, api), api)) }
        schema.properties?.forEach { (k, v) -> result[k] = v as Schema<*> }
        return result
    }

    private fun effectiveRequired(schema: Schema<*>, api: OpenAPI): Set<String> {
        val result = mutableSetOf<String>()
        schema.allOf?.forEach { sub -> result.addAll(effectiveRequired(resolveRef(sub, api), api)) }
        schema.required?.let { result.addAll(it) }
        return result
    }

    private fun toKotlinType(schema: Schema<*>?): String = when {
        schema == null -> "String"
        schema.type == "string" -> "String"
        schema.type == "number" -> "Double"
        schema.type == "integer" && schema.format == "int64" -> "Long"
        schema.type == "integer" -> "Int"
        schema.type == "boolean" -> "Boolean"
        schema.type == "array" -> "List<Any>"
        else -> "String"
    }

    private fun isNumericOrBool(schema: Schema<*>?): Boolean =
        schema?.type in listOf("number", "integer", "boolean")
}
