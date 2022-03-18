package io.paddle.utils.hash

import io.paddle.utils.json.JSON
import kotlinx.serialization.SerializationStrategy
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.Adler32


interface Hashable {
    fun hash(): String
}

class EmptyHashable : Hashable {
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

fun Boolean.hashable() = StringHashable(this.toString())

class FileHashable(private val file: File) : Hashable {
    override fun hash(): String {
        if (!file.exists()) return StringHashable("empty_file").hash()
        if (file.isDirectory) return hashFolder()
        if (file.isFile) return hashFile()
        error("Unexpected type of hashable file")
    }

    private fun hashFile(): String {
        val ad32 = Adler32().apply { update(file.readBytes()) }
        val updatedAt = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java).lastModifiedTime()
        return ad32.value.toString() + file.canonicalPath + updatedAt
    }

    private fun hashFolder(): String {
        val path = StringHashable(file.absolutePath)
        val files = file.walkTopDown().asSequence().filter { it.isFile }.map { FileHashable(it) }
        return AggregatedHashable(listOf(path) + files).hash()
    }
}

fun File.hashable(): Hashable {
    return FileHashable(this)
}
