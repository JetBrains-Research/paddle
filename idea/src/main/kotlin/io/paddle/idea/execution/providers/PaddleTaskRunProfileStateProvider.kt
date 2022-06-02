package io.paddle.idea.execution.providers

import com.intellij.execution.configurations.RunProfileState
import com.intellij.openapi.extensions.ExtensionPointName
import io.paddle.tasks.Task


interface PaddleTaskRunProfileStateProvider<T : Task> {
    fun getState(task: T, context: PaddleTaskRunProfileStateContext): RunProfileState?

    companion object {
        var EP_NAME: ExtensionPointName<PaddleTaskRunProfileStateProvider<*>> =
            ExtensionPointName.create("io.paddle.idea.runProfileStateProvider")

        fun <T : PaddleTaskRunProfileStateProvider<*>> findInstance(cls: Class<T>): T? {
            return EP_NAME.findExtension(cls)
        }
    }
}
