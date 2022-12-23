package io.paddle.idea.copypaste.common

import io.paddle.idea.copypaste.poetry.PoetryConverter
import io.paddle.idea.copypaste.requirements.RequirementsTxtConverter
import io.paddle.plugin.python.utils.PyPackagesRepositoryUrl
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.StringWriter

@Suppress("UNCHECKED_CAST")
abstract class ConverterBase constructor(private val paddleConfig: MutableMap<String, Any>) {
    abstract val sections: Set<String>

    val yaml: String
        get() {
            val writer = StringWriter()
            Yaml(yamlDumpOptions).dump(paddleConfig, writer)
            return writer.toString() + "\n"
        }


    fun getYamlSectionString(section: String): String {
        val sectionMap = paddleConfig.filterKeys { it == section }
        if (sectionMap.isEmpty()) return ""

        val writer = StringWriter()
        Yaml(yamlDumpOptions).dump(sectionMap, writer)
        return writer.toString() + "\n"
    }

    private val yamlDumpOptions = DumperOptions().apply {
        defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    }

    companion object {
        fun ConverterType.getConverter(text: String, paddleConfig: MutableMap<String, Any>): ConverterBase {
            return when (this) {
                ConverterType.Poetry -> PoetryConverter.from(text, paddleConfig)
                ConverterType.RequirementsTxt -> RequirementsTxtConverter.from(text, paddleConfig)
            }
        }

        fun putRepo(
            config: MutableMap<String, Any>,
            isExtra: Boolean,
            name: String,
            url: PyPackagesRepositoryUrl
        ) {
            val repos: MutableList<Map<String, Any>> by lazy {
                config.getOrPut("repositories") { ArrayList<Map<String, Any>>() } as MutableList<Map<String, Any>>
            }
            if (isExtra && !repos.any { it["name"] == name && it["url"] == url && it["secondary"] == "True" }) {
                repos.add(linkedMapOf("name" to name, "url" to url, "secondary" to "True"))
            } else if (!isExtra && !repos.any { it["name"] == name && it["url"] == url && it["default"] == "True" }) {
                repos.add(linkedMapOf("name" to name, "url" to url, "default" to "True"))
            }
        }
    }
}
