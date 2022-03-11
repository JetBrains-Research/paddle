#!/usr/bin/env python3

from src.paddle_api import PaddleTask, TaskDefaultGroups, AbstractPaddleProject, PaddlePlugin


class GreetingTask(PaddleTask):
    def __init__(self):
        PaddleTask.__init__(self, name="greeting", group=TaskDefaultGroups.APP.value, deps=[])

    def initialize(self, project: AbstractPaddleProject):
        pass

    def act(self, project: AbstractPaddleProject):
        project.print_message(message="Hello, world!", is_err=False, color="GREEN")

    def run(self, project: AbstractPaddleProject):
        pass


class GreetingPlugin(PaddlePlugin):
    def __init__(self):
        tasks = [GreetingTask()]
        PaddlePlugin.__init__(self, name="greeting", tasks=tasks)

    def configure(self, project: AbstractPaddleProject):
        pass

    def extensions(self, project: AbstractPaddleProject):
        pass


plugin = GreetingPlugin()
