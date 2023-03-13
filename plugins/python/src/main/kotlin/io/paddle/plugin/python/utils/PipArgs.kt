package io.paddle.plugin.python.utils

internal class PipArgs private constructor(val args: List<String>) {

    companion object {
        inline fun build(command: String, block: Builder.() -> Unit) = Builder(command).apply(block).build()
    }

    class Builder(private val command: String /* is there enum should be passed? */) {
        var noDeps: Boolean = false
        var noCacheDir: Boolean = false
        var additionalArgs: List<String> = emptyList()
        var packages: List<PyUrl> = listOf()
        var noIndex: Boolean = false
        fun build() = PipArgs(buildList {
            add("-m")
            add("pip")
            add(command)
            if (noDeps) {
                add("--no-deps")
            }
            if (noCacheDir) {
                add("--no-cache-dir")
            }
            if (noIndex) {
                add("--no-index")
            }
            addAll(additionalArgs)
            addAll(packages)
        })
    }
}
