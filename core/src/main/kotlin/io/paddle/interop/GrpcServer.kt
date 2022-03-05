package io.paddle.interop

import io.grpc.*

class GrpcServer(private val port: Int, service: BindableService) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(service)
        .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@GrpcServer.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}
