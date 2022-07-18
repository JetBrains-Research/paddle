package io.paddle.idea.execution.runners

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultDebugExecutor
import com.jetbrains.python.debugger.PyDebugRunner
import io.paddle.idea.execution.PaddleRunConfiguration

class PaddlePythonDebugRunner : PyDebugRunner() {
    override fun getRunnerId(): String = "PaddlePythonDebugRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        if (profile !is PaddleRunConfiguration) return false
        val taskName = profile.settings.taskNames.first()
        return DefaultDebugExecutor.EXECUTOR_ID == executorId
            && PaddleRunConfiguration.DEBUGGABLE_TASK_NAMES.any { taskName.startsWith(it) }
    }
}
