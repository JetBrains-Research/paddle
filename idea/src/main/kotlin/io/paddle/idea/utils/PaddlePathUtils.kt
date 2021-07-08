package io.paddle.idea.utils

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path
import kotlin.io.path.isRegularFile


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
        isRegularFile() && fileName.toString() == "paddle.yaml"
