package io.paddle.idea.settings.global

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import io.paddle.idea.copypaste.common.ConverterType
import kotlin.reflect.KMutableProperty0

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

        fun getDontShowDialogOnPasteBind(converterType: ConverterType): KMutableProperty0<Boolean> =
            when (converterType) {
                ConverterType.Poetry -> getInstance()::isDontShowDialogOnPoetryPaste
                ConverterType.RequirementsTxt -> getInstance()::isDontShowDialogOnRequirementTxtPaste
            }
    }

    override fun getState(): PaddleAppSettings = this

    override fun loadState(state: PaddleAppSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
