package io.paddle.idea.execution.parser

import com.intellij.build.events.BuildEvent
import com.intellij.build.events.impl.*
import com.intellij.build.output.BuildOutputInstantReader
import com.intellij.build.output.BuildOutputParser
import io.paddle.terminal.CommandOutput
import io.paddle.terminal.Terminal
import java.util.function.Consumer

class PaddleBuildOutputParser: BuildOutputParser {
    override fun parse(line: String, reader: BuildOutputInstantReader, messageConsumer: Consumer<in BuildEvent>): Boolean {
        val raw = Terminal.decolor(line)

        if (!raw.isNextMessage()) return false

        val task = CommandOutput.Command.Task.parse(raw) ?: return false

        val event = when (task.status) {
            CommandOutput.Command.Task.Status.EXECUTE -> StartEventImpl(task.route, reader.parentEventId, System.currentTimeMillis(), raw)
            CommandOutput.Command.Task.Status.FAILED -> FinishEventImpl(task.route, reader.parentEventId, System.currentTimeMillis(), raw, FailureResultImpl())
            CommandOutput.Command.Task.Status.CANCELLED -> FinishEventImpl(task.route, reader.parentEventId, System.currentTimeMillis(), raw, FailureResultImpl())
            CommandOutput.Command.Task.Status.UNKNOWN -> FinishEventImpl(task.route, reader.parentEventId, System.currentTimeMillis(), raw, FailureResultImpl())
            CommandOutput.Command.Task.Status.DONE -> FinishEventImpl(task.route, reader.parentEventId, System.currentTimeMillis(), raw, SuccessResultImpl())
            CommandOutput.Command.Task.Status.UP_TO_DATE -> FinishEventImpl(task.route, reader.parentEventId, System.currentTimeMillis(), raw, SuccessResultImpl(true))
        }
        messageConsumer.accept(event)

        return false
    }

    private fun String.isNextMessage() = startsWith("> ")
}
