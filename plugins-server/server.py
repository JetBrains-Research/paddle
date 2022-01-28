#!/usr/bin/env python3

import ast
import socket
import subprocess

import google.protobuf.internal.decoder as decoder
import google.protobuf.internal.encoder as encoder

from model import message_pb2


def run_server(port=1234):
    serv_sock = create_serv_sock(port)
    while True:
        client_sock = accept_client_conn(serv_sock)
        serve_client(client_sock)


def serve_client(client_sock):
    request = read_request(client_sock)
    if request is None:
        print(f'Client disconnected')
    else:
        response = handle_request(request)
        write_response(client_sock, response)


def create_serv_sock(serv_port):
    serv_sock = socket.socket(socket.AF_INET,
                              socket.SOCK_STREAM,
                              proto=0)
    serv_sock.bind(('', serv_port))
    serv_sock.listen()
    return serv_sock


def accept_client_conn(serv_sock):
    client_sock, client_addr = serv_sock.accept()
    print(f'Client connected '
          f'{client_addr[0]}:{client_addr[1]}')
    return client_sock


def read_java_varint_delimited_stream(sock):
    buf = []
    data = sock.recv(1024)
    rCount = len(data)
    (size, position) = decoder._DecodeVarint(data, 0)

    buf.append(data)
    while rCount < size + position:
        data = sock.recv(size + position - rCount)
        rCount += len(data)
        buf.append(data)

    return b''.join(buf), size, position


def read_request(sock):
    data, size, position = read_java_varint_delimited_stream(sock)
    msg = message_pb2.BasePluginRepoRequest()
    msg.ParseFromString(data[position:position + size])
    return msg


def handle_request(request):
    field = request.WhichOneof("request")
    requests = {
        "pluginsRequest": handle_plugins,
        "tasksRequest": handle_tasks,
        "runTaskRequest": handle_run_task
    }
    response = requests[field](request)
    return response


def handle_plugins(request):
    plugins = ["simple-python-plugin"]
    response = message_pb2.GetPluginIdsResponse()
    response.pluginIds.extend(plugins)

    base_response = message_pb2.BasePluginRepoResponse()
    base_response.pluginsResponse.CopyFrom(response)
    return base_response


def handle_tasks(request):
    tasks = ast.literal_eval(run_plugin(["--tasks"]))
    response = message_pb2.GetTasksResponse()
    task = message_pb2.TaskInfo()
    task.id = "simple-task"
    task.group = "build"
    response.tasks.extend([task])
    base_response = message_pb2.BasePluginRepoResponse()
    base_response.tasksResponse.CopyFrom(response)
    return base_response


def handle_run_task(request):
    task_id = request.taskId
    run_plugin(["--task_id ", str(task_id)])
    response = message_pb2.RunTaskResponse()
    response.message = "success"
    base_response = message_pb2.BasePluginRepoResponse()
    base_response.runTaskResponse.CopyFrom(response)
    return base_response


def run_plugin(args):
    output = subprocess.run(["python3 plugins/simple-plugin ", *args], capture_output=True)
    return output.stdout.decode("utf-8")


def write_response(client_sock, response):
    out = response.SerializeToString()
    client_sock.send(encoder._VarintBytes(len(out)) + out)
    print(f'Client has been served')


if __name__ == '__main__':
    run_server()
