package io.paddle.plugin.python.dependencies.interpretator

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.paddle.plugin.python.utils.*
import org.jsoup.Jsoup

data class InterpreterVersion(val number: String) : Comparable<InterpreterVersion> {
    init {
        require(number.matches(RegexCache.PYTHON_VERSION_REGEX) && number.count { it == '.' } <= 2) {
            "Invalid python version specified."
        }
    }

    companion object {
        suspend fun getAvailableRemoteVersions(): Collection<InterpreterVersion> {
            val httpResponse = httpClient.get(PythonPaths.PYTHON_DISTRIBUTIONS_BASE_URL)
            val page = Jsoup.parse(httpResponse.bodyAsText())
            return page.body().getElementsByTag("a")
                .map { it.text().trim('/') }
                .filter { it.matches(RegexCache.PYTHON_VERSION_REGEX) }
                .map { InterpreterVersion(it) }
                .toSet()
        }
    }

    private val parts = number.split(".")
    private val supportedImplementations = listOf("cp", "py")

    val major: Int = parts[0].toInt()
    val minor: Int? = parts.getOrNull(1)?.toInt()
    val patch: Int? = parts.getOrNull(2)?.toInt()

    val pep425candidates: List<String>
        get() {
            minor ?: return supportedImplementations.map { it + major }
            return (minor downTo 0).toList()
                .product(supportedImplementations)
                .map { (minor, impl) -> "$impl$major$minor" } +
                supportedImplementations.map { it + major }
        }

    val executableName: String
        get() = "python${major}"

    val fullName: String
        get() = "Python-$number"

    override fun toString() = number

    /**
     * Check if user defined version matches with current
     */
    fun matches(userDefinedVersion: InterpreterVersion): Boolean {
        return when (userDefinedVersion.number.count { it == '.' }) {
            0 -> major == userDefinedVersion.major
            1 -> number.startsWith(userDefinedVersion.number)
                && userDefinedVersion.number.substringAfter('.') == number.substringAfter('.')
                .substringBefore('.')

            2 -> number == userDefinedVersion.number
            else -> throw IllegalStateException("Invalid python version specified.")
        }
    }

    // Because of versions like "3.10.1" we can't just compare double representations of the strings
    // Also, minor and patch version are not always specified
    override fun compareTo(other: InterpreterVersion): Int {
        if (major == other.major) {
            if (minor == null || other.minor == null) return 0
            if (minor == other.minor) {
                if (patch == null || other.patch == null) return 0
                return patch - other.patch
            } else {
                return minor - other.minor
            }
        } else {
            return major - other.major
        }
    }
}
