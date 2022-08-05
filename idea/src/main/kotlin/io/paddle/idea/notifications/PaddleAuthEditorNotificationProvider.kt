package io.paddle.idea.notifications

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import java.io.File
import java.util.function.Function
import javax.swing.JComponent

class PaddleAuthEditorNotificationProvider : EditorNotificationProvider {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?> {
        if (file.name != "paddle.auth.yaml") return EditorNotificationProvider.CONST_NULL
        val rootDir = project.basePath?.let { File(it) } ?: return EditorNotificationProvider.CONST_NULL

        if (file.canonicalPath != rootDir.resolve("paddle.auth.yaml").canonicalPath) {
            return Function {
                EditorNotificationPanel().apply {
                    text = "File paddle.auth.yaml must be placed in the root directory of the project: ${project.basePath}"
                }
            }
        }

        return EditorNotificationProvider.CONST_NULL
    }
}
