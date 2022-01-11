package io.paddle.terminal

class Terminal(private val output: TextOutput) {
    val commands = CommandOutput(output)

    companion object {
        private const val RESET_COLOR = "\u001B[0m"

        fun decolor(message: String): String {
            var result = message
            for (color in Color.values().map { it.char } + RESET_COLOR) {
                result = result.replace(color, "")
            }
            return result
        }

        fun colored(message: String, color: Color): String {
            return color.char + message + RESET_COLOR
        }
    }

    enum class Color(val char: String) {
        BLACK("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),
        CYAN("\u001B[36m"),
        WHITE("\u001B[37m")
    }

    fun debug(message: String, newline: Boolean = true) {
        stdout("${colored("[DEBUG]", Color.WHITE)} $message", newline)
    }

    fun info(message: String, newline: Boolean = true) {
        stdout("${colored("[INFO]", Color.GREEN)} $message", newline)
    }

    fun warn(message: String, newline: Boolean = true) {
        stdout("${colored("[WARNING]", Color.YELLOW)} $message", newline)
    }

    fun error(message: String, newline: Boolean = true) {
        stderr("${colored("[ERROR]", Color.RED)} $message", newline)
    }

    fun stdout(message: String, newline: Boolean = true) {
        output.stdout(message)
        if (newline) output.stdout("\n")
    }

    fun stderr(message: String, newline: Boolean = true) {
        output.stderr(message)
        if (newline) output.stdout("\n")
    }
}
