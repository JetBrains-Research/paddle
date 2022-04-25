package io.paddle.idea.utils

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.paddle.interop.GrpcServer
import io.paddle.interop.PaddleApiProviderService
import io.paddle.plugin.plugins
import io.paddle.project.Project
import io.paddle.specification.tree.SpecializedConfigSpec
import io.paddle.terminal.TextOutput
import io.paddle.utils.config.Configuration
import java.io.File

object PaddleProject {
    // TODO: remove to PaddleDaemon
    private val paddleApiService = PaddleApiProviderService()
    private val server = GrpcServer(0, paddleApiService).also { it.start() }

    // todo start PaddlePyPS

    private val channel: ManagedChannel = ManagedChannelBuilder
        .forAddress("localhost",50052)
        .usePlaintext()
        .build()

    init {
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                server.stop()
                channel.shutdownNow()
                // terminate paddle pyPS process
                println("*** server shut down")
            }
        )
    }

    var currentProject: Project? = null

    fun load(file: File, workDir: File, output: TextOutput = TextOutput.Console): Project {
        val config = Configuration.from(file)
        val configSpec = SpecializedConfigSpec.fromResource("/schema/paddle-schema.json")

        val project = Project(config, configSpec, workDir, output, channel)
        paddleApiService.register(project)

        project.register(project.plugins.enabled)
        return project.also { currentProject = it }
    }
}
