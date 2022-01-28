#!/usr/bin/env python3
import click


def create_file():
    with open("test_file.txt", "w+") as out:
        out.write("Hello, world!\n")


_tasks = {'simple_task' : create_file}


@click.group()
def cli():
    pass


@cli.command()
def tasks():
    print(list(_tasks.keys()))


@cli.command()
@click.option("--task_id", default="simple_task")
def run_task(task_id: str):
    if task_id in _tasks:
        _tasks[task_id]()


if __name__ == '__main__':
    cli()
