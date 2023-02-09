package io.paddle.idea.settings.global

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.ui.dsl.builder.*
import io.paddle.idea.settings.global.PaddleAppSettings.TaskTypeOnProjectReload.INSTALL
import io.paddle.idea.settings.global.PaddleAppSettings.TaskTypeOnProjectReload.RESOLVE

class PaddleAppSettingsConfigurable : BoundSearchableConfigurable(
    displayName = "Paddle Application Settings",
    helpTopic = ""
) {
    private val settings = PaddleAppSettings.getInstance()

    override fun createPanel() = panel {
        buttonsGroup {
            row("Run task on project reload:") {
                radioButton(INSTALL.message, INSTALL)
                radioButton(RESOLVE.message, RESOLVE)
            }
        }.bind(
            { settings.onReload },
            { settings.onReload = it }
        )
        row {
            checkBox("Don't show copy-paste dialog for requirements.txt next time")
                .bindSelected(PaddleAppSettings.getInstance()::isDontShowDialogOnRequirementTxtPaste)
        }
        row {
            checkBox("Disable pip's caching behaviour (pass --no-cache-dir flag)").bindSelected(
                { settings.noCacheDir},
                {
                    settings.noCacheDir = it
                    // TODO: save to config
                }
            )
        }
        row {
            checkBox("Replace cached wheels with newer versions from PyPI if possible").bindSelected(
                { settings.autoRemove },
                {
                    settings.autoRemove = it
                    // TODO: save to config
                }
            )
        }
    }
}
