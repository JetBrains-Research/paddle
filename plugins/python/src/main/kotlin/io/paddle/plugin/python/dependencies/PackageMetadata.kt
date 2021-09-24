package io.paddle.plugin.python.dependencies

import com.jetbrains.python.packaging.requirement.PyRequirementVersionSpec
import io.paddle.plugin.python.dependencies.parser.MetadataPyRequirementImpl
import io.paddle.plugin.python.dependencies.parser.PyRequirementMetadataParser
import java.io.*
import java.util.*
import javax.mail.Header
import javax.mail.Session
import javax.mail.internet.MimeMessage
import kotlin.properties.ReadOnlyProperty

@Suppress("UNCHECKED_CAST")
class PackageMetadata private constructor(private val headers: Map<String, Any?>) {
    private fun <T, V> T.map(headers: Map<String, Any?>): ReadOnlyProperty<T, V> {
        return ReadOnlyProperty { _, property ->
            // transform given property name (e.g., "requiresDist") to the proper header key (e.g., "Requires-Dist")
            val key = property.name.split(Regex("(?=\\p{Upper})")).joinToString("-")
            headers[key[0].toUpperCase() + key.drop(1)] as V
        }
    }

    val metadataVersion: String by map(headers)
    val name: String by map(headers)
    val version: String by map(headers)

    val summary: String? by map(headers)
    val description: String? by map(headers)

    val requiresDist: List<MetadataPyRequirementImpl> by map(headers)
    val requiresPython: List<PyRequirementVersionSpec> by map(headers)
    val providesExtra: List<String> by map(headers)

    companion object {
        fun parse(file: File): PackageMetadata {
            val stream: InputStream = ByteArrayInputStream(file.readBytes())
            val content = MimeMessage(Session.getInstance(Properties()), stream)

            val headers = mutableMapOf<String, Any?>()
            headers["Requires-Dist"] = mutableListOf<MetadataPyRequirementImpl>();
            for (header in content.allHeaders) {
                val (key, value) = Pair((header as Header).name, header.value)
                when (key) {
                    "Requires-Dist" -> {
                        val dependency = PyRequirementMetadataParser.parseRequirement(value)
                            ?: error("Requires-Dist parse error in package ${headers.getOrDefault("Name", "<unknown>")}.")
                        (headers["Requires-Dist"] as MutableList<MetadataPyRequirementImpl>).add(dependency)
                    }
                    "Requires-Python" ->
                        headers["Requires-Python"] = PyRequirementMetadataParser.parseVersionSpecs(value)
                    else -> {
                        if (headers.containsKey(key) && headers[key] is MutableList<*>) {
                            (headers[key] as MutableList<Any?>).add(value)
                        } else if (headers.containsKey(key)) {
                            headers[key] = mutableListOf(headers[key], value)
                        } else {
                            headers[key] = value
                        }
                    }
                }
            }
            headers.computeIfPresent("Provides-Extra") { _, value -> if (value is String) listOf(value) else value }
            headers.putIfAbsent("Provides-Extra", emptyList<String>())
            return PackageMetadata(headers)
        }
    }

    fun get(key: String): Any? = headers[key]
}
