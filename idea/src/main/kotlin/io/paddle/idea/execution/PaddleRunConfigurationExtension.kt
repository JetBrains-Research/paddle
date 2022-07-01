package io.paddle.idea.execution

import com.intellij.openapi.externalSystem.service.execution.configuration.*
import com.intellij.openapi.externalSystem.service.ui.project.path.ExternalSystemWorkingDirectoryInfo
import com.intellij.openapi.externalSystem.service.ui.project.path.WorkingDirectoryField
import com.intellij.openapi.project.Project
import io.paddle.idea.PaddleManager
import io.paddle.idea.execution.beforeRun.PaddleBeforeRunTaskProvider
import io.paddle.idea.execution.cmdline.PaddleCommandLineInfo

class PaddleRunConfigurationExtension
    : ExternalSystemReifiedRunConfigurationExtension<PaddleRunConfiguration>(PaddleRunConfiguration::class.java) {

    override fun SettingsFragmentsContainer<PaddleRunConfiguration>.configureFragments(configuration: PaddleRunConfiguration) {
        val project = configuration.project
        addBeforeRunFragment(PaddleBeforeRunTaskProvider.ID)
        val workingDirectoryField = addWorkingDirectoryFragment(project).component().component
        addCommandLineFragment(project, workingDirectoryField)
    }

    private fun SettingsFragmentsContainer<PaddleRunConfiguration>.addWorkingDirectoryFragment(
        project: Project
    ) = addWorkingDirectoryFragment(
        project = project,
        workingDirectoryInfo = ExternalSystemWorkingDirectoryInfo(project, PaddleManager.ID)
    )

    private fun SettingsFragmentsContainer<PaddleRunConfiguration>.addCommandLineFragment(
        project: Project,
        workingDirectoryField: WorkingDirectoryField
    ) = addCommandLineFragment(
        project = project,
        commandLineInfo = PaddleCommandLineInfo(project, workingDirectoryField),
        getCommandLine = { rawCommandLine },
        setCommandLine = { rawCommandLine = it }
    )
}
