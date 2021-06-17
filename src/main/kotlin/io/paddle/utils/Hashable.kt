package io.paddle.utils

import kotlinx.serialization.SerializationStrategy
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.io.File

interface Hashable {
    fun hash(): String
}

class EmptyHashable: Hashable {
    override fun hash(): String {
        return "empty"
    }
}

class AggregatedHashable(private val hashables: List<Hashable>) : Hashable {
    override fun hash(): String {
        val hashes = hashables.map { it.hash() }
        return DigestUtils.md5Hex(hashes.joinToString(separator = "|"))
    }
}

fun List<Hashable>.hashable() = AggregatedHashable(this)

class ObjectHashable<T : Any>(private val serializer: SerializationStrategy<T>, private val obj: T) : Hashable {
    override fun hash(): String {
        return StringHashable(JSON.string(serializer, obj)).hash()
    }
}

fun <T : Any> T.hashable(serializer: SerializationStrategy<T>) = ObjectHashable(serializer, this)

class StringHashable(private val input: String) : Hashable {
    override fun hash(): String {
        return DigestUtils.md5Hex(input)
    }
}

fun String.hashable() = StringHashable(this)

class FileHashable(private val file: File) : Hashable {
    init {
        require(file.isFile) { "FileHashable is not a file" }
    }

    override fun hash(): String {
        val path = StringHashable(file.canonicalPath)
        val digest = StringHashable(Hex.encodeHexString(DigestUtils.digest(DigestUtils.getMd5Digest(), file)))
        return AggregatedHashable(listOf(path, digest)).hash()
    }
}

class FolderHashable(private val folder: File) : Hashable {
    init {
        require(folder.isDirectory) { "FolderHashable is not a folder" }
    }

    override fun hash(): String {
        val path = StringHashable(folder.absolutePath)
        val files = folder.walkTopDown().asSequence().filter { it.isFile }.map { FileHashable(it) }
        return AggregatedHashable(listOf(path) + files).hash()
    }
}

fun File.hashable(): Hashable {
    if (isFile) return FileHashable(this)
    if (isDirectory) return FolderHashable(this)
    throw IllegalArgumentException("Unknown type of file at $absolutePath")
}