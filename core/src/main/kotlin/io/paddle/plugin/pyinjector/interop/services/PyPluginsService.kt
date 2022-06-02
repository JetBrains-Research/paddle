package io.paddle.plugin.pyinjector.interop.services

import io.grpc.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

class PyPluginsService private constructor(/*private val process: Process,*/ val channelTo: Channel) {

    companion object {
        private const val PYTHON_PLUGINS_SERVER_URL = ""

        @Volatile
        private var instance: PyPluginsService? = null

        fun getInstance(
            basePath: Path = Path.of("/home/sergey/IdeaProjects/paddle/python-plugins/"),
            paddleServicePort: Int,
            serverPort: Int
        ): PyPluginsService =
            instance ?: synchronized(this) {
                instance ?: createAndStart(
                    basePath,
                    basePath.resolve("venv/bin/python3"),
                    basePath.resolve("src/python_plugins_server/server.py"),
                    paddleServicePort,
                    serverPort
                ).also { instance = it }
            }

        private fun createAndStart(baseDir: Path, interpreterPath: Path, serverPath: Path, paddlePort: Int, serverPort: Int): PyPluginsService {
//            val pyPluginsServiceProcess = ProcessBuilder()
//                .directory(baseDir.toFile())
//                .command(
//                    interpreterPath.absolutePathString(), "-m", serverPath.absolutePathString(),
//                    "--server_port", serverPort.toString(),
//                    "--client_port", paddlePort.toString()
//                )
//                .start()

//            val processOutput = BufferedReader(InputStreamReader(pyPluginsServiceProcess.inputStream))
//            val port = processOutput.readLine().toInt()

            val channel: ManagedChannel = ManagedChannelBuilder
                .forAddress("localhost", serverPort)
                .usePlaintext()
                .build()

            return PyPluginsService(/*pyPluginsServiceProcess,*/ channel).apply {
                Runtime.getRuntime().addShutdownHook(
                    Thread {
                        println("*** shutting down PaddlePyPS server since JVM is shutting down")
                        channel.shutdown().awaitTermination(2, TimeUnit.SECONDS)
//                        this@apply.stop()
                        println("*** server shut down")
                    }
                )
            }
        }
    }

//    private fun stop() {
//        process.destroy()
//        with(process.waitFor()) {
//            if (this != 0) {
//                error("Failed to startup Paddle's plugins service. Exit with error code ")
//            }
//        }
//    }
}
