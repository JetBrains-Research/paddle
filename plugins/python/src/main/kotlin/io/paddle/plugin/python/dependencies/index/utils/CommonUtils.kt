package io.paddle.plugin.python.dependencies.index.utils

typealias PyPackageName = String

internal fun Iterable<String>.letters(): Set<Char> = flatMap { it.toSet() }.toSet()

internal fun <T> Iterable<T>.withIndexAt(start: Int) = withIndex().map { IndexedValue(it.index + start, it.value) }

internal fun ByteArray.compare(other: ByteArray) = this.compare(0, size, other)

internal fun ByteArray.compare(start: Int, size: Int, other: ByteArray): Int {
    if (size != other.size) return size - other.size
    //compare elements values
    for (i in 0 until size) {
        val diff = this[i + start] - other[i]

        if (diff != 0) return diff
    }
    return 0
}

private val emptyByteArray = ByteArray(0)

internal fun emptyByteArray() = emptyByteArray
