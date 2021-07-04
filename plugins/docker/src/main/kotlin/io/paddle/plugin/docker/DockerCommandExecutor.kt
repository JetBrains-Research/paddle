package io.paddle.plugin.docker

import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.command.WaitContainerResultCallback
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import io.paddle.execution.CommandExecutor
import io.paddle.project.Project
import io.paddle.terminal.TerminalUI
import io.paddle.utils.ext.Extendable
import java.io.File

class DockerCommandExecutor(private val image: String) : CommandExecutor() {
    object Extension : Project.Extension<DockerCommandExecutor> {
        override val key: Extendable.Key<DockerCommandExecutor> = Extendable.Key()

        override fun create(project: Project): DockerCommandExecutor {
            val image: String? = project.config.get("docker.image")
            return DockerCommandExecutor(image!!)
        }
    }

    private val config = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
    private val http = ApacheDockerHttpClient.Builder().dockerHost(config.dockerHost).sslConfig(config.sslConfig).build()
    private val client = DockerClientImpl.getInstance(config, http)

    override fun execute(command: String, args: Iterable<String>, working: File): Int {
        val (name, tag) = image.split(":")

        if (client.listImagesCmd().exec().all { image !in it.repoTags }) {
            TerminalUI.echoln("> Executor :docker: ${TerminalUI.colored("PULLING $image", TerminalUI.Color.CYAN)}")
            client.pullImageCmd(name).withTag(tag).exec(PullImageResultCallback()).awaitCompletion()
        }

        val oldPath = File(".").absolutePath.dropLast(2)

        val fixedCommand =  if (command.startsWith(oldPath)) "/project/${command.drop(oldPath.length + 1)}" else command
        val fixedArgs = args.map { if (it.startsWith(oldPath)) "/project/${it.drop(oldPath.length + 1)}" else it }

        val container = client.createContainerCmd(image)
            .withBinds(
                Bind.parse(File(".").absolutePath + ":" + "/project"),
            )
            .withWorkingDir("/project/${working.toRelativeString(File("."))}")
            .withCmd(fixedCommand, *fixedArgs.toTypedArray())
            .withAttachStderr(true)
            .withAttachStdout(true)
            .exec()

        client.startContainerCmd(container.id).exec()

        client.logContainerCmd(container.id)
            .withFollowStream(true)
            .withStdErr(true)
            .withStdOut(true)
            .exec(object : ResultCallback.Adapter<Frame>() {
                override fun onNext(obj: Frame) {
                    TerminalUI.echo(String(obj.payload))
                }
            })
            .awaitCompletion()

        val result = client.waitContainerCmd(container.id).exec(WaitContainerResultCallback()).awaitCompletion()
        client.removeContainerCmd(container.id).exec()
        return result.awaitStatusCode()
    }
}
