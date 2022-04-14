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

open class FileHashable(private val file: File, private val hashingFunction: (File) -> String = File::checksumHash) : Hashable {
    override fun hash(): String {
        if (!file.exists()) return StringHashable("empty_file").hash()
        if (file.isDirectory) return hashFolder()
        if (file.isFile) return hashingFunction(file)
        error("Unexpected type of hashable file")
    }

    open protected fun hashFolder(): String {
        val files = file.walkTopDown().asSequence().filter { it.isFile }.map { hashingFunction(it) }
        return StringHashable(file.canonicalPath + files.joinToString("|")).hash()
    }
}

class LightFileHashable(private val file: File) : FileHashable(file, File::attributesHash) {
    override fun hashFolder(): String {
        if (file.isVenv()) return StringHashable(file.canonicalPath).hash()
        return super.hashFolder()
    }
}

fun File.hashable(): Hashable {
    return FileHashable(this)
}

fun File.lightHashable(): Hashable {
    return LightFileHashable(this)
}

fun File.checksumHash(): String {
    val ad32 = Adler32().apply { update(readBytes()) }
    val updatedAt = Files.readAttributes(toPath(), BasicFileAttributes::class.java).lastModifiedTime()
    return ad32.value.toString() + canonicalPath + updatedAt
}

fun File.attributesHash(): String {
    val updatedAt = Files.readAttributes(toPath(), BasicFileAttributes::class.java).lastModifiedTime()
    return canonicalPath + updatedAt
}

fun File.isVenv(): Boolean {
    return resolve("bin").exists()
        && resolve("lib").exists()
        && resolve("include").exists()
        && resolve("pyvenv.cfg").exists()
}
