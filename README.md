# Paddle

Paddle is a young, extensible and IDE-friendly build system for Python.

It provides a declarative way to managing project dependencies, configuring execution
environment, running tasks and more.

### Why do I need to use Paddle?

- **Paddle is so easy to start with**.
  You only need a single YAML configuration file for your project,
  and the build system will do the rest.
  If you are familiar with some basic concepts of a build
  system like [Gradle](https://docs.gradle.org/current/userguide/what_is_gradle.html),
  and also have some experience of using various Python development tools
  (such as `venv`/`pytest`/`pylint`/`twine`) — you already know how to use Paddle!
- **Paddle supports Python**.
  It is not just another CLI tool to solve some limited scope of tasks which appear when you are
  developing in Python — Paddle is an ultimate decision to use for a Python project.
  It resolves and installs a needed version of the Python interpreter automatically,
  manages dependencies in the virtual environments,    
  provides a way to reliably run incremental tasks with scripts/tests/linters and more.
- **Paddle supports multi-project builds.**
  [Monorepos](https://en.wikipedia.org/wiki/Monorepo) gained a huge popularity
  in the industrial software development standards nowadays. With Paddle, it became possible
  to declare inter-project dependencies between packages and to configure complicated
  building&publishing pipelines for your Python monorepo.
- **Paddle is fully supported in the PyCharm IDE.**
  You can surely use an old-fashioned command line interface, or choose a preferred brand-new
  plugin for [PyCharm](https://www.jetbrains.com/pycharm/),
  a popular IDE for Python developed by [JetBrains](https://www.jetbrains.com/).
- **Paddle is an extensible general-purpose build system by its nature.**
  Yet it focuses on the Python projects support at first, it could also be easily customized to
  suit your own needs by writing and using various plugins.
  For instance, bundled `docker` and `ssh` plugins provide a way to run your tasks within
  other execution environments, such as
  [Docker containers](https://www.docker.com/resources/what-container/) and remote machines.



