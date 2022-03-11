#!/usr/bin/env python3
import enum
from abc import ABC, abstractmethod
from typing import List, Dict


@enum.unique
class TaskDefaultGroups(enum.Enum):
    LINT = "lint"
    TEST = "test"
    BUILD = "build"
    APP = "application"


class PaddleTask(ABC):
    @abstractmethod
    def __init__(self, name: str, group: str, deps: List["PaddleTask"]) -> None:
        self.__name = name
        self.__group = group
        self.__deps = deps

    @property
    def name(self) -> str:
        return self.__name

    @property
    def group(self) -> str:
        return self.__group

    @property
    def deps(self) -> List["PaddleTask"]:
        return self.__deps

    @abstractmethod
    def initialize(self, project) -> None:
        pass

    @abstractmethod
    def act(self, project) -> None:
        pass

    @abstractmethod
    def run(self, project) -> None:
        pass


class PaddlePlugin(ABC):
    @abstractmethod
    def __init__(self, name: str, tasks: List[PaddleTask]) -> None:
        self.__name = name
        self.__tasks = self.__list_to_dict(tasks)

    @staticmethod
    def __list_to_dict(tasks: List[PaddleTask]) -> Dict[str, PaddleTask]:
        return {task.name: task for task in tasks}

    @property
    def tasks(self) -> Dict[str, PaddleTask]:
        return self.__tasks

    @abstractmethod
    def configure(self, project) -> None:
        pass

    def task(self, name) -> PaddleTask:
        task = self.tasks.get(name, None)
        if not task:
            raise KeyError(f"cannot find task: {name}")
        return task

    @abstractmethod
    def extensions(self, project):
        pass


class AbstractPaddleProject(ABC):
    @abstractmethod
    def print_message(self, message: str, is_err: bool, color: str):
        pass
