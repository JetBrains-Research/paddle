package io.paddle.plugin.python.dependencies.lock

import io.paddle.plugin.python.dependencies.lock.models.LockedPyPackage
import io.paddle.plugin.python.utils.jsonParser
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
                error("$FILENAME was not found in the project.")
            }
            return jsonParser.decodeFromString(file.readText())
        }
    }

    fun save(path: Path) {
        val json = Json { prettyPrint = true }
        path.resolve(FILENAME).toFile().writeText(json.encodeToString(this))
    }
}
