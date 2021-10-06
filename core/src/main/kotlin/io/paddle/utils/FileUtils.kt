package io.paddle.utils

import java.io.File
import java.nio.file.Files

fun File.deleteRecursivelyWithoutSymlinks() {
    if (this.isDirectory && !Files.isSymbolicLink(this.toPath())) {
        val children = this.listFiles() ?: return
        children.forEach { it.deleteRecursivelyWithoutSymlinks() }
    }
    this.delete()
}
