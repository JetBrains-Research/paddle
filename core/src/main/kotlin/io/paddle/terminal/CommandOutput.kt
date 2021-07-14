package io.paddle.terminal

interface CommandOutput {
    fun stdout(text: String)
    fun stderr(text: String)


    object Console : CommandOutput {
        override fun stderr(text: String) {
            System.err.print(text)
        }

        override fun stdout(text: String) {
            System.out.print(text)
        }
    }
}
