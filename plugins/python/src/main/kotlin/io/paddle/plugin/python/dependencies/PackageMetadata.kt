package io.paddle.plugin.python.dependencies

import java.io.File

class PackageMetadata private constructor(private val config: Map<String, List<String>>) {
    companion object {
        fun parse(file: File): PackageMetadata {
            val config = mutableMapOf<String, MutableList<String>>()
            for (line in file.readLines()) {
                if (line.matches(Regex("[-\\w]+: .+"))) {
                    val key = line.substringBefore(": ")
                    val value = line.substringAfter(": ")
                    config[key]?.add(value) ?: run { config[key] = mutableListOf(value) }
                }
            }
            return PackageMetadata(config)
        }
    }

    fun get(key: String) = config[key]

    val requiresDist: List<String> // TODO: parse and resolve versions constraints
        get() =
            if (!config.containsKey("Requires-Dist"))
                emptyList()
            else
                config["Requires-Dist"]!!.map { distDescription -> distDescription.substringBefore(" ") }


    val providesExtra: List<String>
        get() =
            if (!config.containsKey("Provides-Extra"))
                emptyList()
            else
                config["Provides-Extra"]!!.map { distDescription -> distDescription.substringBefore(" ") }
}
