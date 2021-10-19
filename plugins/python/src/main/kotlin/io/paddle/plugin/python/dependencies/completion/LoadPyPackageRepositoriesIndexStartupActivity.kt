package io.paddle.plugin.python.dependencies.completion

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import io.paddle.plugin.python.dependencies.index.PyPackageRepositories

class LoadPyPackageRepositoriesIndexStartupActivity : StartupActivity.Background {
    override fun runActivity(project: Project) {
        PyPackageRepositories
    }
}
