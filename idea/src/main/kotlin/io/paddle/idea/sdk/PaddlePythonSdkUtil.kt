package io.paddle.idea.sdk

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.module.Module
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.jetbrains.python.configuration.PyConfigurableInterpreterList
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.pythonSdk
import io.paddle.plugin.python.extensions.environment
import io.paddle.plugin.python.extensions.interpreter
import io.paddle.project.extensions.descriptor
import kotlin.io.path.absolutePathString

object PaddlePythonSdkUtil {
    fun configurePythonSdk(module: Module, paddleProject: io.paddle.project.PaddleProject) {
        val existingSdks = PyConfigurableInterpreterList.getInstance(null).model.sdks

        var foundExistingSdk = false
        for (existingSdk in existingSdks) {
            if (existingSdk !is ProjectJdkImpl || existingSdk.homePath == null) continue
            if (pythonBasePathEquals(existingSdk.homePath!!, paddleProject.environment.interpreterPath.absolutePathString())) {
                ApplicationManager.getApplication().invokeLater {
                    module.pythonSdk = existingSdk
                }
                foundExistingSdk = true
            }
        }

        if (!foundExistingSdk) {
            val sdk = SdkConfigurationUtil.createSdk(
                existingSdks.asList(),
                paddleProject.environment.interpreterPath.absolutePathString(),
                PythonSdkType.getInstance(),
                null,
                paddleProject.interpreter.pythonVersion.fullName + " :" + paddleProject.descriptor.name
            ).apply {
                versionString = paddleProject.interpreter.pythonVersion.number
            }
            val jdkTable = ProjectJdkTable.getInstance()

            ApplicationManager.getApplication().invokeLater {
                runWriteActionAndWait {
                    jdkTable.addJdk(sdk)
                    module.pythonSdk = sdk
                }
            }

            PythonSdkType.getInstance().setupSdkPaths(sdk)
        }
    }

    private fun pythonBasePathEquals(path1: String, path2: String): Boolean {
        if (path1 == path2 || path1.startsWith(path2) || path2.startsWith(path1)) return true
        // TODO: check Python installation's version via CLI
        return false
    }
}
