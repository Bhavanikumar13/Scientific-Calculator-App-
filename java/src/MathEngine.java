package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MathEngine {

    private static final Map<String, Integer> PRECEDENCE = new HashMap<>();
    static {
        PRECEDENCE.put("+", 1);
        PRECEDENCE.put("-", 1);
        PRECEDENCE.put("*", 2);
        PRECEDENCE.put("/", 2);
        PRECEDENCE.put("%", 2);
        PRECEDENCE.put("U-", 3); // Unary minus
        PRECEDENCE.put("^", 4);
        PRECEDENCE.put("P", 4);
        PRECEDENCE.put("C", 4);
        PRECEDENCE.put("!", 5);
    }

    public static double evaluate(String exprStr, String angleMode, double prevAns, Map<String, Double> variables) throws Exception {
        List<ExpressionParser.Token> tokens = ExpressionParser.tokenize(exprStr);
        List<ExpressionParser.Token> processedTokens = ExpressionParser.processImplicitMultiplication(tokens);
        List<ExpressionParser.Token> rpn = shuntingYard(processedTokens);
        return evaluateRPN(rpn, angleMode, prevAns, variables);
    }

    public static List<ExpressionParser.Token> shuntingYard(List<ExpressionParser.Token> tokens) throws Exception {
        List<ExpressionParser.Token> output = new ArrayList<>();
        Stack<ExpressionParser.Token> operatorStack = new Stack<>();

        int size = tokens.size();
        for (int i = 0; i < size; i++) {
            ExpressionParser.Token token = tokens.get(i);

            if (token.type == ExpressionParser.TokenType.NUMBER) {
                output.add(token);
            } else if (token.type == ExpressionParser.TokenType.CONSTANT 
                    || token.type == ExpressionParser.TokenType.VARIABLE 
                    || token.value.equals("Ans")) {
                output.add(token);
            } else if (token.type == ExpressionParser.TokenType.FUNCTION) {
                operatorStack.push(token);
            } else if (token.type == ExpressionParser.TokenType.OPERATOR) {
                String opVal = token.value;

                // Unary minus detection
                if (opVal.equals("-")) {
                    boolean isUnary = false;
                    if (i == 0) {
                        isUnary = true;
                    } else {
                        ExpressionParser.Token prev = tokens.get(i - 1);
                        if (prev.type == ExpressionParser.TokenType.OPERATOR && !prev.value.equals("!")) {
                            isUnary = true;
                        } else if (prev.type == ExpressionParser.TokenType.BRACKET && prev.value.equals("(")) {
                            isUnary = true;
                        }
                    }
                    if (isUnary) {
                        operatorStack.push(new ExpressionParser.Token(ExpressionParser.TokenType.OPERATOR, "U-"));
                        continue;
                    }
                }

                while (!operatorStack.isEmpty() 
                        && !operatorStack.peek().value.equals("(") 
                        && (operatorStack.peek().type == ExpressionParser.TokenType.FUNCTION 
                        || PRECEDENCE.getOrDefault(operatorStack.peek().value, 0) > PRECEDENCE.getOrDefault(opVal, 0) 
                        || (PRECEDENCE.getOrDefault(operatorStack.peek().value, 0) == PRECEDENCE.getOrDefault(opVal, 0) && !opVal.equals("^")))) {
                    output.add(operatorStack.pop());
                }

                operatorStack.push(new ExpressionParser.Token(ExpressionParser.TokenType.OPERATOR, opVal));
            } else if (token.value.equals("(")) {
                operatorStack.push(token);
            } else if (token.value.equals(")")) {
                boolean lBracketFound = false;
                while (!operatorStack.isEmpty()) {
                    if (operatorStack.peek().value.equals("(")) {
                        operatorStack.pop();
                        lBracketFound = true;
                        break;
                    } else {
                        output.add(operatorStack.pop());
                    }
                }
                if (!lBracketFound) {
                    throw new IllegalArgumentException("Syntax ERROR");
                }
            }
        }

        while (!operatorStack.isEmpty()) {
            ExpressionParser.Token op = operatorStack.pop();
            if (op.value.equals("(")) {
                throw new IllegalArgumentException("Syntax ERROR");
            }
            output.add(op);
        }

        return output;
    }

    public static double evaluateRPN(List<ExpressionParser.Token> rpn, String angleMode, double prevAns, Map<String, Double> variables) throws Exception {
        Stack<Double> stack = new Stack<>();

        for (ExpressionParser.Token token : rpn) {
            if (token.type == ExpressionParser.TokenType.NUMBER) {
                stack.push(Double.parseDouble(token.value));
            } else if (token.type == ExpressionParser.TokenType.CONSTANT) {
                stack.push(token.value.equals("pi") ? Math.PI : Math.E);
            } else if (token.type == ExpressionParser.TokenType.VARIABLE) {
                stack.push(variables.getOrDefault(token.value, 0.0));
            } else if (token.value.equals("Ans")) {
                stack.push(prevAns);
            } else if (token.value.equals("U-")) {
                if (stack.size() < 1) throw new IllegalArgumentException("Syntax ERROR");
                stack.push(-stack.pop());
            } else if (token.value.equals("!")) {
                if (stack.size() < 1) throw new IllegalArgumentException("Syntax ERROR");
                double v = stack.pop();
                if (v < 0 || v != Math.floor(v)) throw new ArithmeticException("Math ERROR");
                stack.push((double) factorial((int) v));
            } else if (token.type == ExpressionParser.TokenType.OPERATOR) {
                if (stack.size() < 2) throw new IllegalArgumentException("Syntax ERROR");
                double b = stack.pop();
                double a = stack.pop();

                switch (token.value) {
                    case "+": stack.push(a + b); break;
                    case "-": stack.push(a - b); break;
                    case "*": stack.push(a * b); break;
                    case "/":
                        if (b == 0) throw new ArithmeticException("Math ERROR");
                        stack.push(a / b);
                        break;
                    case "%": stack.push(a % b); break;
                    case "^": stack.push(Math.pow(a, b)); break;
                    case "C": stack.push((double) combinations((int) a, (int) b)); break;
                    case "P": stack.push((double) permutations((int) a, (int) b)); break;
                }
            } else if (token.type == ExpressionParser.TokenType.FUNCTION) {
                if (stack.size() < 1) throw new IllegalArgumentException("Syntax ERROR");
                double val = stack.pop();
                double angle = val;

                // Trig angle configurations
                if ((token.value.equals("sin") || token.value.equals("cos") || token.value.equals("tan")) && angleMode.equals("DEG")) {
                    angle = Math.toRadians(val);
                }

                double res = 0.0;
                switch (token.value) {
                    case "sin": res = Math.sin(angle); break;
                    case "cos": res = Math.cos(angle); break;
                    case "tan": res = Math.tan(angle); break;
                    case "asin":
                        if (val < -1 || val > 1) throw new ArithmeticException("Math ERROR");
                        res = Math.asin(val);
                        if (angleMode.equals("DEG")) res = Math.toDegrees(res);
                        break;
                    case "acos":
                        if (val < -1 || val > 1) throw new ArithmeticException("Math ERROR");
                        res = Math.acos(val);
                        if (angleMode.equals("DEG")) res = Math.toDegrees(res);
                        break;
                    case "atan":
                        res = Math.atan(val);
                        if (angleMode.equals("DEG")) res = Math.toDegrees(res);
                        break;
                    case "log":
                        if (val <= 0) throw new ArithmeticException("Math ERROR");
                        res = Math.log10(val);
                        break;
                    case "ln":
                        if (val <= 0) throw new ArithmeticException("Math ERROR");
                        res = Math.log(val);
                        break;
                    case "sqrt":
                        if (val < 0) throw new ArithmeticException("Math ERROR");
                        res = Math.sqrt(val);
                        break;
                    case "cbrt":
                        res = Math.cbrt(val);
                        break;
                }
                stack.push(res);
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Syntax ERROR");
        }
        return stack.pop();
    }

    private static long factorial(int n) {
        if (n < 0) throw new ArithmeticException("Math ERROR");
        long res = 1;
        for (int i = 2; i <= n; i++) {
            res *= i;
        }
        return res;
    }

    private static long combinations(int n, int r) {
        if (n < 0 || r < 0 || r > n) throw new ArithmeticException("Math ERROR");
        return factorial(n) / (factorial(r) * factorial(n - r));
    }

    private static long permutations(int n, int r) {
        if (n < 0 || r < 0 || r > n) throw new ArithmeticException("Math ERROR");
        return factorial(n) / factorial(n - r);
    }
}
