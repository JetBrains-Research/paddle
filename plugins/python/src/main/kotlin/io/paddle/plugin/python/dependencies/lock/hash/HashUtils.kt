package io.paddle.plugin.python.dependencies.lock.hash

import io.paddle.plugin.python.dependencies.lock.hash.StringUtils.encodeHex
import java.io.*
import java.security.MessageDigest

/**
 * https://gist.github.com/LongClipeus/84db46e7d9714f67c4cbc40a67c8be1e
 */
object HashUtils {
    private const val STREAM_BUFFER_LENGTH = 1024

    fun getCheckSumFromFile(digest: MessageDigest, filePath: String): String {
        val file = File(filePath)
        return getCheckSumFromFile(digest, file)
    }

    fun getCheckSumFromFile(digest: MessageDigest, file: File): String {
        val fis = FileInputStream(file)
        val byteArray = updateDigest(digest, fis).digest()
        fis.close()
        val hexCode = encodeHex(byteArray, true)
        return String(hexCode)
    }

    fun getCheckSumFromString(digest: MessageDigest, src: String): String {
        val stream = src.byteInputStream()
        val byteArray = updateDigest(digest, stream).digest()
        stream.close()
        val hexCode = encodeHex(byteArray, true)
        return String(hexCode)
    }

    /**
     * Reads through an InputStream and updates the digest for the data
     *
     * @param digest The MessageDigest to use (e.g. MD5)
     * @param data Data to digest
     * @return the digest
     */
    private fun updateDigest(digest: MessageDigest, data: InputStream): MessageDigest {
        val buffer = ByteArray(STREAM_BUFFER_LENGTH)
        var read = data.read(buffer, 0, STREAM_BUFFER_LENGTH)
        while (read > -1) {
            digest.update(buffer, 0, read)
            read = data.read(buffer, 0, STREAM_BUFFER_LENGTH)
        }
        return digest
    }
}
