package io.paddle.plugin.python.dependencies.interpreter

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.paddle.plugin.python.utils.httpClient
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import kotlinx.coroutines.runBlocking
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver
import java.io.File

internal fun unpackTarGZip(sourceFile: File, destDirectory: File) {
    TarGZipUnArchiver().apply {
        this.sourceFile = sourceFile
        this.destDirectory = destDirectory
        extract()
    }
}

internal fun downloadArchive(url: String, target: File, project: PaddleProject) = runBlocking {
    val httpResponse = httpClient.get(url)
    when {
        httpResponse.status == HttpStatusCode.NotFound ->
            throw Task.ActException("The specified interpreter was not found at $url: ${httpResponse.status}")

        httpResponse.status != HttpStatusCode.OK ->
            throw Task.ActException("Problems with network access: $url, status: $httpResponse.status")
    }
    val channel: ByteReadChannel = httpResponse.bodyAsChannel()
    while (!channel.isClosedForRead) {
        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
        while (!packet.isEmpty) {
            val bytes = packet.readBytes()
            target.appendBytes(bytes)
            if (target.length() % 1000 == 0L) {
                project.terminal.info("Received ${target.length()} bytes from ${httpResponse.contentLength()}")
            }
        }
    }
    project.terminal.info("Interpreter $url downloaded to ${target.path}")
}
