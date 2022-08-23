package io.paddle.utils.ext

open class Extendable {
    private val storage = HashMap<Key<*>, Any>()

    @Suppress("unused")
    class Key<T>

    fun <T : Any> register(key: Key<T>, value: T) {
        storage[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: Key<T>): T? {
        return storage[key] as T?
    }

    inline fun <reified T : Any> getOrFail(key: Key<T>): T {
        return checkNotNull(get(key)) { "Could not load extension ${T::class.qualifiedName}." }
    }


    operator fun <T : Any> contains(key: Key<T>): Boolean {
        return key in storage
    }
}
