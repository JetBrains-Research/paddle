package io.paddle.terminal

class TerminalUI(val output: CommandOutput) {
    companion object {
        private const val RESET_COLOR = "\u001B[0m"
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

    fun stdout(message: String, newline: Boolean = true) {
        output.stdout(message)
        if (newline) output.stdout("\n")
    }

    fun stderr(message: String, newline: Boolean = true) {
        output.stderr(message)
        if (newline) output.stdout("\n")
    }


    fun colored(message: String, color: Color): String {
        return color.char + message + RESET_COLOR
    }
}
