package io.paddle.utils.ext

open class Extendable {
    private val storage = HashMap<Key<*>, Any>()

    @Suppress("unused")
    class Key<T>

    fun <T: Any> register(key: Key<T>, value: T) {
        storage[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> get(key: Key<T>) : T? {
        return storage[key] as T?
    }
}
