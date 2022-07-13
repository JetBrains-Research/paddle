package io.paddle.idea.execution.state

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.runners.ExecutionEnvironment
import java.io.File

data class PaddleTaskRunProfileStateContext(
    val moduleDir: File,
    val rootDir: File,
    val executor: Executor,
    val environment: ExecutionEnvironment,
    val originalRunConfiguration: RunConfiguration
)
