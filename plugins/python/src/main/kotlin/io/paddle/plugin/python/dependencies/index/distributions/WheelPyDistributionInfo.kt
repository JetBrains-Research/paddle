package io.paddle.plugin.python.dependencies.index.distributions

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class WheelPyDistributionInfo(
    override val name: String,
    override val version: String,
    override val buildTag: String? = null,
    val requiresPython: String,
    val abiTag: String,
    val platformTag: String,
    override val distributionFilename: String
) : PyDistributionInfo() {

    @Transient
    override val ext: String = ".whl"

    companion object {
        private val WHEEL_DISTRIBUTION_PATTERN by lazy {
            Regex(
                "^(?<name>(.*)?)-" +
                    "(?<version>.*?)" +
                    "(?<buildTag>(-[0-9].*)?)-" +
                    "(?<pyTag>((py|cp|ip|pp|jy)([0-9]+))(\\.(py|cp|ip|pp|jy)([0-9]+))*)-" +
                    "(?<abiTag>[^-]+?)-" +
                    "(?<platformTag>[^-]+?)" +
                    "\\.whl\$"
            )
        }

        fun fromString(filename: String): WheelPyDistributionInfo? {
            val matchResult = WHEEL_DISTRIBUTION_PATTERN.find(filename)
            if (matchResult != null) {
                val name = matchResult.groups["name"]?.value!!
                val version = matchResult.groups["version"]?.value!!
                val buildTag = matchResult.groups["buildTag"]?.value?.drop(1)
                val pyTag = matchResult.groups["pyTag"]!!.value
                val abiTag = matchResult.groups["abiTag"]!!.value
                val platformTag = matchResult.groups["platformTag"]!!.value
                return WheelPyDistributionInfo(name, version, buildTag, pyTag, abiTag, platformTag, filename)
            }
            return null
        }
    }
}
