package io.paddle.plugin.migration.tasks.migrate

import io.paddle.plugin.migration.utils.collectFiles
import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectProvider
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.*
import java.io.File
import kotlin.system.measureTimeMillis

class CreateDefaultBuildFilesTask(project: PaddleProject) : IncrementalTask(project) {
    override val id: String = "createDefaultBuildFiles"

    override val group: String = "migrate"

    override val outputs: List<Hashable>
        get() = listOf(AggregatedHashable(project.workDir.collectFiles("paddle.yaml").map { it.hashable() }.toList()))

    override fun act() {
        project.terminal.info("Creating paddle.yaml files...")
        val duration = measureTimeMillis {
            project.workDir.walkTopDown().asSequence()
                .filter { it.name == "requirements.txt" && !it.parentFile.resolve("paddle.yaml").exists() }
                .forEach {
                    it.parentFile.resolve("paddle.yaml").apply {
                        if (!exists()) {
                            createNewFile()
                        }
                        val name = it.parentFile.relativeTo(project.rootDir).path
                            .split(File.separator)
                            .joinToString("-")
                        writeText(
                            """
                                descriptor:
                                    name: $name
                                    version: 0.1.0
                                """.trimIndent()
                        )
                    }
                }
            PaddleProjectProvider.getInstance(project.rootDir).sync()
        }
        project.terminal.info("Finished: ${duration}ms")
    }
}
