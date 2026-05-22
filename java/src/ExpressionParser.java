package src;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionParser {

    public enum TokenType {
        NUMBER, FUNCTION, OPERATOR, CONSTANT, VARIABLE, BRACKET
    }

    public static class Token {
        public TokenType type;
        public String value;

        public Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("[%s: %s]", type, value);
        }
    }

    public static List<Token> tokenize(String expr) throws IllegalArgumentException {
        // Normalize arithmetic operators
        expr = expr.replace("×", "*").replace("÷", "/").replace("−", "-").replace("°", "");

        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int len = expr.length();

        while (i < len) {
            char c = expr.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Parse numbers (decimals included)
            if (Character.isDigit(c) || (c == '.' && i + 1 < len && Character.isDigit(expr.charAt(i + 1)))) {
                StringBuilder num = new StringBuilder();
                while (i < len && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    num.append(expr.charAt(i));
                    i++;
                }
                tokens.add(new Token(TokenType.NUMBER, num.toString()));
                continue;
            }

            // Parse functions and Ans
            String substring = expr.substring(i);
            Matcher funcMatcher = Pattern.compile("^(sin|cos|tan|asin|acos|atan|log|ln|cbrt|Ans)").matcher(substring);
            if (funcMatcher.find()) {
                String func = funcMatcher.group(0);
                tokens.add(new Token(TokenType.FUNCTION, func));
                i += func.length();
                continue;
            }

            if (substring.startsWith("³√")) {
                tokens.add(new Token(TokenType.FUNCTION, "cbrt"));
                i += 2;
                continue;
            }

            if (c == '√') {
                tokens.add(new Token(TokenType.FUNCTION, "sqrt"));
                i++;
                continue;
            }

            if (c == '²') {
                tokens.add(new Token(TokenType.OPERATOR, "^"));
                tokens.add(new Token(TokenType.NUMBER, "2"));
                i++;
                continue;
            }

            if (c == '³') {
                tokens.add(new Token(TokenType.OPERATOR, "^"));
                tokens.add(new Token(TokenType.NUMBER, "3"));
                i++;
                continue;
            }

            if (c == '⁻' && i + 1 < len && expr.charAt(i + 1) == '¹') {
                tokens.add(new Token(TokenType.OPERATOR, "^"));
                tokens.add(new Token(TokenType.NUMBER, "-1"));
                i += 2;
                continue;
            }

            if (c == 'π' || (substring.startsWith("pi"))) {
                tokens.add(new Token(TokenType.CONSTANT, "pi"));
                i += substring.startsWith("pi") ? 2 : 1;
                continue;
            }

            if (c == 'e') {
                tokens.add(new Token(TokenType.CONSTANT, "e"));
                i++;
                continue;
            }

            // Context-sensitive C check: OPERATOR or VARIABLE
            if (c == 'C') {
                boolean isOperator = false;
                if (!tokens.isEmpty()) {
                    Token prev = tokens.get(tokens.size() - 1);
                    if (prev.type == TokenType.NUMBER 
                            || prev.type == TokenType.CONSTANT 
                            || prev.type == TokenType.VARIABLE 
                            || prev.value.equals(")") 
                            || prev.value.equals("!") 
                            || prev.value.equals("Ans")) {
                        isOperator = true;
                    }
                }
                if (isOperator) {
                    tokens.add(new Token(TokenType.OPERATOR, "C"));
                } else {
                    tokens.add(new Token(TokenType.VARIABLE, "C"));
                }
                i++;
                continue;
            }

            // Variables (excluding C, which is handled above)
            if (c == 'A' || c == 'B' || c == 'D' || c == 'X' || c == 'Y' || c == 'M') {
                tokens.add(new Token(TokenType.VARIABLE, String.valueOf(c)));
                i++;
                continue;
            }

            // Brackets
            if (c == '(' || c == ')') {
                tokens.add(new Token(TokenType.BRACKET, String.valueOf(c)));
                i++;
                continue;
            }

            // Operators
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '%' || c == '!' || c == 'P' || c == 'C') {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(c)));
                i++;
                continue;
            }

            throw new IllegalArgumentException("Syntax ERROR");
        }

        return tokens;
    }

    public static List<Token> processImplicitMultiplication(List<Token> tokens) {
        List<Token> result = new ArrayList<>();
        int size = tokens.size();

        for (int i = 0; i < size; i++) {
            Token curr = tokens.get(i);
            result.add(curr);

            if (i < size - 1) {
                Token next = tokens.get(i + 1);

                boolean isCurrOperand = curr.type == TokenType.NUMBER 
                        || curr.type == TokenType.CONSTANT 
                        || curr.type == TokenType.VARIABLE 
                        || curr.value.equals(")") 
                        || curr.value.equals("!") 
                        || curr.value.equals("Ans");

                boolean isNextOperand = next.type == TokenType.NUMBER 
                        || next.type == TokenType.CONSTANT 
                        || next.type == TokenType.VARIABLE 
                        || next.type == TokenType.FUNCTION 
                        || next.value.equals("(") 
                        || next.value.equals("Ans");

                if (isCurrOperand && isNextOperand) {
                    result.add(new Token(TokenType.OPERATOR, "*"));
                }
            }
        }

        return result;
    }
}
