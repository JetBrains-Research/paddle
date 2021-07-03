package io.paddle.project

import io.paddle.utils.config.Configuration
import java.io.File

class Locations(val workingFolder: File) {
    companion object {
        fun from(configuration: Configuration): Locations {
            return Locations(File("."))
        }
    }
}
