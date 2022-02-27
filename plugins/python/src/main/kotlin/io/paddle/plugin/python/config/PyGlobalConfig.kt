package io.paddle.plugin.python.config

import io.paddle.plugin.python.PyLocations
import io.paddle.plugin.python.extensions.Repositories
import io.paddle.plugin.python.utils.getDefaultName
import io.paddle.plugin.python.utils.getSimple
import io.paddle.utils.config.Configuration
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileWriter

class PyGlobalConfig(file: File) {
    val repoDescriptors: List<Repositories.Descriptor>

    /**
     * If `$PADDLE_HOME/config.yaml` file exists, then it is assumed to be a global configuration for Paddle.
     *
     * Otherwise, Paddle tries to find `pip.conf` file somewhere on your local machine, and if it succeeds,
     * it parses and saves a new `config.yaml` file.
     */
    init {
        if (file.exists()) {
            repoDescriptors = (Configuration.from(file).get<List<Map<String, String>>>("repositories") ?: emptyList())
                .map {
                    Repositories.Descriptor(
                        it["name"]!!,
                        it["url"]!!,
                        it["default"]?.toBoolean() ?: false,
                        it["secondary"]?.toBoolean() ?: true,
                    )
                }
        } else {
            val descriptors = arrayListOf<Repositories.Descriptor>()
            val pipConfig = PipConfig.getInstance()

            pipConfig.indexUrl?.let { url ->
                descriptors += Repositories.Descriptor(
                    url.getDefaultName(),
                    url.getSimple(),
                    default = true,
                    secondary = false
                )
            }
            pipConfig.extraIndexUrl?.let { url ->
                descriptors += Repositories.Descriptor(
                    url.getDefaultName(),
                    url.getSimple(),
                    default = false,
                    secondary = true
                )
            }

            repoDescriptors = descriptors

            val writer = FileWriter(PyLocations.globalConfig)
            val options = DumperOptions()
            options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            Yaml(options).dump(
                mapOf("repositories" to descriptors.map {
                    mapOf(
                        "name" to it.name,
                        "url" to it.url,
                        "default" to it.default,
                        "secondary" to it.secondary
                    )
                }), writer
            )
        }
    }
}
