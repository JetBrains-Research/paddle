# Parallel tasks execution

## Common observations

* Pip couldn't work in parallel ([Link](https://github.com/pypa/pip/issues/8187))
* The only tasks that are really needed to run in parallel is:
    * Resolving:
        * Repositories and requirements could be easily paralleled.
        * Interpreter need a bit of rework: IMO we need to do resolve in two phases:
            1. Resolve version and check compatibility
            2. Download and install needed versions.
        * The issue with interpreter task that if we have specified e.g. `3.10.1` and `3.10.11` we
          need to download every version. But we don't need to download the same version twice.
    * Install surely could be paralleled with `--no-cache-dir`, but with cache that is common for
      all pips (can we change it?) I'm not sure.
    * All other tasks couldn't be paralleled (like `run`) or can be with internal
      settings (`pytest`?)
* Common strategy to parallelize:
    * Since we have topological order of tasks, we can just distribute the task to worker.
        * Ideal scheduling is hard (need to look into some discrete math)
    * The only problem is how to determine the all modified files:
        * Like:
            * project-wide files like build folder, code folder (linter + run), etc.
            * Cache-wide folders: pip cache, build cache... (Do we care?)

## Parallel subprojects execution

### Pros

1. Easy to implement
2. Modifying files is not really a big problem, since we have only some cache problems
3. Lack of blocking

### Cons

1. No improvement in big plain projects
2. Blocking in common project see (`flat-repo` test in core)
    * Probably we can try to avoid these kind of tasks in first try.

### Implementation sketch

* Inside one project any task, that are not blocking could be taken. But not more than one task of
  project could be running simultaneously.
* So
    * We can choose one main thread (or coroutine) that orchestrate the execution process: for
      example updating list of projects, where task could be executed or manage the list of these
      task inside the project.
    * a callback thing: we can maintain counter of unresolved dependencies. When counter equals
      zero: fetch new task. 
