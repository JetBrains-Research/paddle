package io.paddle.plugin.python.dependencies.interpretator

import io.paddle.project.PaddleProject

internal class GenericInterpreterDownloader(userDefinedVersion: InterpreterVersion, project: PaddleProject) :
    AbstractInterpreterDownloader(userDefinedVersion, project)
