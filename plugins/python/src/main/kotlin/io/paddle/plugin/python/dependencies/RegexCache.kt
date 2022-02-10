package io.paddle.plugin.python.dependencies

internal object RegexCache {
    val UPPER_CASE_SPLIT_REGEX = Regex("(?=\\p{Upper})")
    val PYTHON_DIR_NAME_REGEX = Regex("python\\d.\\d")

    private val distInfoRegexCache = HashMap<String, Regex>()
    private val packageRelatedRegexCache = HashMap<Pair<String, String>, Regex>()

    fun getDistInfoRegex(packageName: String): Regex =
        distInfoRegexCache.getOrPut(packageName) { Regex("^$packageName-[.0-9]+\\.dist-info\$") }

    fun getPackageRelatedRegex(packageName: String, topLevelName: String): Regex =
        packageRelatedRegexCache.getOrPut(Pair(packageName, topLevelName)) {
            Regex("^.*[\\-_]*(${packageName}|${topLevelName})(-|\\.|_|c\$|c\\.|\$|).*\$")
        }
}
