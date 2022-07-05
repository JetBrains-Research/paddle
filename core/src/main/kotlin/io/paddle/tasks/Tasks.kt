package io.paddle.tasks

class Tasks {
    private val tasks = HashMap<String, Task>()

    fun all(): Set<Task> {
        return tasks.values.toSet()
    }

    fun register(vararg given: Task) {
        for (task in given) {
            if (task.id in tasks) return
            tasks[task.id] = task
            task.initialize()
        }
    }

    fun get(id: String): Task? {
        return tasks[id]
    }

    fun getOrFail(id: String): Task {
        return tasks[id] ?: error("Can't find task $id")
    }
}
