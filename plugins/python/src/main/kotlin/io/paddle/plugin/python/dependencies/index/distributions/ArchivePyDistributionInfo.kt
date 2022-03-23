package io.paddle.plugin.python.dependencies.index.distributions

import kotlinx.serialization.Serializable

@Serializable
data class ArchivePyDistributionInfo(
    override val name: String,
    override val version: String,
    override val buildTag: String? = null,
    override val ext: String,
    override val distributionFilename: String
) : PyDistributionInfo() {

    companion object {
        private val ARCHIVE_DISTRIBUTION_PATTERN by lazy {
            Regex(
                "^(?<name>.*?)-" +
                    "(?<version>.*?)" +
                    "(?<buildTag>(-[0-9].*)?)" +
                    "(?<archExt>\\.tar\\.gz|\\.zip?)"
            )
        }

        fun fromString(filename: String): ArchivePyDistributionInfo? {
            val matchResult = ARCHIVE_DISTRIBUTION_PATTERN.find(filename)
            if (matchResult != null) {
                val name = matchResult.groups["name"]?.value!!
                val version = matchResult.groups["version"]?.value!!
                val buildTag = matchResult.groups["buildTag"]?.value?.drop(1)
                val archExt = matchResult.groups["archExt"]?.value!!
                return ArchivePyDistributionInfo(name, version, buildTag, archExt, filename)
            }
            return null
        }
    }
}
