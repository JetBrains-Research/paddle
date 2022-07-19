package io.paddle.utils.yaml

import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.resolver.ScalarResolver

internal object YAML {
    private val yaml: Load

    init {
        val scalarResolver = ScalarResolver { _, _ -> Tag.STR }
        yaml = Load(LoadSettings.builder().setScalarResolver(scalarResolver).build())
    }

    inline fun <reified V> parse(text: String): V {
        return yaml.loadFromString(text) as V
    }
}

