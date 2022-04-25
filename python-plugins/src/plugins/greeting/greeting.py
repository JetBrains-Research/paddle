#!/usr/bin/env python3

from src.paddle_api import PaddleTask, TaskDefaultGroups, AsyncPaddleProjectAPI, PaddlePlugin


class GreetingTask(PaddleTask):
    def __init__(self):
        PaddleTask.__init__(self, name="greeting", group=TaskDefaultGroups.APP.value, deps=[])

    def initialize(self, project: AsyncPaddleProjectAPI):
        pass

    def act(self, project: AsyncPaddleProjectAPI):
        project.print_message(message="Hello, world!", is_err=False, color="GREEN")

    def run(self, project: AsyncPaddleProjectAPI):
        pass


class GreetingPlugin(PaddlePlugin):
    def __init__(self):
        tasks = [GreetingTask()]
        PaddlePlugin.__init__(self, name="greeting", tasks=tasks)

    def configure(self, project: AsyncPaddleProjectAPI):
        pass

    def extensions(self, project: AsyncPaddleProjectAPI):
        pass


plugin = GreetingPlugin()
