import sys
from kernel import app

if __name__ == '__main__':
    print("Hello World")
    print(sys.path)
    bar = app.Bar("name")
    print(bar.name)
