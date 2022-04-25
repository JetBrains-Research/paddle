package io.paddle

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.paddle.interop.*
import io.paddle.plugin.plugins
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.terminal.Terminal
import io.paddle.terminal.TextOutput
import java.io.File

class Paddle(private val project: Project) : CliktCommand() {
    val task by argument("task", "Use name of task")

    override fun run() {
        try {
            project.execute(task)
        } catch (e: Task.ActException) {
            return
        }
    }
}

fun main(args: Array<String>) {
    val file = File("paddle.yaml")
    if (!file.exists()) {
        Terminal(TextOutput.Console).stderr("Can't find paddle.yaml in root")
        return
    }

    /*
    Services execution sequence:
    1. Paddle Projects API gRPC Server.
    2. Paddle PyPS.
    3. Client to Paddle PyPS for Paddle.
     */

    val service = PaddleApiProviderService()
    val server = GrpcServer(50051, service)
    server.start()
    val paddleApiPort = server.port
    // TODO: start python plugins server with client to paddle Api port

    val port = 50052
    val channel: ManagedChannel = ManagedChannelBuilder
        .forAddress("localhost", port)
        .usePlaintext()
        .build()

    val project = Project.load(file, "/schema/paddle-schema.json", channel)
    project.register(project.plugins.enabled)
    service.register(project)

    Paddle(project).main(args)

    server.stop()
    channel.shutdownNow()
}
