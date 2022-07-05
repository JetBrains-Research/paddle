package io.paddle.plugin

import io.paddle.project.PaddleProject
import io.paddle.tasks.Task

interface Plugin {
    val id: String
    fun configure(project: PaddleProject)
    fun tasks(project: PaddleProject): List<Task>
    fun extensions(project: PaddleProject): List<PaddleProject.Extension<Any>>
}
