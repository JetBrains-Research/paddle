package io.paddle.plugin.python.dependencies

class Dependencies {
    private val _dependencies: MutableList<CachedPackage> = arrayListOf()

    fun all(): Set<CachedPackage> {
        return _dependencies.toSet()
    }

    fun register(dependency: CachedPackage) {
        _dependencies.add(dependency)
    }
}
