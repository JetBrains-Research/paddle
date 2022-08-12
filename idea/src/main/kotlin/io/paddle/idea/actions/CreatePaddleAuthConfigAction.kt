package io.paddle.idea.actions

import com.intellij.ide.actions.CreateFileAction
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import icons.PythonIcons
import java.util.function.Consumer

class CreatePaddleAuthConfigAction : CreateFileAction(
    "Paddle Auth YAML",
    "Create new Paddle Authentication configuration file",
    PythonIcons.Python.Python
) {
    override fun create(newName: String, directory: PsiDirectory): Array<PsiElement> {
        val fileName = getFileName(newName)
        val template = FileTemplateManager.getInstance(directory.project).getTemplate(fileName)

        if (directory.findFile(fileName) != null) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Paddle")
                .createNotification("$fileName already exists.", NotificationType.ERROR)
                .notify(directory.project)
            return emptyArray()
        }

        val psiElement = FileTemplateUtil.createFromTemplate(template, fileName, null, directory)
        return arrayOf(psiElement)
    }

    override fun invokeDialog(project: Project, directory: PsiDirectory, elementsConsumer: Consumer<in Array<PsiElement>>) {
        val validator: MyInputValidator = MyValidator(project, directory)
        elementsConsumer.accept(validator.create(getFileName(null)))
    }

    override fun getDefaultExtension(): String = DEFAULT_EXTENSION

    override fun getFileName(newName: String?): String = "paddle.auth.$DEFAULT_EXTENSION"

    companion object {
        const val DEFAULT_EXTENSION = "yaml"
    }
}
