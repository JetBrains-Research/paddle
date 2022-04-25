#!/usr/bin/env python3

import project_pb2 as project_api
import project_pb2_grpc as project_servicer
from src.paddle_api import MessageType, ExtendedAsyncPaddleProjectAPI


class AsyncPaddleApiClient:
    def __init__(self, grpc_channel) -> None:
        self.stub = project_servicer.ProjectStub(grpc_channel)

    async def print_message(self, project_id: str, message: str, type_name: str) -> None:
        await self.stub.PrintMessage(project_api.PrintRequest(
            projectId=project_id, message=message, type=project_api.PrintRequest.Type.Value(type_name))
        )


class AsyncPaddleProjectAPIImpl(ExtendedAsyncPaddleProjectAPI):
    def __init__(self, project_id: str, working_dir: str, grpc_client: AsyncPaddleApiClient):
        self.__project_id = project_id
        self.__working_dir = working_dir
        self.__paddle_client = grpc_client
        self.__paddle_config = None

    async def print_message(self, message: str, message_type: MessageType):
        await self.__paddle_client.print_message(self.__project_id, message, message_type.name)

    async def config_spec(self):
        pass

    def reload_config(self):
        pass
