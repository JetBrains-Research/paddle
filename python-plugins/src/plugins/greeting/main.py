#!/usr/bin/env python3

import grpc
import src.container.project_pb2 as project_api
import src.container.project_pb2_grpc as project_servicer


class GreetingPlugin:
    def __init__(self):
        self.name = "greeting"
        self._tasks = {"greeting": GreetingTask()}

    def configure(self):
        pass

    def tasks(self):
        return self._tasks

    def task(self, name):
        return self.tasks()[name]

    def extensions(self):
        pass


class GreetingTask:
    def __init__(self):
        self.id = "greeting"
        self.group = "application"
        self.deps = []

    def initialize(self):
        pass

    def act(self):
        with grpc.insecure_channel('localhost:50051') as channel:
            stub = project_servicer.ProjectStub(channel)
            stub.PrintMessage(project_api.PrintRequest(projectId="1", message="Hello, world!", isErr=False, color="GREEN"))

    def run(self):
        pass


plugin = GreetingPlugin()
