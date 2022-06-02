package io.paddle.idea.execution.runners

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultRunExecutor
import com.jetbrains.python.run.PythonRunner
import io.paddle.idea.execution.PaddleRunConfiguration

class PaddlePythonRunner : PythonRunner() {
    override fun getRunnerId(): String = "PaddlePythonRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return profile is PaddleRunConfiguration && DefaultRunExecutor.EXECUTOR_ID == executorId
    }
}
