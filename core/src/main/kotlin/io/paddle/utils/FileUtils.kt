package io.paddle.utils

import io.paddle.utils.hash.StringHashable
import java.io.File
import java.io.FileFilter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.Adler32

fun File.deleteRecursivelyWithoutSymlinks() {
    if (this.isDirectory && !Files.isSymbolicLink(this.toPath())) {
        val children = this.listFiles() ?: return
        children.forEach { it.deleteRecursivelyWithoutSymlinks() }
    }
    this.delete()
}

fun Path.exists(): Boolean = Files.exists(this)

val File.isPaddle: Boolean
    get() = exists() && name == "paddle.yaml"

fun File.checksumHash(): String {
    val ad32 = Adler32().apply { update(readBytes()) }
    val updatedAt = Files.readAttributes(toPath(), BasicFileAttributes::class.java).lastModifiedTime()
    return ad32.value.toString() + canonicalPath + updatedAt
}

fun File.fileAttributesHash(): String {
    val updatedAt = Files.readAttributes(toPath(), BasicFileAttributes::class.java).lastModifiedTime()
    return canonicalPath + updatedAt
}

fun File.lightHash(): String {
    if (!exists()) return "empty"
    return if (isDirectory) lightDirectoryHash() else fileAttributesHash()
}

fun File.lightDirectoryHash(): String {
    val hashes = listFiles(FileFilter { !it.isVenv() })?.map { it.lightHash() } ?: emptyList()
    return StringHashable(canonicalPath + hashes.joinToString("|")).hash()
}

fun File.isVenv(): Boolean {
    return resolve("bin").exists()
        && resolve("lib").exists()
        && resolve("include").exists()
        && resolve("pyvenv.cfg").exists()
}
