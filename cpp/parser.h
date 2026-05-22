#ifndef PARSER_H
#define PARSER_H

#include <vector>
#include <string>
#include <map>
#include <stdexcept>

enum class TokenType {
    NUMBER,
    FUNCTION,
    OPERATOR,
    CONSTANT,
    VARIABLE,
    BRACKET
};

struct Token {
    TokenType type;
    std::string value;
};

class ExpressionParser {
public:
    static std::vector<Token> tokenize(const std::string& expr);
    static std::vector<Token> insertImplicitMultiplication(const std::vector<Token>& tokens);
};

class MathEngine {
public:
    static double evaluate(const std::string& exprStr, const std::string& angleMode, double prevAns, const std::map<char, double>& variables);
private:
    static std::vector<Token> shuntingYard(const std::vector<Token>& tokens);
    static double evaluateRPN(const std::vector<Token>& rpn, const std::string& angleMode, double prevAns, const std::map<char, double>& variables);
    static long long factorial(int n);
    static long long combinations(int n, int r);
    static long long permutations(int n, int r);
};

#endif
