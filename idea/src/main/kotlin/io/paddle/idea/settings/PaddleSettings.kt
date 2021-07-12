package io.paddle.idea.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.util.xmlb.annotations.XCollection

@State(name = "PaddleSettings", storages = [Storage("paddle.xml")])
class PaddleSettings(project: Project) : AbstractExternalSystemSettings<
    PaddleSettings,
    PaddleProjectSettings,
    PaddleProjectSettings.Listener
    >(PaddleProjectSettings.Listener.TOPIC, project),
    PersistentStateComponent<PaddleSettings.MyState> {

    class MyState : State<PaddleProjectSettings> {
        private val mySettings = HashSet<PaddleProjectSettings>()

        @XCollection(elementTypes = [PaddleProjectSettings::class])
        override fun getLinkedExternalProjectsSettings(): MutableSet<PaddleProjectSettings> {
            return mySettings
        }

        override fun setLinkedExternalProjectsSettings(settings: MutableSet<PaddleProjectSettings>?) {
            mySettings.addAll(settings ?: return)
        }
    }

    companion object {
        fun getInstance(project: Project): PaddleSettings {
            return project.getService(PaddleSettings::class.java)
        }
    }


    override fun subscribe(listener: ExternalSystemSettingsListener<PaddleProjectSettings>) {
        project.messageBus.connect().subscribe(changesTopic, PaddleProjectSettings.Listener.DelegatingAdapter(listener))
    }

    override fun copyExtraSettingsFrom(settings: PaddleSettings) {
    }

    override fun checkSettings(old: PaddleProjectSettings, current: PaddleProjectSettings) {
    }

    override fun loadState(state: MyState) {
        super.loadState(state)
    }

    override fun getState(): MyState {
        val state = MyState()
        fillState(state)
        return state
    }
}
