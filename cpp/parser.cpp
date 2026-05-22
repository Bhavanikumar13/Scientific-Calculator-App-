#include "parser.h"
#include <cmath>
#include <cctype>
#include <stack>

// Setup Precedence mapping
static int getPrecedence(const std::string& op) {
    if (op == "+" || op == "-") return 1;
    if (op == "*" || op == "/" || op == "%") return 2;
    if (op == "U-") return 3; // Unary Minus
    if (op == "^" || op == "P" || op == "C") return 4;
    if (op == "!") return 5;
    return 0;
}

static std::string replaceAll(std::string str, const std::string& from, const std::string& to) {
    size_t start_pos = 0;
    while((start_pos = str.find(from, start_pos)) != std::string::npos) {
        str.replace(start_pos, from.length(), to);
        start_pos += to.length();
    }
    return str;
}

std::vector<Token> ExpressionParser::tokenize(const std::string& expr) {
    std::string normalized = expr;
    normalized = replaceAll(normalized, "×", "*");
    normalized = replaceAll(normalized, "÷", "/");
    normalized = replaceAll(normalized, "−", "-");
    normalized = replaceAll(normalized, "°", "");

    std::vector<Token> tokens;
    size_t i = 0;
    size_t len = normalized.length();

    while (i < len) {
        char c = normalized[i];

        if (std::isspace(c)) {
            i++;
            continue;
        }

        // Parse numbers
        if (std::isdigit(c) || (c == '.' && i + 1 < len && std::isdigit(normalized[i + 1]))) {
            std::string num = "";
            while (i < len && (std::isdigit(normalized[i]) || normalized[i] == '.')) {
                num += normalized[i];
                i++;
            }
            tokens.push_back({ TokenType::NUMBER, num });
            continue;
        }

        // Helper check for string prefixes
        std::string sub = normalized.substr(i);
        auto startsWith = [&](const std::string& prefix) {
            return sub.rfind(prefix, 0) == 0;
        };

        // Multi-byte symbols and special operations
        if (startsWith("³√")) {
            tokens.push_back({ TokenType::FUNCTION, "cbrt" });
            i += std::string("³√").length();
            continue;
        }
        if (startsWith("√")) {
            tokens.push_back({ TokenType::FUNCTION, "sqrt" });
            i += std::string("√").length();
            continue;
        }
        if (startsWith("²")) {
            tokens.push_back({ TokenType::OPERATOR, "^" });
            tokens.push_back({ TokenType::NUMBER, "2" });
            i += std::string("²").length();
            continue;
        }
        if (startsWith("³")) {
            tokens.push_back({ TokenType::OPERATOR, "^" });
            tokens.push_back({ TokenType::NUMBER, "3" });
            i += std::string("³").length();
            continue;
        }
        if (startsWith("⁻¹")) {
            tokens.push_back({ TokenType::OPERATOR, "^" });
            tokens.push_back({ TokenType::NUMBER, "-1" });
            i += std::string("⁻¹").length();
            continue;
        }
        if (startsWith("π")) {
            tokens.push_back({ TokenType::CONSTANT, "pi" });
            i += std::string("π").length();
            continue;
        }

        // Functions
        std::string foundFunc = "";
        std::vector<std::string> funcs = { "asin", "acos", "atan", "sin", "cos", "tan", "log", "ln", "sqrt", "cbrt", "Ans" };
        for (const auto& f : funcs) {
            if (startsWith(f)) {
                foundFunc = f;
                break;
            }
        }

        if (!foundFunc.empty()) {
            tokens.push_back({ TokenType::FUNCTION, foundFunc });
            i += foundFunc.length();
            continue;
        }

        // Support standard constants
        if (startsWith("pi")) {
            tokens.push_back({ TokenType::CONSTANT, "pi" });
            i += 2;
            continue;
        }
        if (c == 'e') {
            tokens.push_back({ TokenType::CONSTANT, "e" });
            i++;
            continue;
        }

        // Context-sensitive C check: OPERATOR or VARIABLE
        if (c == 'C') {
            bool isOperator = false;
            if (!tokens.empty()) {
                Token prev = tokens.back();
                if (prev.type == TokenType::NUMBER 
                        || prev.type == TokenType::CONSTANT 
                        || prev.type == TokenType::VARIABLE 
                        || prev.value == ")" 
                        || prev.value == "!" 
                        || prev.value == "Ans") {
                    isOperator = true;
                }
            }
            if (isOperator) {
                tokens.push_back({ TokenType::OPERATOR, "C" });
            } else {
                tokens.push_back({ TokenType::VARIABLE, "C" });
            }
            i++;
            continue;
        }

        // Variables (excluding C, which is handled above)
        if (c == 'A' || c == 'B' || c == 'D' || c == 'X' || c == 'Y' || c == 'M') {
            tokens.push_back({ TokenType::VARIABLE, std::string(1, c) });
            i++;
            continue;
        }

        // Brackets
        if (c == '(' || c == ')') {
            tokens.push_back({ TokenType::BRACKET, std::string(1, c) });
            i++;
            continue;
        }

        // Operators
        if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '%' || c == '!' || c == 'P' || c == 'C') {
            tokens.push_back({ TokenType::OPERATOR, std::string(1, c) });
            i++;
            continue;
        }

        throw std::invalid_argument("Syntax ERROR");
    }

    return tokens;
}

std::vector<Token> ExpressionParser::insertImplicitMultiplication(const std::vector<Token>& tokens) {
    std::vector<Token> result;
    size_t size = tokens.size();

    for (size_t i = 0; i < size; i++) {
        Token curr = tokens[i];
        result.push_back(curr);

        if (i < size - 1) {
            Token next = tokens[i + 1];

            bool isCurrOperand = (curr.type == TokenType::NUMBER || 
                                  curr.type == TokenType::CONSTANT || 
                                  curr.type == TokenType::VARIABLE || 
                                  curr.value == ")" || 
                                  curr.value == "!" || 
                                  curr.value == "Ans");

            bool isNextOperand = (next.type == TokenType::NUMBER || 
                                  next.type == TokenType::CONSTANT || 
                                  next.type == TokenType::VARIABLE || 
                                  next.type == TokenType::FUNCTION || 
                                  next.value == "(" || 
                                  next.value == "Ans");

            if (isCurrOperand && isNextOperand) {
                result.push_back({ TokenType::OPERATOR, "*" });
            }
        }
    }

    return result;
}

double MathEngine::evaluate(const std::string& exprStr, const std::string& angleMode, double prevAns, const std::map<char, double>& variables) {
    std::vector<Token> tokens = ExpressionParser::tokenize(exprStr);
    std::vector<Token> processed = ExpressionParser::insertImplicitMultiplication(tokens);
    std::vector<Token> rpn = shuntingYard(processed);
    return evaluateRPN(rpn, angleMode, prevAns, variables);
}

std::vector<Token> MathEngine::shuntingYard(const std::vector<Token>& tokens) {
    std::vector<Token> output;
    std::stack<Token> operatorStack;

    size_t size = tokens.size();
    for (size_t i = 0; i < size; i++) {
        Token token = tokens[i];

        if (token.type == TokenType::NUMBER) {
            output.push_back(token);
        } else if (token.type == TokenType::CONSTANT || token.type == TokenType::VARIABLE || token.value == "Ans") {
            output.push_back(token);
        } else if (token.type == TokenType::FUNCTION) {
            operatorStack.push(token);
        } else if (token.type == TokenType::OPERATOR) {
            std::string opVal = token.value;

            // Unary Minus
            if (opVal == "-") {
                bool isUnary = false;
                if (i == 0) {
                    isUnary = true;
                } else {
                    Token prev = tokens[i - 1];
                    if (prev.type == TokenType::OPERATOR && prev.value != "!") {
                        isUnary = true;
                    } else if (prev.type == TokenType::BRACKET && prev.value == "(") {
                        isUnary = true;
                    }
                }
                if (isUnary) {
                    operatorStack.push({ TokenType::OPERATOR, "U-" });
                    continue;
                }
            }

            while (!operatorStack.empty() && 
                   operatorStack.top().value != "(" && 
                   (operatorStack.top().type == TokenType::FUNCTION || 
                    getPrecedence(operatorStack.top().value) > getPrecedence(opVal) || 
                    (getPrecedence(operatorStack.top().value) == getPrecedence(opVal) && opVal != "^"))) {
                output.push_back(operatorStack.top());
                operatorStack.pop();
            }

            operatorStack.push({ TokenType::OPERATOR, opVal });
        } else if (token.value == "(") {
            operatorStack.push(token);
        } else if (token.value == ")") {
            bool lBracketFound = false;
            while (!operatorStack.empty()) {
                if (operatorStack.top().value == "(") {
                    operatorStack.pop();
                    lBracketFound = true;
                    break;
                } else {
                    output.push_back(operatorStack.top());
                    operatorStack.pop();
                }
            }
            if (!lBracketFound) {
                throw std::invalid_argument("Syntax ERROR");
            }
        }
    }

    while (!operatorStack.empty()) {
        Token op = operatorStack.top();
        operatorStack.pop();
        if (op.value == "(") {
            throw std::invalid_argument("Syntax ERROR");
        }
        output.push_back(op);
    }

    return output;
}

double MathEngine::evaluateRPN(const std::vector<Token>& rpn, const std::string& angleMode, double prevAns, const std::map<char, double>& variables) {
    std::stack<double> stack;

    for (const auto& token : rpn) {
        if (token.type == TokenType::NUMBER) {
            stack.push(std::stod(token.value));
        } else if (token.type == TokenType::CONSTANT) {
            stack.push(token.value == "pi" ? 3.14159265358979323846 : 2.71828182845904523536);
        } else if (token.type == TokenType::VARIABLE) {
            char key = token.value[0];
            auto it = variables.find(key);
            stack.push(it != variables.end() ? it->second : 0.0);
        } else if (token.value == "Ans") {
            stack.push(prevAns);
        } else if (token.value == "U-") {
            if (stack.size() < 1) throw std::invalid_argument("Syntax ERROR");
            double val = stack.top();
            stack.pop();
            stack.push(-val);
        } else if (token.value == "!") {
            if (stack.size() < 1) throw std::invalid_argument("Syntax ERROR");
            double val = stack.top();
            stack.pop();
            if (val < 0 || val != std::floor(val)) throw std::runtime_error("Math ERROR");
            stack.push(static_cast<double>(factorial(static_cast<int>(val))));
        } else if (token.type == TokenType::OPERATOR) {
            if (stack.size() < 2) throw std::invalid_argument("Syntax ERROR");
            double b = stack.top(); stack.pop();
            double a = stack.top(); stack.pop();

            if (token.value == "+") stack.push(a + b);
            else if (token.value == "-") stack.push(a - b);
            else if (token.value == "*") stack.push(a * b);
            else if (token.value == "/") {
                if (b == 0) throw std::runtime_error("Math ERROR");
                stack.push(a / b);
            }
            else if (token.value == "%") stack.push(std::fmod(a, b));
            else if (token.value == "^") stack.push(std::pow(a, b));
            else if (token.value == "C") stack.push(static_cast<double>(combinations(static_cast<int>(a), static_cast<int>(b))));
            else if (token.value == "P") stack.push(static_cast<double>(permutations(static_cast<int>(a), static_cast<int>(b))));
        } else if (token.type == TokenType::FUNCTION) {
            if (stack.size() < 1) throw std::invalid_argument("Syntax ERROR");
            double val = stack.top(); stack.pop();
            double angle = val;

            if ((token.value == "sin" || token.value == "cos" || token.value == "tan") && angleMode == "DEG") {
                angle = val * 3.14159265358979323846 / 180.0;
            }

            double res = 0.0;
            if (token.value == "sin") res = std::sin(angle);
            else if (token.value == "cos") res = std::cos(angle);
            else if (token.value == "tan") res = std::tan(angle);
            else if (token.value == "asin") {
                if (val < -1.0 || val > 1.0) throw std::runtime_error("Math ERROR");
                res = std::asin(val);
                if (angleMode == "DEG") res = res * 180.0 / 3.14159265358979323846;
            }
            else if (token.value == "acos") {
                if (val < -1.0 || val > 1.0) throw std::runtime_error("Math ERROR");
                res = std::acos(val);
                if (angleMode == "DEG") res = res * 180.0 / 3.14159265358979323846;
            }
            else if (token.value == "atan") {
                res = std::atan(val);
                if (angleMode == "DEG") res = res * 180.0 / 3.14159265358979323846;
            }
            else if (token.value == "log") {
                if (val <= 0.0) throw std::runtime_error("Math ERROR");
                res = std::log10(val);
            }
            else if (token.value == "ln") {
                if (val <= 0.0) throw std::runtime_error("Math ERROR");
                res = std::log(val);
            }
            else if (token.value == "sqrt") {
                if (val < 0.0) throw std::runtime_error("Math ERROR");
                res = std::sqrt(val);
            }
            else if (token.value == "cbrt") {
                res = std::cbrt(val);
            }
            stack.push(res);
        }
    }

    if (stack.size() != 1) {
        throw std::invalid_argument("Syntax ERROR");
    }
    return stack.top();
}

long long MathEngine::factorial(int n) {
    if (n < 0) throw std::runtime_error("Math ERROR");
    long long res = 1;
    for (int i = 2; i <= n; i++) {
        res *= i;
    }
    return res;
}

long long MathEngine::combinations(int n, int r) {
    if (n < 0 || r < 0 || r > n) throw std::runtime_error("Math ERROR");
    return factorial(n) / (factorial(r) * factorial(n - r));
}

long long MathEngine::permutations(int n, int r) {
    if (n < 0 || r < 0 || r > n) throw std::runtime_error("Math ERROR");
    return factorial(n) / factorial(n - r);
}
