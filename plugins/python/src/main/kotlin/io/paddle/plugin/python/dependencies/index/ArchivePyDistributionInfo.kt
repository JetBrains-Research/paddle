package io.paddle.plugin.python.dependencies.index

import kotlinx.serialization.Serializable

@Serializable
data class ArchivePyDistributionInfo(
    override val version: String,
    override val buildTag: String? = null,
    val ext: String,
    override val distrFilename: String
) : PyDistributionInfo() {

    companion object {
        private val ARCHIVE_DISTRIBUTION_PATTERN = Regex(
            "^(?<name>.*?)-" +
                "(?<version>.*?)" +
                "(?<buildTag>(-[0-9].*)?)" +
                "(?<archExt>\\.tar\\.gz|\\.zip?)"
        )

        fun fromString(filename: String): ArchivePyDistributionInfo? {
            val matchResult = ARCHIVE_DISTRIBUTION_PATTERN.find(filename)
            if (matchResult != null) {
                val version = matchResult.groups["version"]?.value!!
                val buildTag = matchResult.groups["buildTag"]?.value?.drop(1)
                val archExt = matchResult.groups["archExt"]?.value!!
                return ArchivePyDistributionInfo(version, buildTag, archExt, filename)
            }
            return null
        }
    }

}
