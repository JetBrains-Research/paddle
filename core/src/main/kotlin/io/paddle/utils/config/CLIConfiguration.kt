package io.paddle.utils.config

class CLIConfiguration internal constructor(initMap: Map<String, String>) : Configuration() {
    private val options: Map<String, Any>

    init {
        options = buildMap {
            initMap.forEach { (key, value) -> put(key, value.prepare().tryCastOrString()) }
        }
    }


    override fun <T> get(key: String): T? = options[key] as T?

    /**
     * Convert CLI Arguments to supported (by Configuration) types, or stay as string otherwise
     */
    private fun String.tryCastOrString(): Any {
        return toBooleanStrictOrNull()
            ?: toIntOrNull()
            ?: toDoubleOrNull()
            ?: this
    }

    private fun String.prepare(): String =
        when {
            startsWith("\"") && endsWith("\"") -> this.drop(1).dropLast(1)
            startsWith("'") && endsWith("'") -> this.drop(1).dropLast(1)
            else -> this
        }
}
