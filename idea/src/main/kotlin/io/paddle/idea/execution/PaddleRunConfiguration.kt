package io.paddle.idea.execution

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import io.paddle.idea.PaddleManager
import io.paddle.idea.execution.cmd.PaddleCommandLine
import org.jdom.Element
import java.util.*

class PaddleRunConfiguration(project: Project?, factory: ConfigurationFactory?, name: String?) :
    ExternalSystemRunConfiguration(PaddleManager.ID, project, factory, name) {

    init {
        isDebugServerProcess = true
        isReattachDebugProcess = true
    }

    var isDebugAllEnabled = false

    override fun getState(executor: Executor, env: ExecutionEnvironment): RunProfileState? {
        putUserData<Boolean>(DEBUG_FLAG_KEY, java.lang.Boolean.valueOf(isDebugServerProcess()))
        putUserData<Boolean>(DEBUG_ALL_KEY, java.lang.Boolean.valueOf(isDebugAllEnabled))
        return super.getState(executor, env)
    }

    var rawCommandLine: String
        get() {
            val commandLine = StringJoiner(" ")
            for (taskName in settings.taskNames) {
                commandLine.add(taskName)
            }
            return commandLine.toString()
        }
        set(value) {
            commandLine = PaddleCommandLine.parse(value)
        }

    var commandLine: PaddleCommandLine
        get() = PaddleCommandLine.parse(rawCommandLine)
        set(value) {
            settings.taskNames = value.tasksAndArguments.toList()
        }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        val child = element.getChild(DEBUG_FLAG_NAME)
        if (child != null) {
            isDebugServerProcess = java.lang.Boolean.valueOf(child.text)
        }
        val debugAll = element.getChild(DEBUG_ALL_NAME)
        if (debugAll != null) {
            isDebugAllEnabled = java.lang.Boolean.valueOf(debugAll.text)
        }
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        val debugAll = Element(DEBUG_ALL_NAME)
        debugAll.text = isDebugAllEnabled.toString()
        element.addContent(debugAll)
    }

    companion object {
        const val DEBUG_FLAG_NAME = "PaddleScriptDebugEnabled"
        const val DEBUG_ALL_NAME = "DebugAllEnabled"
        val DEBUG_FLAG_KEY = Key.create<Boolean>("DEBUG_PADDLE_SCRIPT")
        val DEBUG_ALL_KEY = Key.create<Boolean>("DEBUG_ALL_TASKS")
    }
}
