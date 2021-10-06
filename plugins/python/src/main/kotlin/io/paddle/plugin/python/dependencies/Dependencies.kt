package io.paddle.plugin.python.dependencies

class Dependencies {
    private val dependencies: MutableList<CachedPackage> = arrayListOf()

    fun all(): Set<CachedPackage> {
        return dependencies.toSet()
    }

    fun register(dependency: CachedPackage) {
        dependencies.add(dependency)
    }
}
