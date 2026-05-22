import sys
sys.path.append('python')
from calculator import CasioCalculator
import tkinter as tk

class DummyRoot:
    def title(self, val): pass
    def geometry(self, val): pass
    def configure(self, bg): pass
    def resizable(self, x, y): pass
    def mainloop(self): pass

root = tk.Tk()
# Hide window
root.withdraw()

calc = CasioCalculator(root)

tests = [
    ("3+5", 8.0),
    ("3*5+2", 17.0),
    ("3*(5+2)", 21.0),
    ("³√(-8)", -2.0),
    ("³√(27)", 3.0),
    ("5²", 25.0),
    ("2³", 8.0),
    ("2^-1", 0.5),
    ("3!", 6.0),
    ("5C2", 10.0),
    ("5P2", 20.0),
    ("sin(30)", 0.5),
    ("cos(60)", 0.5),
    ("tan(45)", 1.0),
    ("asin(0.5)", 30.0),
    ("acos(0.5)", 60.0),
    ("atan(1)", 45.0),
    ("ln(e)", 1.0),
    ("log(100)", 2.0),
    ("Ans", 42.0),
    ("2π", 2 * 3.141592653589793),
]

passed = 0
total = 0

# Set initial states
calc.prev_ans = 42.0
calc.variables['A'] = 10.0
calc.variables['B'] = 2.0
calc.variables['M'] = 5.0

print("Running Python Parser tests...")

for expr, expected in tests:
    total += 1
    try:
        tokens = calc.tokenize(expr)
        processed = calc.process_implicit_multiplication(tokens)
        rpn = calc.shunting_yard(processed)
        res = calc.evaluate_rpn(rpn)
        if abs(res - expected) < 1e-9:
            passed += 1
            print(f"PASS: {expr} = {res}")
        else:
            print(f"FAIL: {expr} -> expected: {expected}, got: {res}")
    except Exception as e:
        print(f"FAIL: {expr} -> expected: {expected}, caught exception: {e}")

# Error cases
error_tests = [
    ("1/0", "Math ERROR"),
    ("√(-4)", "Math ERROR"),
    ("ln(-1)", "Math ERROR"),
    ("asin(2)", "Math ERROR"),
    ("5C6", "Math ERROR"),
    ("((3+5)", "Syntax ERROR"),
    ("3++5", "Syntax ERROR"),
]

for expr, expected_err in error_tests:
    total += 1
    try:
        tokens = calc.tokenize(expr)
        processed = calc.process_implicit_multiplication(tokens)
        rpn = calc.shunting_yard(processed)
        res = calc.evaluate_rpn(rpn)
        print(f"FAIL: {expr} -> expected error: {expected_err}, but completed successfully with {res}")
    except ValueError as e:
        if str(e) == expected_err:
            passed += 1
            print(f"PASS (Expected Error): {expr} -> {e}")
        else:
            print(f"FAIL: {expr} -> expected error: {expected_err}, got error: {e}")
    except Exception as e:
        print(f"FAIL: {expr} -> expected error: {expected_err}, got exception: {type(e).__name__}: {e}")

print(f"Python results: {passed}/{total} passed.")
sys.exit(0 if passed == total else 1)
