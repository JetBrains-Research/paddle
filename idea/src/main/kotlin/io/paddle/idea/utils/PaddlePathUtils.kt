package io.paddle.idea.utils

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.exists
import com.intellij.util.io.isDirectory
import java.io.File
import java.nio.file.Files.isRegularFile
import java.nio.file.Path


/**
 * @return whether this file is a `Paddle` build file.
 */
internal val VirtualFile.isPaddle: Boolean
    get() =
        VfsUtil.virtualToIoFile(this).toPath().isPaddle

/**
 * @return whether this file exists and is a `Paddle` build file.
 */
internal val Path.isPaddle: Boolean
    get() =
        isRegularFile(this) && fileName.toString() == "paddle.yaml"

internal fun Path.findPaddleInDirectory(): Path? {
    require(isDirectory()) { "Trying to find paddle.yaml not in directory" }
    return resolve("paddle.yaml").takeIf { it.exists() }
}

fun File.containsPrefix(other: File): Boolean {
    return canonicalPath.startsWith(other.canonicalPath)
}
