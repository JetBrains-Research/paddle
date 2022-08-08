package io.paddle.tasks

import java.util.concurrent.atomic.AtomicBoolean

open class CancellationToken {
    companion object {
        val DEFAULT = object : CancellationToken() {
            override fun cancel() {
                return
            }
        }
    }

    private val _isCancelled = AtomicBoolean(false)

    open fun cancel() {
        _isCancelled.set(true)
    }

    val isCancelled: Boolean
        get() = _isCancelled.get()
}
