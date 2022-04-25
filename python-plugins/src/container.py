#!/usr/bin/env python3
import importlib
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import List, Iterable

from grpc.aio import Channel

from src.client import AsyncPaddleApiClient, AsyncPaddleProjectAPIImpl
from src.paddle_api import PaddlePlugin, PaddleTask


@dataclass
class PyPackageInfo:
    name: str
    version: str


@dataclass
class PyModuleInfo:
    name: str
    repo_dir: str
    relative_path: str


@dataclass
class VersionedPyPlugin:
    plugin: PaddlePlugin
    version: str = None


class PaddlePluginsProvider:
    def __init__(self, plugins_site_packages_path: Path) -> None:
        self.__plugins_packages_path = plugins_site_packages_path
        self.__plugins = None
        sys.path.append(str(plugins_site_packages_path))

    def import_plugins_from_site_packages(self, packages_info: Iterable[PyPackageInfo]) -> None:
        updated_plugins = dict()
        if not self.__plugins:
            self.__plugins = updated_plugins
        for package in packages_info:
            versioned_plugin = self.__plugins.get(package.name, None)
            if not versioned_plugin or versioned_plugin.version != package.version:
                module = importlib.import_module(name="main", package=package.name)
                plugin = getattr(module, "plugin")
                if not plugin:
                    raise KeyError(f"cannot find plugin: {package.name}")
                versioned_plugin = VersionedPyPlugin(plugin, package.version)
            updated_plugins[package.name] = versioned_plugin
        self.__plugins = updated_plugins

    def import_plugins_from_module(self, modules_info: Iterable[PyModuleInfo]) -> None:
        for module in modules_info:
            if module.repo_dir not in sys.path:
                sys.path.append(module.repo_dir)
            mod = importlib.import_module(name=module.name, package=module.relative_path)
            plugin = getattr(mod, "plugin")
            if not plugin:
                raise KeyError(f"cannot find plugin: {module.name}")
            self.__plugins[module.name] = VersionedPyPlugin(plugin)

    def __getitem__(self, plugin_name: str) -> PaddlePlugin:
        return self.__plugins[plugin_name].plugin


class PaddleProjectContainer:
    def __init__(self, project_id: str, working_dir: str, plugins_dir: str, channel_to_paddle: Channel) -> None:
        self.__project = AsyncPaddleProjectAPIImpl(project_id, working_dir, AsyncPaddleApiClient(grpc_channel=channel_to_paddle))
        self.__plugins_provider = PaddlePluginsProvider(Path(working_dir).resolve().joinpath(plugins_dir))
        self.__tasks = dict()

    def import_package_plugins(self, plugins: Iterable[PyPackageInfo]) -> None:
        self.__plugins_provider.import_plugins_from_site_packages(plugins)

    def import_module_plugins(self, plugins: Iterable[PyModuleInfo]) -> None:
        self.__plugins_provider.import_plugins_from_module(plugins)

    async def configure_plugin(self, plugin_name: str) -> None:
        await self.__plugins_provider[plugin_name].configure(self.__project)

    async def plugin_tasks(self, plugin_name: str) -> List[PaddleTask]:
        tasks = await self.__plugins_provider[plugin_name].tasks(self.__project)
        for task in tasks:
            self.__tasks[task.identifier] = task
        return tasks

    def task(self, task_id: str) -> PaddleTask:
        return self.__tasks[task_id]

    def reload(self) -> None:
        self.__project.reload_config()
        self.__tasks = None
