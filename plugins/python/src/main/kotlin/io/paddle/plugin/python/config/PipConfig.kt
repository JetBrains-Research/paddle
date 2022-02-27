package io.paddle.plugin.python.config

import io.paddle.plugin.python.utils.exists
import org.codehaus.plexus.util.Os
import org.ini4j.Ini
import java.nio.file.Paths

class PipConfig private constructor(
    private val userConfig: Ini? = null,
    private val globalConfig: Ini? = null,
) {
    companion object {
        fun getInstance(): PipConfig {
            when {
                Os.isFamily(Os.FAMILY_MAC) -> {
                    val home = System.getenv("HOME")!!
                    val userConfig = Paths.get(home, "Library", "Application Support", "pip", "pip.conf")
                        .takeIf { it.exists() }?.let { Ini(it.toFile()) }
                        ?: Paths.get(home, ".config", "pip", "pip.conf")
                            .takeIf { it.exists() }?.let { Ini(it.toFile()) }
                    val globalConfig = Paths.get("Library", "Application Support", "pip", "pip.conf")
                        .takeIf { it.exists() }?.let { Ini(it.toFile()) }

                    return PipConfig(userConfig, globalConfig)
                }
                else -> TODO()
            }
        }
    }

    private fun resolve(sectionName: String, optionName: String): String? {
        return userConfig?.get(sectionName, optionName)
            ?: globalConfig?.get(sectionName, optionName)
    }

    val indexUrl: String?
        get() = resolve("global", "indexUrl")

    val extraIndexUrl: String?
        get() = resolve("global", "extraIndexUrl")
}
