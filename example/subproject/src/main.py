import sys
from dependency.src.kernel import app

from util import some_func

print(sys.path)

if __name__ == '__main__':
    bar = app.Bar("name")
    print(bar.name)
    print(some_func(1))

