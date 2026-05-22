#include <iostream>
#include <string>
#include <map>
#include <chrono>
#include "parser.h"

int main() {
    std::cout << "========================================================\n";
    std::cout << "      CASIO fx-991EX High-Performance Engine (C++)\n";
    std::cout << "========================================================\n";
    std::cout << "Commands:\n";
    std::cout << "  - Type expression to calculate (e.g. 5sin(pi/6) + 2^3)\n";
    std::cout << "  - 'deg' to switch to Degree mode\n";
    std::cout << "  - 'rad' to switch to Radian mode\n";
    std::cout << "  - 'vars' to list variables\n";
    std::cout << "  - 'set <var> = <val>' to store variable (e.g. set X = 4.5)\n";
    std::cout << "  - 'exit' or 'quit' to quit\n";
    std::cout << "========================================================\n";

    std::string angleMode = "DEG";
    double prevAns = 0.0;
    std::map<char, double> variables = {
        {'A', 0.0}, {'B', 0.0}, {'C', 0.0}, {'D', 0.0}, {'X', 0.0}, {'Y', 0.0}, {'M', 0.0}
    };

    std::string line;
    while (true) {
        std::cout << "Casio [" << angleMode << "]> ";
        if (!std::getline(std::cin, line)) {
            break;
        }

        // Trim input whitespace
        line.erase(0, line.find_first_not_of(" \t\r\n"));
        line.erase(line.find_last_not_of(" \t\r\n") + 1);

        if (line.empty()) continue;

        if (line == "exit" || line == "quit") {
            break;
        }

        if (line == "deg") {
            angleMode = "DEG";
            std::cout << "Angle mode set to Degrees.\n";
            continue;
        }

        if (line == "rad") {
            angleMode = "RAD";
            std::cout << "Angle mode set to Radians.\n";
            continue;
        }

        if (line == "vars") {
            std::cout << "Variables:\n";
            for (const auto& pair : variables) {
                std::cout << "  " << pair.first << " = " << pair.second << "\n";
            }
            std::cout << "  Ans = " << prevAns << "\n";
            continue;
        }

        // Store variable pattern: set X = 4.5
        if (line.rfind("set ", 0) == 0) {
            std::string sub = line.substr(4);
            size_t eqPos = sub.find('=');
            if (eqPos != std::string::npos) {
                std::string varStr = sub.substr(0, eqPos);
                std::string valStr = sub.substr(eqPos + 1);

                // Trim var name and value
                varStr.erase(0, varStr.find_first_not_of(" \t"));
                varStr.erase(varStr.find_last_not_of(" \t") + 1);
                valStr.erase(0, valStr.find_first_not_of(" \t"));
                valStr.erase(valStr.find_last_not_of(" \t") + 1);

                if (varStr.length() == 1 && std::isalpha(varStr[0])) {
                    char varChar = std::toupper(varStr[0]);
                    if (variables.find(varChar) != variables.end()) {
                        try {
                            double val = std::stod(valStr);
                            variables[varChar] = val;
                            std::cout << "Stored variable " << varChar << " = " << val << "\n";
                        } catch (...) {
                            std::cout << "Error: Invalid numeric value.\n";
                        }
                    } else {
                        std::cout << "Error: Allowed variable slots are A, B, C, D, X, Y, M.\n";
                    }
                } else {
                    std::cout << "Error: Variable name must be a single alphabet character.\n";
                }
            } else {
                std::cout << "Error: Invalid set syntax. Use 'set <var> = <val>'\n";
            }
            continue;
        }

        // Evaluate expression and time execution in microseconds
        try {
            auto start = std::chrono::high_resolution_clock::now();
            double result = MathEngine::evaluate(line, angleMode, prevAns, variables);
            auto end = std::chrono::high_resolution_clock::now();
            auto duration = std::chrono::duration_cast<std::chrono::microseconds>(end - start).count();

            std::cout << "Result: " << result << "\n";
            std::cout << "[Executed in " << duration << " microseconds]\n\n";
            prevAns = result;
        } catch (const std::exception& e) {
            std::cout << e.what() << "\n\n";
        }
    }

    std::cout << "Exiting Casio high-performance engine...\n";
    return 0;
}
