package io.paddle.terminal

class CommandOutput(private val output: TextOutput) {
    sealed class Command {
        abstract fun output(): String

        class Task(val id: String, val status: Status): Command() {
            companion object {
                private val regex = Regex("> Task :(.*?): (.*)")

                fun parse(line: String): Task? {
                    val match = regex.matchEntire(Terminal.decolor(line)) ?: return null
                    val (id, status) = match.groups.drop(1)
                    return Task(id!!.value, Status.values().single { it.display == status!!.value })
                }
            }

            enum class Status(val display: String, val color: Terminal.Color) {
                EXECUTE("EXECUTE", Terminal.Color.YELLOW),
                FAILED("FAILED", Terminal.Color.RED),
                UNKNOWN("UNKNOWN", Terminal.Color.RED),
                DONE("DONE", Terminal.Color.GREEN),
                UP_TO_DATE("UP-TO-DATE", Terminal.Color.GREEN)
            }

            override fun output(): String {
                return "> Task :${id}: ${Terminal.colored(status.display, status.color)}"
            }
        }
    }

    fun stdout(command: Command) {
        output.stdout(command.output() + "\n")
    }

    fun stderr(command: Command) {
        output.stderr(command.output() + "\n")
    }
}
