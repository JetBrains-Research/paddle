package io.paddle.plugin.docker

import io.paddle.project.Project
import io.paddle.terminal.TerminalUI
import io.paddle.utils.ext.Extendable

class DockerWrapper(val project: Project) {
    object Extension : Project.Extension<DockerWrapper> {
        override val key: Extendable.Key<DockerWrapper> = Extendable.Key()

        override fun create(project: Project): DockerWrapper {
            return DockerWrapper(project)
        }
    }

    val image: String?
        get() = project.config.get("docker.image")

    fun shouldBeWrapped(): Boolean {
        return image != null && !DockerWrapperClient.isWrapped()
    }

    fun startWrappedSession(args: List<String>) {
        TerminalUI.echoln("> Executor :docker: ${TerminalUI.colored("EXECUTE", TerminalUI.Color.YELLOW)}")
        DockerWrapperClient.startWrappedSession(image!!, project, args)
        TerminalUI.echoln("> Executor :docker: ${TerminalUI.colored("DONE", TerminalUI.Color.GREEN)}")
    }
}

val Project.docker: DockerWrapper
    get() = extensions.get(DockerWrapper.Extension.key) as DockerWrapper
