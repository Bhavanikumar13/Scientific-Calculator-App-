package src;

import java.util.HashMap;
import java.util.Map;

public class TestJava {
    private static int total = 0;
    private static int passed = 0;

    public static void main(String[] args) {
        System.out.println("Running Java Math Engine tests...");

        // Setup variables
        Map<String, Double> vars = new HashMap<>();
        vars.put("A", 10.0);
        vars.put("B", 2.0);
        vars.put("M", 5.0);

        // Test cases: expression, angleMode, expectedResult, expectedExceptionMessage
        test("3+5", "DEG", 8.0, vars);
        test("3*5+2", "DEG", 17.0, vars);
        test("3*(5+2)", "DEG", 21.0, vars);
        test("³√(-8)", "DEG", -2.0, vars);
        test("³√(27)", "DEG", 3.0, vars);
        test("5²", "DEG", 25.0, vars);
        test("2³", "DEG", 8.0, vars);
        test("2^-1", "DEG", 0.5, vars);
        test("3!", "DEG", 6.0, vars);
        test("5C2", "DEG", 10.0, vars);
        test("5P2", "DEG", 20.0, vars);
        test("sin(30)", "DEG", 0.5, vars);
        test("sin(pi/6)", "RAD", 0.5, vars);
        test("cos(60)", "DEG", 0.5, vars);
        test("tan(45)", "DEG", 1.0, vars);
        test("asin(0.5)", "DEG", 30.0, vars);
        test("acos(0.5)", "DEG", 60.0, vars);
        test("atan(1)", "DEG", 45.0, vars);
        test("ln(e)", "DEG", 1.0, vars);
        test("log(100)", "DEG", 2.0, vars);
        test("Ans", "DEG", 42.0, 42.0, vars); // prevAns is 42.0
        test("2pi", "RAD", 2 * Math.PI, vars); // Implicit multiplication check

        // Variables check
        test("A+B", "DEG", 12.0, vars);
        test("3M", "DEG", 15.0, vars); // Implicit multiplication with variable

        // Exception checks
        testError("1/0", "DEG", "Math ERROR", vars);
        testError("√(-4)", "DEG", "Math ERROR", vars);
        testError("ln(-1)", "DEG", "Math ERROR", vars);
        testError("asin(2)", "DEG", "Math ERROR", vars);
        testError("5C6", "DEG", "Math ERROR", vars);
        testError("((3+5)", "DEG", "Syntax ERROR", vars);
        testError("3++5", "DEG", "Syntax ERROR", vars);

        System.out.printf("Test results: %d/%d passed.%n", passed, total);
        if (passed < total) {
            System.exit(1);
        }
    }

    private static void test(String expr, String angleMode, double expected, Map<String, Double> vars) {
        test(expr, angleMode, expected, 0.0, vars);
    }

    private static void test(String expr, String angleMode, double expected, double prevAns, Map<String, Double> vars) {
        total++;
        try {
            double result = MathEngine.evaluate(expr, angleMode, prevAns, vars);
            double diff = Math.abs(result - expected);
            // Allow small float tolerance for trig
            if (diff < 1e-9) {
                passed++;
                System.out.println("PASS: " + expr + " = " + result);
            } else {
                System.err.println("FAIL: " + expr + " -> expected: " + expected + ", got: " + result);
            }
        } catch (Exception e) {
            System.err.println("FAIL: " + expr + " -> expected: " + expected + ", caught exception: " + e);
        }
    }

    private static void testError(String expr, String angleMode, String expectedMessage, Map<String, Double> vars) {
        total++;
        try {
            MathEngine.evaluate(expr, angleMode, 0.0, vars);
            System.err.println("FAIL: " + expr + " -> expected error: " + expectedMessage + ", but completed successfully");
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().equals(expectedMessage)) {
                passed++;
                System.out.println("PASS (Expected Error): " + expr + " -> " + e.getMessage());
            } else {
                System.err.println("FAIL: " + expr + " -> expected error: " + expectedMessage + ", got exception: " + e);
                e.printStackTrace();
            }
        }
    }
}
