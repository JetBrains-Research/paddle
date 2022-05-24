package io.paddle.idea.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemLocalSettings
import com.intellij.openapi.project.Project
import io.paddle.idea.PaddleManager

@State(name = "PaddleLocalSettings", storages = [Storage(StoragePathMacros.CACHE_FILE)])
class PaddleLocalSettings(project: Project) :
    AbstractExternalSystemLocalSettings<AbstractExternalSystemLocalSettings.State>(PaddleManager.ID, project),
    PersistentStateComponent<AbstractExternalSystemLocalSettings.State> {

}

