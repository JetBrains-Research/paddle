package io.paddle.tasks

import io.paddle.project.PaddleProject

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

    fun resolve(id: String, project: PaddleProject): Task? {
        return tasks[id]
            ?: run {
                // try to extract a common task with @id for all subprojects
                val includedTasks = project.subprojects.map { it.tasks.resolve(id, it) }

                if (includedTasks.isEmpty() || includedTasks.any { it == null }) {
                    return null
                }

                return object : Task(project) {
                    override val id: String = id

                    override val group: String = includedTasks.first()!!.group
                    override val dependencies: List<Task>
                        get() = includedTasks as List<Task>

                    override fun act() {}
                }
            }
    }

    fun get(id: String): Task? {
        return tasks[id]
    }

    fun getOrFail(id: String): Task {
        return tasks[id] ?: error("Can't find task $id")
    }
}
