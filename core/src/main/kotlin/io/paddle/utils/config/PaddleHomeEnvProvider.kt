package io.paddle.utils.config

import io.paddle.utils.exists
import org.koin.dsl.module
import java.nio.file.Path
import kotlin.io.path.createDirectories


val paddleHomeEnvProvider = module {
    single<PaddleApplicationSettings.PaddleHomeProvider> { PaddleHomeEnvProvider() }
}
class PaddleHomeEnvProvider : PaddleApplicationSettings.PaddleHomeProvider {
    override fun getPath(): Path {
        val fromEnv = System.getenv("PADDLE_HOME")?.let { Path.of(it) }
        val result = fromEnv ?: System.getProperty("user.home").let { Path.of(it).resolve(".paddle") }
        if (!result.exists()) result.createDirectories()
        return result
    }
}
