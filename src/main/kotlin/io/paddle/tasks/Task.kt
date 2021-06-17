package io.paddle.tasks

abstract class Task(val id: String, val dependencies: List<Task> = emptyList()) {
    abstract fun act()

    fun run() {
        for (dep in dependencies) {
            dep.run()
        }
        act()
    }
}