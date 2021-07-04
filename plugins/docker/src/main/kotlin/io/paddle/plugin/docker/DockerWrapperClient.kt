package io.paddle.plugin.docker

import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.command.WaitContainerResultCallback
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import io.paddle.project.Project
import io.paddle.terminal.TerminalUI
import java.io.File

object DockerWrapperClient {
    private val config = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
    private val http = ApacheDockerHttpClient.Builder().dockerHost(config.dockerHost).sslConfig(config.sslConfig).build()
    private val client = DockerClientImpl.getInstance(config, http)

    fun isWrapped(): Boolean {
        return System.getenv("PADDLE_IN_DOCKER") == "TRUE"
    }

    fun startWrappedSession(image: String, project: Project, args: List<String>) {
        val (name, tag) = image.split(":")
        if (client.listImagesCmd().exec().all { image !in it.repoTags }) {
            TerminalUI.echoln("> Executor :docker: ${TerminalUI.colored("PULLING $image", TerminalUI.Color.CYAN)}")
            client.pullImageCmd(name).withTag(tag).exec(PullImageResultCallback()).awaitCompletion()
        }

        TerminalUI.echoln("> Executor :docker: ${TerminalUI.colored("RUNNING", TerminalUI.Color.CYAN)}")
        val container = client.createContainerCmd(image)
            .withBinds(
                Bind.parse(File(".").absolutePath + ":" + "/project"),
                Bind.parse(File("../build/app/install/app").absolutePath + ":" + "/paddle"),

            )
            .withEnv("PADDLE_IN_DOCKER=TRUE")
            .withWorkingDir("/project")
            .withCmd("/paddle/bin/app", *args.toTypedArray())
            .withAttachStderr(true)
            .withAttachStdout(true)
            .exec()

        client.startContainerCmd(container.id).exec()

        val logs = client.logContainerCmd(container.id)
            .withFollowStream(true)
            .withStdErr(true)
            .withStdOut(true)
            .exec(object : ResultCallback.Adapter<Frame>() {
                override fun onNext(obj: Frame) {
                    TerminalUI.echo(String(obj.payload))
                }
            })
            .awaitCompletion()

        client.waitContainerCmd(container.id).exec(WaitContainerResultCallback()).awaitCompletion()
    }
}
