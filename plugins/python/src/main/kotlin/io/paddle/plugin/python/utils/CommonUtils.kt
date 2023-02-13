package io.paddle.plugin.python.utils

import io.paddle.terminal.Terminal
import kotlinx.coroutines.*
import java.io.File
import java.net.URL
import java.nio.file.*
import java.util.*


typealias PyPackageName = String

fun PyPackageName.normalize(): PyPackageName {
    return this.lowercase().replace('-', '_').replace('.', '_')
}

fun PyPackageName.canonicalize(): PyPackageName {
    return this.lowercase().replace('_', '-').replace(".", "-")
}

fun Path.exists(): Boolean = Files.exists(this)

fun String.isValidUrl(): Boolean = try {
    URL(this).toURI()
    true
} catch (e: Exception) {
    false
}

fun String.isValidPath(): Boolean = try {
    Paths.get(this)
    true
} catch (e: Exception) {
    false
}

suspend fun <A> Iterable<A>.parallelForEach(action: suspend (A) -> Unit): Unit = coroutineScope {
    map { launch { action(it) } }.joinAll()
}

suspend fun <A, R> Iterable<A>.parallelMap(transform: suspend (A) -> R): Iterable<R> = coroutineScope {
    map { async { transform(it) } }.awaitAll()
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

fun File.deepResolve(vararg relatives: String): File {
    return this.resolve(relatives.joinToString(File.separator))
}

fun Path.deepResolve(vararg relatives: String): Path {
    return this.resolve(relatives.joinToString(File.separator))
}

fun File.resolveRelative(other: String): File {
    val parts = other.split(File.separatorChar)
    val up = parts.count { it == ".." }

    var cur = this
    repeat(up) { cur = cur.parentFile }

    val suffix = parts.subList(up, parts.size).joinToString(File.separator)
    return cur.resolve(suffix)
}

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
val snakeRegex = "_[a-zA-Z]".toRegex()

fun String.camelToSnakeCase(): String {
    return camelRegex.replace(this) {
        "_${it.value}"
    }.lowercase(Locale.getDefault())
}

fun <T> Iterable<T>.takeIfAllAreEqual(): Iterable<T>? {
    return takeIf { all { it == first() } }
}

object PaddleLogger {
    var terminal: Terminal = Terminal.MOCK
}

fun Path.isEmpty(): Boolean {
    if (Files.isDirectory(this)) {
        Files.newDirectoryStream(this).use { directory -> return !directory.iterator().hasNext() }
    }
    return false
}
