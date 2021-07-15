package io.paddle.idea.runner.parser

import com.intellij.build.output.BuildOutputParser
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemOutputParserProvider
import io.paddle.idea.PaddleManager

class PaddleParserProvider: ExternalSystemOutputParserProvider {
    override fun getExternalSystemId(): ProjectSystemId  = PaddleManager.ID

    override fun getBuildOutputParsers(taskId: ExternalSystemTaskId): MutableList<BuildOutputParser> {
        return mutableListOf(PaddleBuildOutputParser())
    }
}
