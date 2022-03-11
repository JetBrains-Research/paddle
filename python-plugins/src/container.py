#!/usr/bin/env python3
import importlib
import os
import sys
from typing import Dict, List

from src.client import PaddleClientConfiguration, GrpcStubPaddleProject, PaddleClient
from src.paddle_api import PaddlePlugin, PaddleTask


class PaddlePluginsProvider:
    def __init__(self, plugins_dir: str) -> None:
        self.__plugins_dir = plugins_dir
        self.__plugins = dict()

        # todo: replace dynamically modifying sys.path with using lib/site-package inside plugins dir
        for dir_name in os.listdir(plugins_dir):
            dir_path = os.path.join(plugins_dir, dir_name)
            if os.path.isdir(dir_path):
                sys.path.append(dir_path)

    @property
    def plugins(self) -> Dict[str, PaddlePlugin]:
        return self.__plugins

    def __getitem__(self, plugin_id: str) -> PaddlePlugin:
        return self.get(plugin_id)

    def get(self, plugin_id: str) -> PaddlePlugin:
        plugin = self.plugins.get(plugin_id, None)
        if not plugin:
            mod = importlib.import_module(plugin_id)
            plugin = getattr(mod, "plugin")
            self.plugins[plugin_id] = plugin

        if not plugin:
            raise KeyError(f"cannot find plugin: {plugin_id}")

        return plugin

    def invalidate_cache(self):
        self.__plugins = dict()


class PluginsContainer:
    def __init__(self, working_dir: str, config: PaddleClientConfiguration) -> None:
        self.__paddle_client = None
        self.__plugins_provider = None
        self.__projects = dict()
        self.__paddle_client_config = config
        self.__plugins_dir = f"{working_dir}/plugins"
        self.__started = False

    def __assert_started(self) -> None:
        if not self.__started:
            raise RuntimeError("plugins container not started yet")

    def get_tasks(self, plugin_id: str) -> List[PaddleTask]:
        self.__assert_started()
        plugin = self.__plugins_provider[plugin_id]
        return list(plugin.tasks.values())

    def execute_act(self, project_id: str, plugin_id: str, task_id: str) -> None:
        self.__assert_started()
        project = self.__projects.get(project_id, None)
        if not project:
            project = GrpcStubPaddleProject(project_id, self.__paddle_client)
            self.__projects[project_id] = project
        plugin = self.__plugins_provider[plugin_id]
        plugin.task(task_id).act(project=project)

    def start(self) -> None:
        if self.__started:
            raise RuntimeError("plugins container already started")
        self.__paddle_client = PaddleClient(configuration=self.__paddle_client_config)
        self.__plugins_provider = PaddlePluginsProvider(self.__plugins_dir)
        self.__started = True

    def stop(self) -> None:
        self.__assert_started()
        self.__paddle_client.stop()
        self.__plugins_provider.invalidate_cache()
        self.__started = False
