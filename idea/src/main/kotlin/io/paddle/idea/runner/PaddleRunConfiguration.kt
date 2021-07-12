// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package io.paddle.idea.runner

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import io.paddle.idea.PaddleManager
import org.jdom.Element

class PaddleRunConfiguration(project: Project?, factory: ConfigurationFactory?, name: String?) :
    ExternalSystemRunConfiguration(PaddleManager.ID, project, factory, name) {
    var isDebugAllEnabled = false

    override fun getState(executor: Executor, env: ExecutionEnvironment): RunProfileState? {
        putUserData<Boolean>(DEBUG_FLAG_KEY, java.lang.Boolean.valueOf(isDebugServerProcess()))
        putUserData<Boolean>(DEBUG_ALL_KEY, java.lang.Boolean.valueOf(isDebugAllEnabled))
        return super.getState(executor, env)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        val child = element.getChild(DEBUG_FLAG_NAME)
        if (child != null) {
            setDebugServerProcess(java.lang.Boolean.valueOf(child.text))
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
        const val DEBUG_FLAG_NAME = "GradleScriptDebugEnabled"
        const val DEBUG_ALL_NAME = "DebugAllEnabled"
        val DEBUG_FLAG_KEY = Key.create<Boolean>("DEBUG_GRADLE_SCRIPT")
        val DEBUG_ALL_KEY = Key.create<Boolean>("DEBUG_ALL_TASKS")

    }

    init {
        setDebugServerProcess(true)
        setReattachDebugProcess(true)
    }
}
