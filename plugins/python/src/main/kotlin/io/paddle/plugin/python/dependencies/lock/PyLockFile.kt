package io.paddle.plugin.python.dependencies.lock

import io.paddle.plugin.python.dependencies.lock.models.LockedPyPackage
import io.paddle.plugin.python.utils.jsonParser
import io.paddle.tasks.Task
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Path

@Serializable
class PyLockFile(val interpreterVersion: String, val lockedPackages: Set<LockedPyPackage>) {
    companion object {
        const val FILENAME = "paddle-lock.json"

        fun fromFile(file: File): PyLockFile {
            if (!file.exists()) {
                throw Task.ActException("$FILENAME was not found in the project.")
            }
            return jsonParser.decodeFromString(serializer(), file.readText())
        }
    }

    fun save(path: Path) {
        val json = Json { prettyPrint = true }
        path.resolve(FILENAME).toFile().writeText(json.encodeToString(serializer(), this))
    }
}
