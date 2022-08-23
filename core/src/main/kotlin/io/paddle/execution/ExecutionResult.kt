package io.paddle.execution

class ExecutionResult(private val code: Int) {
    fun then(action: (Int) -> ExecutionResult): ExecutionResult =
        if (code == 0) action.invoke(code)
        else this

    fun orElse(action: (Int) -> ExecutionResult): ExecutionResult =
        if (code == 0) this
        else action.invoke(code)

    fun orElseDo(onFail: (Int) -> Unit) {
        if (code != 0) {
            onFail.invoke(code)
        }
    }

    fun <T> expose(onSuccess: (Int) -> T, onFail: (Int) -> T): T =
        if (code == 0) onSuccess.invoke(code)
        else onFail.invoke(code)
}