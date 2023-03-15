package io.paddle.utils.config

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.nio.file.Path
import kotlin.io.path.*

object PaddleApplicationSettings {

    val paddleHome: Path
        get() {
            val fromEnv = System.getenv("PADDLE_HOME")?.let { Path.of(it) }
            val result = fromEnv ?: System.getProperty("user.home").let { Path.of(it).resolve(".paddle") }
            if (!result.exists()) result.createDirectories()
            return result
        }

    val registry: Path
        get() = paddleHome.resolve("registry.yaml").apply { if (!exists()) { createFile()} }
    private val config: MutableMap<String, Any>
        get() = registry.takeIf { it.readText().isNotEmpty() }
            ?.let { ConfigurationYAML.from(it.toFile()).toMutableMap() } ?: mutableMapOf()


    private fun modify(key: String, value: Any) = dumpConfig(config.run { set(key, value); this })

    private fun dumpConfig(map: MutableMap<String, Any>) {
        val options = DumperOptions().apply {
            isPrettyFlow = true
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            defaultScalarStyle = DumperOptions.ScalarStyle.PLAIN
        }
        Yaml(options).dump(map, registry.toFile().bufferedWriter())
    }

    object Python {
        private val pythonConfig: MutableMap<String, Any>
            get() = config["python"]?.let { it as? MutableMap<String, Any> } ?: mutableMapOf()

        private fun modify(key: String, value: Any) = PaddleApplicationSettings.modify("python", pythonConfig.run { set(key, value); this })

        var noCacheDir: Boolean
            get() = (pythonConfig["noCacheDir"] as? String)?.toBooleanStrictOrNull() ?: false
            set(x) = modify("noCacheDir", x)
        var autoRemove: Boolean
            get() = (pythonConfig["autoRemove"] as? String)?.toBooleanStrictOrNull() ?: true
            set(x) = modify("autoRemove", x)
    }
}
