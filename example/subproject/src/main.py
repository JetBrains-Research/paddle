import sys

from dependency.src.kernel import app

print(sys.path)

if __name__ == '__main__':
    bar = app.Bar("name")
    print(bar.name)

