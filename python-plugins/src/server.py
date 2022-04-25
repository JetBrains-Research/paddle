#!/usr/bin/env python3
import asyncio
import os
from dataclasses import dataclass
from threading import Lock

import click
import grpc
from grpc.aio import Channel

import plugins_pb2 as plugins_api
import plugins_pb2_grpc as grpc_servicer
from src.container import PaddleProjectContainer, PyPackageInfo, PyModuleInfo

_cleanup_coroutines = []


@dataclass
class PaddlePyPSConfig:
    working_dir: str
    channel_to_paddle: Channel


class PaddlePythonPluginsService(grpc_servicer.PluginsServicer):
    """
    Paddle Python Plugins Server (PaddlePyPS) implementation.
    """

    def __init__(self, config: PaddlePyPSConfig) -> None:
        self.__working_dir = config.working_dir
        self.__channel = config.channel_to_paddle
        self.__containers = dict()
        self.__containers_lock = Lock()

    # TODO: add exception handling
    async def InitializeProjectStub(self, request: plugins_api.InitializeProjectRequest,
                                    context: grpc.aio.ServicerContext) -> plugins_api.google_dot_protobuf_dot_empty__pb2.Empty:
        project_id = request.projectId
        working_dir = request.workingDir
        plugins_dir = request.pluginsSitePackages
        with self.__containers_lock:
            container = self.__containers.setdefault(project_id, PaddleProjectContainer(project_id, working_dir, plugins_dir, self.__channel))
        container.reload()
        return plugins_api.google_dot_protobuf_dot_empty__pb2.Empty()

    async def ImportPyModulePlugins(self, request: plugins_api.ImportPyModulePluginsRequest,
                                    context: grpc.aio.ServicerContext) -> plugins_api.google_dot_protobuf_dot_empty__pb2.Empty:
        project_id = request.projectId
        plugins = request.plugins
        with self.__containers_lock:
            container = self.__containers[project_id]
        container.import_module_plugins(map(lambda p: PyModuleInfo(name=p.pluginName, repo_dir=p.repoDir, relative_path=p.relativePath), plugins))

    async def ImportPyPackagePlugins(self, request: plugins_api.ImportPyPackagePluginsRequest,
                                     context: grpc.aio.ServicerContext) -> plugins_api.google_dot_protobuf_dot_empty__pb2.Empty:
        project_id = request.projectId
        plugins = request.plugins
        with self.__containers_lock:
            container = self.__containers[project_id]
        container.import_package_plugins(map(lambda p: PyPackageInfo(name=p.pluginName, version=p.pluginVersion), plugins))

    async def Configure(self, request: plugins_api.ProcessPluginRequest,
                        context: grpc.aio.ServicerContext) -> plugins_api.google_dot_protobuf_dot_empty__pb2.Empty:
        project_id = request.projectId
        plugin_name = request.pluginName
        with self.__containers_lock:
            container = self.__containers[project_id]
        await container.configure_plugin(plugin_name)
        return plugins_api.google_dot_protobuf_dot_empty__pb2.Empty()

    async def Tasks(self, request: plugins_api.ProcessPluginRequest, context: grpc.aio.ServicerContext) -> plugins_api.GetTasksResponse:
        project_id = request.projectId
        plugin_name = request.pluginName
        with self.__containers_lock:
            container = self.__containers[project_id]
        tasks = await container.plugin_tasks(plugin_name)
        tasks_info = list(map(lambda task: plugins_api.TaskInfo(id=task.identifier, group=task.group, depsIds=task.deps), tasks))
        return plugins_api.GetTasksResponse(taskInfoList=tasks_info)

    async def Initialize(self, request: plugins_api.ProcessTaskRequest,
                         context: grpc.aio.ServicerContext) -> plugins_api.google_dot_protobuf_dot_empty__pb2.Empty:
        project_id = request.projectId
        task_id = request.taskId
        with self.__containers_lock:
            container = self.__containers[project_id]
        task = container.task(task_id)
        await task.initialize()
        return plugins_api.google_dot_protobuf_dot_empty__pb2.Empty()

    async def Act(self, request: plugins_api.ProcessTaskRequest, context: grpc.aio.ServicerContext) -> plugins_api.google_dot_protobuf_dot_empty__pb2.Empty:
        project_id = request.projectId
        task_id = request.taskId
        with self.__containers_lock:
            container = self.__containers[project_id]
        task = container.task(task_id)
        await task.act()
        return plugins_api.google_dot_protobuf_dot_empty__pb2.Empty()


async def serve(*, working_dir: str, server_port: int, client_port: int) -> None:
    os.chdir(working_dir)
    channel_to_paddle = grpc.aio.insecure_channel(f"localhost:{client_port}")
    service = PaddlePythonPluginsService(PaddlePyPSConfig(working_dir=working_dir, channel_to_paddle=channel_to_paddle))
    aio_server = grpc.aio.server()
    grpc_servicer.add_PluginsServicer_to_server(service, aio_server)
    chosen_port = aio_server.add_insecure_port(f"[::]:{server_port}")

    await aio_server.start()
    print(chosen_port)

    async def service_graceful_shutdown():
        await aio_server.stop(grace=2)
        await channel_to_paddle.close(grace=2)

    _cleanup_coroutines.append(service_graceful_shutdown())
    await aio_server.wait_for_termination()


@click.command()
@click.option("--server_port", default=0, help="Port number to start PaddlePyPS")
@click.option("--client_port", default=50051, help="Port number for client connection to Paddle Project API Service")
@click.option("--working_dir", default=".", help="Working directory")
def main(server_port: int, client_port: int, working_dir: str) -> None:
    loop = asyncio.get_event_loop()
    try:
        loop.run_until_complete(serve(working_dir=working_dir, server_port=server_port, client_port=client_port))
    finally:
        loop.run_until_complete(*_cleanup_coroutines)
        loop.close()


if __name__ == '__main__':
    main()
