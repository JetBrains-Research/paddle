import sys
from kernel import app

if __name__ == '__main__':
    for i in range(10000000):
        pass
    print("Hello")
    print(sys.path)
    bar = app.Bar("name")
    print(bar.name)
