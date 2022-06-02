package io.paddle.idea.execution.providers

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.runners.ExecutionEnvironment
import java.io.File

data class PaddleTaskRunProfileStateContext(
    val moduleDir: File,
    val rootDir: File,
    val environment: ExecutionEnvironment,
    val originalRunConfiguration: RunConfiguration
)
