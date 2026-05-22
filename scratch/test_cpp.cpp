#include <iostream>
#include <string>
#include <map>
#include <vector>
#include <cmath>
#include "../cpp/parser.h"

int passed = 0;
int total = 0;

void test(const std::string& expr, const std::string& angleMode, double expected, double prevAns, const std::map<char, double>& vars) {
    total++;
    try {
        double res = MathEngine::evaluate(expr, angleMode, prevAns, vars);
        double diff = std::abs(res - expected);
        if (diff < 1e-9) {
            passed++;
            std::cout << "PASS: " << expr << " = " << res << std::endl;
        } else {
            std::cerr << "FAIL: " << expr << " -> expected: " << expected << ", got: " << res << std::endl;
        }
    } catch (const std::exception& e) {
        std::cerr << "FAIL: " << expr << " -> expected: " << expected << ", caught exception: " << e.what() << std::endl;
    }
}

void testError(const std::string& expr, const std::string& angleMode, const std::string& expectedErr, const std::map<char, double>& vars) {
    total++;
    try {
        double res = MathEngine::evaluate(expr, angleMode, 0.0, vars);
        std::cerr << "FAIL: " << expr << " -> expected error: " << expectedErr << ", but completed successfully with " << res << std::endl;
    } catch (const std::exception& e) {
        if (std::string(e.what()) == expectedErr) {
            passed++;
            std::cout << "PASS (Expected Error): " << expr << " -> " << e.what() << std::endl;
        } else {
            std::cerr << "FAIL: " << expr << " -> expected error: " << expectedErr << ", got error: " << e.what() << std::endl;
        }
    }
}

int main() {
    std::cout << "Running C++ Parser tests..." << std::endl;

    std::map<char, double> vars = {
        {'A', 10.0}, {'B', 2.0}, {'C', 0.0}, {'D', 0.0}, {'X', 0.0}, {'Y', 0.0}, {'M', 5.0}
    };

    test("3+5", "DEG", 8.0, 0.0, vars);
    test("3*5+2", "DEG", 17.0, 0.0, vars);
    test("3*(5+2)", "DEG", 21.0, 0.0, vars);
    test("³√(-8)", "DEG", -2.0, 0.0, vars);
    test("³√(27)", "DEG", 3.0, 0.0, vars);
    test("5²", "DEG", 25.0, 0.0, vars);
    test("2³", "DEG", 8.0, 0.0, vars);
    test("2^-1", "DEG", 0.5, 0.0, vars);
    test("3!", "DEG", 6.0, 0.0, vars);
    test("5C2", "DEG", 10.0, 0.0, vars);
    test("5P2", "DEG", 20.0, 0.0, vars);
    test("sin(30)", "DEG", 0.5, 0.0, vars);
    test("cos(60)", "DEG", 0.5, 0.0, vars);
    test("tan(45)", "DEG", 1.0, 0.0, vars);
    test("asin(0.5)", "DEG", 30.0, 0.0, vars);
    test("acos(0.5)", "DEG", 60.0, 0.0, vars);
    test("atan(1)", "DEG", 45.0, 0.0, vars);
    test("ln(e)", "DEG", 1.0, 0.0, vars);
    test("log(100)", "DEG", 2.0, 0.0, vars);
    test("Ans", "DEG", 42.0, 42.0, vars);
    test("2π", "RAD", 2 * 3.141592653589793, 0.0, vars);

    testError("1/0", "DEG", "Math ERROR", vars);
    testError("√(-4)", "DEG", "Math ERROR", vars);
    testError("ln(-1)", "DEG", "Math ERROR", vars);
    testError("asin(2)", "DEG", "Math ERROR", vars);
    testError("5C6", "DEG", "Math ERROR", vars);
    testError("((3+5)", "DEG", "Syntax ERROR", vars);
    testError("3++5", "DEG", "Syntax ERROR", vars);

    std::cout << "C++ results: " << passed << "/" << total << " passed." << std::endl;
    return passed == total ? 0 : 1;
}
