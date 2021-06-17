package io.paddle.terminal

@Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
object TerminalUI {
    private val RESET_COLOR = "\u001B[0m"

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

    fun echo(message: String) {
        print(message)
    }

    fun echoln(message: String) {
        echo(message)
        println()
    }

    fun colored(message: String, color: Color): String {
        return color.char + message + RESET_COLOR
    }
}
