package io.paddle.idea.settings.global

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "io.paddle.idea.settings.global.PaddleAppSettings",
    storages = [Storage("paddleAppSettings.xml")]
)
class PaddleAppSettings : PersistentStateComponent<PaddleAppSettings> {

    enum class TaskTypeOnProjectReload(val message: String) {
        INSTALL("install"),
        RESOLVE("resolveRequirements")
    }

    var onReload: TaskTypeOnProjectReload = TaskTypeOnProjectReload.INSTALL
    var isDontShowDialogOnRequirementTxtPaste: Boolean = false
    var isDontShowDialogOnPoetryPaste: Boolean = false

    companion object {
        fun getInstance(): PaddleAppSettings =
            ApplicationManager.getApplication().getService(PaddleAppSettings::class.java)
    }

    override fun getState(): PaddleAppSettings = this

    override fun loadState(state: PaddleAppSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
