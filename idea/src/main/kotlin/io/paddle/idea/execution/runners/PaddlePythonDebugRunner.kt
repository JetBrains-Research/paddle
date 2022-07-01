package io.paddle.idea.execution.runners

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultDebugExecutor
import com.jetbrains.python.debugger.PyDebugRunner
import io.paddle.idea.execution.PaddleRunConfiguration

class PaddlePythonDebugRunner : PyDebugRunner() {
    override fun getRunnerId(): String = "PaddlePythonDebugRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return profile is PaddleRunConfiguration && DefaultDebugExecutor.EXECUTOR_ID == executorId
    }
}
