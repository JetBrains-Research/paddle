package io.paddle.plugin.python.dependencies.packages

import io.paddle.plugin.python.dependencies.repositories.PyPackageRepoMetadataSerializer
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.utils.*
import kotlinx.serialization.Serializable

/**
 * Implementation of the resolved version of `Requirements.Descriptor`
 */
@Serializable
class PyPackage(
    override val name: PyPackageName,
    override val version: PyPackageVersion,
    @Serializable(with = PyPackageRepoMetadataSerializer::class) override val repo: PyPackageRepository,
    override val distributionUrl: PyPackageUrl,
    var comesFrom: PyPackage? = null,
    val findLinkSource: PyUrl? = null
) : IResolvedPyPackage {
    override fun hashCode(): Int {
        if (distributionUrl.contains('#')) {
            // to avoid hashes at the end, like ...#sha256=...
            return distributionUrl.substringBeforeLast('#').hashCode()
        }
        return distributionUrl.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PyPackage

        if (name != other.name) return false
        if (version != other.version) return false
        if (repo != other.repo) return false

        if (distributionUrl.contains('#')
            && other.distributionUrl.contains('#')
            && distributionUrl != other.distributionUrl
        ) return false

        return true
    }

    override fun toString(): String {
        return "$name==$version"
    }

    data class Version(val epoch: Int, val release: List<Int>, val pre: Pair<Int, Int>, val post: Int = 0, val dev: Int = 0) : Comparable<Version> {
        companion object {
            private val regex = Regex(
                "^(?<epoch>(\\d)+!?)?" +
                    "(?<release>\\d+(\\.\\d+)*?)" + "(\\+.+?)?" +
                    "(?<pre>(a|b|rc)(\\d)*?)?" +
                    "(?<post>\\.post(\\d)*?)?" +
                    "(?<dev>\\.dev(\\d)*?)?$"
            )

            fun from(number: String): Version? {
                val matchResult = regex.find(number)
                if (matchResult != null) {
                    val epoch = matchResult.groups["epoch"]?.value?.toInt() ?: 0
                    val release = matchResult.groups["release"]?.value!!.split(".").map { it.toInt() }
                    val pre = matchResult.groups["pre"]?.value
                    val preParsed = pre?.let {
                        when {
                            pre.startsWith("a") -> 1 to (pre.substringAfter("a").toIntOrNull() ?: 0)
                            pre.startsWith("b") -> 2 to (pre.substringAfter("b").toIntOrNull() ?: 0)
                            pre.startsWith("rc") -> 3 to (pre.substringAfter("rc").toIntOrNull() ?: 0)
                            else -> 0 to 0
                        }
                    } ?: (0 to 0)
                    val post = matchResult.groups["post"]?.value?.substringAfter(".post")?.toInt() ?: 0
                    val dev = matchResult.groups["dev"]?.value?.substringAfter(".dev")?.toInt() ?: 0

                    return Version(epoch, release, preParsed, post, dev)
                }

                return null
            }
        }

        override fun compareTo(other: Version): Int {
            if (epoch == other.epoch) {
                for ((i, num) in release.withIndex()) {
                    if (i >= other.release.size) return num
                    val otherNum = other.release[i]
                    if (num == otherNum) continue
                    return num - otherNum
                }
                return if (pre.first == other.pre.first) {
                    if (pre.second == other.pre.second) {
                        if (post == other.post) {
                            if (dev == other.dev) {
                                0
                            } else dev - other.dev
                        } else post - other.post
                    } else pre.second - other.pre.second
                } else pre.first - other.pre.first
            } else {
                return epoch - other.epoch
            }
        }
    }
}
