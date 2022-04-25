package io.paddle.interop

import io.grpc.*

class GrpcServer(port: Int, service: BindableService) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(service)
        .build()

    val port: Int
        get() = server.port

    fun start() {
        server.start()
        println("Server started, listening on $port")
    }

    fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}
