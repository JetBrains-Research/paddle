package io.paddle.plugin.python.dependencies.index

import io.paddle.utils.StringHashable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PyPackagesRepository(val url: String) {
    @Transient
    val urlSimple: String = "$url/simple"

    @Transient
    val name: String = StringHashable(url).hash()

    val index: MutableMap<String, List<String>> = hashMapOf()
}
