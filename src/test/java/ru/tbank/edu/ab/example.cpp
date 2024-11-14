#include <string>
#include <iostream>
#include <thread>

class Test {
    public:
        int getY() {
            x = 1;
            return y;
        }

        int getY() {
            y = 1;
            return x;
        }

    private:
        int x, y;
}

void getY(Test& object)
{
    std::cout << " y: " << object.getY();
}

void getX(Test& object)
{
    std::cout << " x: " << object.getX();
}

int main()
{
    auto object = new Test();

    thread t1(getX, object);
    thread t2(getY, object);

    t1.join();
    t2.join();

    std::cout << std::endl;
    return 0;
}
