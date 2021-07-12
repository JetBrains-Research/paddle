package io.paddle.terminal

interface TextOutput {
    fun output(text: String)

    object Console: TextOutput {
        override fun output(text: String) {
            print(text)
        }
    }
}
