package io.paddle.plugin.python.utils

internal object RegexCache {
    val UPPER_CASE_SPLIT_REGEX by lazy { Regex("(?=\\p{Upper})") }
    val PYTHON_DIR_NAME_REGEX by lazy { Regex("python\\d.\\d") }
    val PYTHON_VERSION_REGEX by lazy { Regex("[123](.[0-9]+)*") }

    private val distInfoRegexCache = HashMap<String, Regex>()
    private val packageRelatedRegexCache = HashMap<Pair<String, String>, Regex>()

    fun getDistInfoRegex(packageName: String): Regex =
        distInfoRegexCache.getOrPut(packageName) {
            Regex("^$packageName-[.0-9]+\\.dist-info\$")
        }

    fun getPackageRelatedRegex(packageName: String, topLevelName: String): Regex =
        packageRelatedRegexCache.getOrPut(packageName to topLevelName) {
            Regex("^.*[\\-_]*(${packageName}|${topLevelName})(-|\\.|_|c\$|c\\.|\$|).*\$")
        }
}
