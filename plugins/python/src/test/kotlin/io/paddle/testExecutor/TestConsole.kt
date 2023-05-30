package io.paddle.testExecutor

import io.paddle.terminal.TextOutput

class TestConsole : TextOutput {
    private var _stdout = StringBuilder()
    val stdout: String
        get() = _stdout.toString()
    private var _stderr = StringBuilder()
    val stderr: String
        get() = _stderr.toString()
    override fun stdout(text: String) {
        _stdout.append(text)
    }

    override fun stderr(text: String) {
        _stderr.append(text)
    }

    fun clear() {
        _stderr.clear()
        _stdout.clear()
    }
}
