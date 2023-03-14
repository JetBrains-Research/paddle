import sys
from bugloc.app import Bar

if __name__ == '__main__':
    print(sys.path)
    print(sys.argv)
    for i in range(100000000):
        pass
    print("Hello world!")


def bar(*args):
    pass


def example_pylint():
    bar(0, )
