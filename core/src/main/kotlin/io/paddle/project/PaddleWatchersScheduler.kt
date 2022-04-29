package io.paddle.project

import java.io.File
import java.nio.file.*


class PaddleWatchersScheduler private constructor(val rootDir: File) {
    companion object {
        private val schedulerByRootDir = HashMap<File, PaddleWatchersScheduler>()

        fun getInstance(rootDir: File): PaddleWatchersScheduler {
            return schedulerByRootDir.getOrPut(rootDir) { PaddleWatchersScheduler(rootDir) }
        }
    }

    fun schedule(workDir: File) {
        val watchService: WatchService = FileSystems.getDefault().newWatchService()

        workDir.toPath().register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY
        )

        var key: WatchKey
        while (watchService.take().also { key = it } != null) {
            for (event: WatchEvent<*> in key.pollEvents()) {
                val entry = rootDir.resolve(event.context().toString())
                if (entry.exists()
                    && entry.isDirectory
                    && entry.resolve("paddle.yaml").exists()
                    && (event.kind() == StandardWatchEventKinds.ENTRY_CREATE)) {
                    PaddleProjectProvider.getInstance(rootDir).sync()
                }
            }
            key.reset()
        }
    }
}
