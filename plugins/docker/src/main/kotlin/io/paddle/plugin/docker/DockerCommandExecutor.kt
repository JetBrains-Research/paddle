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
import io.paddle.execution.ExecutionResult
import io.paddle.project.Project
import io.paddle.terminal.Terminal
import io.paddle.terminal.TextOutput
import io.paddle.utils.ext.Extendable
import java.io.File

class DockerCommandExecutor(private val image: String, output: TextOutput) : CommandExecutor(OutputConfiguration(output)) {
    object Extension : Project.Extension<DockerCommandExecutor> {
        override val key: Extendable.Key<DockerCommandExecutor> = Extendable.Key()

        override fun create(project: Project): DockerCommandExecutor {
            val image: String? = project.config.get("executor.image")
            return DockerCommandExecutor(image!!, project.output)
        }
    }

    private val config = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
    private val http = ApacheDockerHttpClient.Builder().dockerHost(config.dockerHost).sslConfig(config.sslConfig).build()
    private val client = DockerClientImpl.getInstance(config, http)

    override fun execute(
        command: String,
        args: Iterable<String>,
        workingDir: File,
        terminal: Terminal,
        envVars: Map<String, String>,
        log: Boolean
    ): ExecutionResult {
        val (name, tag) = image.split(":")

        if (client.listImagesCmd().exec().all { image !in it.repoTags }) {
            terminal.stdout("> Executor :docker: ${Terminal.colored("PULLING $image", Terminal.Color.CYAN)}")
            client.pullImageCmd(name).withTag(tag).exec(PullImageResultCallback()).awaitCompletion()
        }

        val oldPath = File(".").absolutePath.dropLast(2)

        //TODO-tanvd consider reworking this part and use standard File API instead of tricks like this one
        val fixedCommand = if (command.startsWith(oldPath)) "/project/${command.drop(oldPath.length + 1)}" else command
        val fixedArgs = args.map { if (it.startsWith(oldPath)) "/project/${it.drop(oldPath.length + 1)}" else it }

        val container = client.createContainerCmd(image)
            .withBinds(
                Bind.parse(File(".").absolutePath + ":" + "/project"),
            )
            .withWorkingDir("/project/${workingDir.toRelativeString(File("."))}")
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
                    terminal.stdout(String(obj.payload), newline = false)
                }
            })
            .awaitCompletion()

        val result = client.waitContainerCmd(container.id).exec(WaitContainerResultCallback()).awaitCompletion()
        client.removeContainerCmd(container.id).exec()
        return ExecutionResult(result.awaitStatusCode())
    }
}
