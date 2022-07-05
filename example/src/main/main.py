import sys

if __name__ == '__main__':
    print(sys.path)
    print("Hello world!")


def bar(*args):
    pass


def example_pylint():
    bar(0, )
