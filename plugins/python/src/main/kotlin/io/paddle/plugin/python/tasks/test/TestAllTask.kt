package io.paddle.plugin.python.tasks.test

import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.tasks.TaskDefaultGroups

class TestAllTask(project: PaddleProject) : Task(project) {
    override val id: String
        get() = "testAll"
    override val group: String
        get() = TaskDefaultGroups.TEST

    override val dependencies: List<Task>
        get() = PyTestTask.from(project)

    override fun act() = Unit
}
