#!/usr/bin/env python3
import enum
from abc import ABC, abstractmethod
from typing import List


@enum.unique
class TaskDefaultGroups(enum.Enum):
    LINT = "lint"
    TEST = "test"
    BUILD = "build"
    APP = "application"


@enum.unique
class MessageType(enum.Enum):
    DEBUG = 0
    INFO = 1
    WARN = 2
    ERROR = 3
    OUT = 4
    ERR = 5


class AsyncPaddleProjectAPI(ABC):
    """
    Abstract class represents Paddle Project API with async methods.
    """

    @abstractmethod
    async def print_message(self, message: str, message_type: MessageType):
        pass


class ExtendedAsyncPaddleProjectAPI(AsyncPaddleProjectAPI):
    """
    Abstract class extends Paddle Project API with methods to extend project's configuration specification.
    """

    @abstractmethod
    async def config_spec(self):
        pass


class PaddleTask(ABC):
    """
    Abstract class represents Paddle Task.
    """

    @abstractmethod
    def __init__(self, project: AsyncPaddleProjectAPI, identifier: str, group: str, deps: List[str]) -> None:
        self.__project = project
        self.__id = identifier
        self.__group = group
        self.__deps = deps

    @property
    def identifier(self) -> str:
        """
        Identifier of the task in the project.
        Note that this identifier will be used to call the task from the terminal.

        :return: string represents the task identifier
        """
        return self.__id

    @property
    def group(self) -> str:
        """
        Tasks are grouped into categories by semantics. Use TaskDefaultGroups for standard groups for tasks.

        :return: string represents the task group
        """
        return self.__group

    @property
    def deps(self) -> List[str]:
        """
        Names of dependencies tasks that should be called before this task.

        :return: list of strings represent names of dependencies
        """
        return self.__deps

    @abstractmethod
    async def initialize(self) -> None:
        """
        Performs initial initialization during import of the whole Paddle project.
        Note that such methods for all tasks execute only in a sequential manner within Paddle Project.
        """
        pass

    @abstractmethod
    async def act(self) -> None:
        """
        Perform action which is the core essence of the task.
        Note that this method can be called as a coroutine within Paddle Project.
        """
        pass


class PaddlePlugin(ABC):
    """
    Abstract class represents Paddle Plugin.
    Note that methods are async in order to use async Paddle Project API calls.
    """

    @abstractmethod
    async def tasks(self, project: AsyncPaddleProjectAPI) -> List[PaddleTask]:
        """
        Returns list of plugin's tasks.
        Note that project API instance has no methods to change configuration of Paddle project. Use configure instead.
        :param project: project API
        :return: list of tasks
        """
        pass

    @abstractmethod
    async def configure(self, project: ExtendedAsyncPaddleProjectAPI) -> None:
        """
        Configures a project with the plugin via Paddle Project API.
        Note that project API instance extended by the methods to change configuration of Paddle project.
        :param project: project API
        """
        pass
