package io.paddle.utils.config

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.nio.file.Path
import kotlin.io.path.*

object PaddleApplicationSettings : KoinComponent {
    interface PaddleHomeProvider {
        /**
         * Provide a path to `PADDLE_HOME`
         */
        fun getPath(): Path
    }

    private val paddleHomeProvider: PaddleHomeProvider by inject()
    val paddleHome: Path
        get() = paddleHomeProvider.getPath()

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
