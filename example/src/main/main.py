import sys
import os
from bugloc.app import Bar

if __name__ == '__main__':
    print(sys.path)
    print(sys.argv)
    print(os.getenv('CUSTOM_ENV')) # You can find an example run configuration in .run folder
    for i in range(100000000):
        pass
    print("Hello world!")


def bar(*args):
    pass


def example_pylint():
    bar(0, )
