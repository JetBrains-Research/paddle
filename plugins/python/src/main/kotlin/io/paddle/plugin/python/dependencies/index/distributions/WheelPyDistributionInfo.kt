package io.paddle.plugin.python.dependencies.index.distributions

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class WheelPyDistributionInfo(
    override val version: String,
    override val buildTag: String? = null,
    val requiresPython: String? = null,
    val abiTag: String? = null,
    val platformTag: String? = null,
    override val distributionFilename: String
) : PyDistributionInfo() {

    @Transient
    val ext: String = ".whl"

    companion object {
        private val WHEEL_DISTRIBUTION_PATTERN = Regex(
            "^(?<name>.*?)-" +
                "(?<version>.*?)" +
                "(?<buildTag>(-[0-9].*)?)-" +
                "(?<pyTag>((py|cp|ip|pp|jy)[0-9]+)|py2.py3?)-" +
                "(?<abiTag>[^-]+?)-" +
                "(?<platformTag>[^-]+?)" +
                "\\.whl\$"
        )

        fun fromString(filename: String): WheelPyDistributionInfo? {
            val matchResult = WHEEL_DISTRIBUTION_PATTERN.find(filename)
            if (matchResult != null) {
                val version = matchResult.groups["version"]?.value!!
                val buildTag = matchResult.groups["buildTag"]?.value?.drop(1)
                val pyTag = matchResult.groups["pyTag"]?.value
                val abiTag = matchResult.groups["abiTag"]?.value
                val platformTag = matchResult.groups["platformTag"]?.value
                return WheelPyDistributionInfo(version, buildTag, pyTag, abiTag, platformTag, filename)
            }
            return null
        }
    }
}
