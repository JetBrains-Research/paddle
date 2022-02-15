package io.paddle.plugin.python.dependencies.packages

import io.paddle.plugin.python.dependencies.parser.antlr.DependencySpecificationLexer
import io.paddle.plugin.python.dependencies.parser.antlr.DependencySpecificationParser
import io.paddle.plugin.python.utils.RegexCache
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.*
import java.util.*
import javax.mail.Header
import javax.mail.Session
import javax.mail.internet.MimeMessage
import kotlin.properties.ReadOnlyProperty

@Suppress("UNCHECKED_CAST")
class PyPackageMetadata private constructor(private val headers: Map<String, Any?>) {
    private fun <T, V> T.map(headers: Map<String, Any?>): ReadOnlyProperty<T, V> {
        return ReadOnlyProperty { _, property ->
            // transform given property name (e.g., "requiresDist") to the proper header key (e.g., "Requires-Dist")
            val key = property.name.split(RegexCache.UPPER_CASE_SPLIT_REGEX).joinToString("-")
            headers[key[0].uppercaseChar() + key.drop(1)] as V
        }
    }

    val metadataVersion: String by map(headers)
    val name: String by map(headers)
    val version: String by map(headers)

    val summary: String? by map(headers)
    val description: String? by map(headers)

    val requiresDist: List<DependencySpecificationParser.SpecificationContext> by map(headers)
    val requiresPython: DependencySpecificationParser.VersionspecContext by map(headers)
    val providesExtra: List<String> by map(headers)

    companion object {
        fun parse(file: File): PyPackageMetadata {
            val stream: InputStream = ByteArrayInputStream(file.readBytes())
            val content = MimeMessage(Session.getInstance(Properties()), stream)
            val pkgName = content.getHeader("Name").first()
            val headers = mutableMapOf<String, Any?>()
            headers["Requires-Dist"] = mutableListOf<DependencySpecificationParser.SpecificationContext>()
            for (header in content.allHeaders) {
                val (key, value) = Pair((header as Header).name, header.value)
                when (key) {
                    "Requires-Dist" -> {
                        val parser = createDependencySpecificationParser(value) ?: error("Parse error in package $pkgName: '$key: $value'")
                        (headers[key] as MutableList<DependencySpecificationParser.SpecificationContext>).add(parser.specification())
                    }
                    "Requires-Python" -> {
                        val parser = createDependencySpecificationParser(value) ?: error("Parse error in package $pkgName: '$key: $value'")
                        headers[key] = parser.versionspec()
                    }
                    else -> {
                        headers.setOrAdd(key, value)
                    }
                }
            }
            headers.computeIfPresent("Provides-Extra") { _, value -> if (value is String) listOf(value) else value }
            headers.putIfAbsent("Provides-Extra", emptyList<String>())
            return PyPackageMetadata(headers)
        }

        private fun createDependencySpecificationParser(source: String): DependencySpecificationParser? {
            val stream = CharStreams.fromString(source.trim())
            val lexer = DependencySpecificationLexer(stream)
            val commonTokenStream = CommonTokenStream(lexer)
            return DependencySpecificationParser(commonTokenStream)
        }
    }

    fun get(key: String): Any? = headers[key]
}


@Suppress("UNCHECKED_CAST")
fun MutableMap<String, Any?>.setOrAdd(key: String, value: Any?) {
    if (this.containsKey(key) && this[key] is MutableList<*>) {
        (this[key] as MutableList<Any?>).add(value)
    } else if (this.containsKey(key)) {
        this[key] = mutableListOf(this[key], value)
    } else {
        this[key] = value
    }
}
