package io.paddle.idea.notifications

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.AbstractVcs
import com.intellij.openapi.vcs.changes.IgnoredBeanFactory
import com.intellij.openapi.vcs.changes.IgnoredFileBean
import com.intellij.openapi.vcs.changes.ignore.actions.writeIgnoreFileEntries
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.*
import com.intellij.vcsUtil.VcsImplUtil
import com.intellij.vcsUtil.VcsUtil
import java.util.function.Function
import javax.swing.JComponent
import kotlin.io.path.readLines

class VcsIgnoreEditorNotificationProvider : EditorNotificationProvider {
    private val PADDLE_AUTH_YAML_REGEX = Regex("paddle.auth.yaml")
    private val PADDLE_CACHE_DIR_REGEX = Regex("\\*\\*/\\.paddle/(\\*\\*)?")

    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?> {
        val ignoreFileRoot = VcsUtil.getVcsRootFor(project, file) ?: return EditorNotificationProvider.CONST_NULL
        val vcs = VcsUtil.getVcsFor(project, file) ?: return EditorNotificationProvider.CONST_NULL
        val ignoredFileContentProvider = VcsImplUtil.findIgnoredFileContentProvider(vcs) ?: return EditorNotificationProvider.CONST_NULL
        val ignoreFileName = ignoredFileContentProvider.fileName
        val ignoreVirtualFile = ignoreFileRoot.findChild(ignoreFileName) ?: return EditorNotificationProvider.CONST_NULL

        when (file.name) {
            "paddle.auth.yaml" -> {
                if (!ignoreVirtualFile.toNioPath().readLines().any { PADDLE_AUTH_YAML_REGEX matches it }) {
                    return Function {
                        EditorNotificationPanel().apply {
                            text = "File paddle.auth.yaml should be ignored in the VCS"
                            createActionLabel("Add to $ignoreFileName") {
                                runWriteAction {
                                    val ignoredEntries = listOf(IgnoredBeanFactory.withMask("paddle.auth.yaml"))
                                    doIgnoreFileEntries(file, ignoreVirtualFile, ignoreFileRoot, ignoredEntries, vcs, project)
                                }
                            }
                        }
                    }
                }
            }

            ignoreFileName -> {
                if (!ignoreVirtualFile.toNioPath().readLines().any { PADDLE_CACHE_DIR_REGEX matches it }) {
                    return Function {
                        EditorNotificationPanel().apply {
                            text = "Directory .paddle should be ignored in the VCS"
                            createActionLabel("Add to $ignoreFileName") {
                                val ignoredEntries = listOf(IgnoredBeanFactory.withMask("**/.paddle/**"))
                                doIgnoreFileEntries(file, ignoreVirtualFile, ignoreFileRoot, ignoredEntries, vcs, project)
                            }
                        }
                    }
                }
            }
        }

        return EditorNotificationProvider.CONST_NULL
    }

    private fun doIgnoreFileEntries(
        originalFile: VirtualFile,
        ignoreFile: VirtualFile,
        ignoreFileRoot: VirtualFile,
        ignoredEntries: List<IgnoredFileBean>,
        vcs: AbstractVcs,
        project: Project
    ) {
        runWriteAction {
            writeIgnoreFileEntries(project, ignoreFile, ignoredEntries, vcs, ignoreFileRoot)
            EditorNotificationsImpl.getInstance(project).updateNotifications(originalFile)
        }
    }
}
