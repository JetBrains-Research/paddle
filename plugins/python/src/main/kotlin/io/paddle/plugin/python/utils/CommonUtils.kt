package io.paddle.plugin.python.utils

import kotlinx.coroutines.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path


typealias PyPackageName = String
typealias PyPackageVersion = String

fun Path.exists(): Boolean = Files.exists(this)

fun String.isValidUrl(): Boolean = try {
    URL(this).toURI()
    true
} catch (e: Exception) {
    false
}

suspend fun <A> Iterable<A>.parallelForEach(action: suspend (A) -> Unit): Unit = coroutineScope {
    map { launch { action(it) } }.joinAll()
}

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

fun <T, U> Collection<T>.product(other: Collection<U>): List<Pair<T, U>> {
    return this.flatMap { lhsElem -> other.map { rhsElem -> lhsElem to rhsElem } }
}
