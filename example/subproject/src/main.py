import sys
from kernel import app

print(sys.path)

if __name__ == '__main__':
    print("Hello World")
    bar = app.Bar("name")
    print(bar.name)
