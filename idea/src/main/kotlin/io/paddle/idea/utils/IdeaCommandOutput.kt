package io.paddle.idea.utils

import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import io.paddle.terminal.TextOutput

class IDEACommandOutput(val id: ExternalSystemTaskId, private val listener: ExternalSystemTaskNotificationListener) : TextOutput {
    override fun stdout(text: String) {
        listener.onTaskOutput(id, text, true)
    }

    override fun stderr(text: String) {
        listener.onTaskOutput(id, text, false)
    }
}
