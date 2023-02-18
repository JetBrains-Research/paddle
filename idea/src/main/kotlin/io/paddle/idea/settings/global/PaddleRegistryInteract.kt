package io.paddle.idea.settings.global

import io.paddle.utils.config.ConfigurationYAML
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.nio.file.Path
import kotlin.io.path.readText

object PaddleRegistryInteract {

    private val PADDLE_HOME: Path
        get() = System.getenv("PADDLE_HOME")?.let { Path.of(it) } ?: System.getProperty("user.home").let { Path.of(it).resolve(".paddle") }

    private val REGISTRY_PATH: Path
        get() = PADDLE_HOME.resolve("registry.yaml")
    private val config: MutableMap<String, Any>
        get() = REGISTRY_PATH.takeIf { it.readText().isNotEmpty() }
            ?.let { ConfigurationYAML.from(it.toFile()).toMutableMap() } ?: mutableMapOf()


    private fun modify(key: String, value: Any) = dumpConfig(config.run { set(key, value); this })

    private fun dumpConfig(map: MutableMap<String, Any>) {
        val options = DumperOptions().also {
            it.isPrettyFlow = true
            it.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            it.defaultScalarStyle = DumperOptions.ScalarStyle.PLAIN
        }
        Yaml(options).dump(map, REGISTRY_PATH.toFile().bufferedWriter())
    }

    object Python {
        private val pythonConfig: MutableMap<String, Any>
            get() = config["python"]?.let { it as? MutableMap<String, Any> } ?: mutableMapOf()

        private fun modify(key: String, value: Any) = PaddleRegistryInteract.modify("python", pythonConfig.run { set(key, value); this })

        var noCacheDir: Boolean
            get() = (pythonConfig["noCacheDir"] as? String)?.toBooleanStrictOrNull() ?: false
            set(x) = modify("noCacheDir", x)
        var autoRemove: Boolean
            get() = (pythonConfig["autoRemove"] as? String)?.toBooleanStrictOrNull() ?: false
            set(x) = modify("autoRemove", x)
    }
}
