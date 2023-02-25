package io.paddle.utils.config
class CLIConfiguration internal constructor(initMap: Map<String, String>) : Configuration() {
    private val options: Map<String, Any>
    init {
        options = buildMap {
            initMap.forEach { (key, value) -> put(key, value.tryCastOrString()) }
        }
    }


    override fun <T> get(key: String): T? = options[key] as T?

    /**
     * Convert CLI Arguments to supported (by Configuration) types, or stay as string otherwise
     */
    private fun String.tryCastOrString(): Any =
        toBooleanStrictOrNull()
            ?: toIntOrNull()
            ?: toDoubleOrNull()
            ?: this
}
