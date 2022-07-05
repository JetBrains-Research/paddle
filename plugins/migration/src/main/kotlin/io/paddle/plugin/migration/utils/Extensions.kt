package io.paddle.plugin.migration.utils

import java.io.File

fun File.collectFiles(name: String): Sequence<File> {
    return walkTopDown().filter { it.name == name }
}
