#!/usr/bin/env python3

from dataclasses import dataclass

import grpc

import project_pb2 as project_api
import project_pb2_grpc as project_servicer
from src.paddle_api import AbstractPaddleProject


@dataclass
class PaddleClientConfiguration:
    url: str = "localhost"
    port: int = 50051

    @property
    def address(self):
        return f"{self.url}:{self.port}"


class PaddleClient:
    def __init__(self, configuration: PaddleClientConfiguration) -> None:
        self.channel = grpc.insecure_channel(configuration.address)
        self.stub = project_servicer.ProjectStub(self.channel)

    def print_message(self, project_id: str, message: str, is_err: bool, color: str) -> None:
        self.stub.PrintMessage(project_api.PrintRequest(
            projectId=project_id, message=message, isErr=is_err, color=color)
        )

    def close(self) -> None:
        self.channel.stop()


class GrpcStubPaddleProject(AbstractPaddleProject):
    def __init__(self, project_id: str, grpc_client: PaddleClient):
        self.__project_id = project_id
        self.__paddle_client = grpc_client

    def print_message(self, message: str, is_err: bool, color: str):
        self.__paddle_client.print_message(self.__project_id, message, is_err, color)
