import tkinter as tk
from tkinter import messagebox
import math
import re

class CasioCalculator:
    def __init__(self, root):
        self.root = root
        self.root.title("Casio fx-991EX ClassWiz Desktop")
        self.root.geometry("450x650")
        self.root.configure(bg="#22252a")
        self.root.resizable(False, False)

        # Calculator States
        self.expression = ""
        self.cursor_pos = 0
        self.prev_ans = 0.0
        self.variables = {
            'A': 0.0, 'B': 0.0, 'C': 0.0, 'D': 0.0, 'X': 0.0, 'Y': 0.0, 'M': 0.0
        }
        self.is_shift = False
        self.is_alpha = False
        self.angle_mode = "DEG" # DEG or RAD
        self.history = []

        # Create Layout
        self.create_widgets()
        self.bind_keys()
        self.update_display()

    def create_widgets(self):
        # 1. Top Panel: Solar Accent and Brand
        top_frame = tk.Frame(self.root, bg="#22252a", height=50)
        top_frame.pack(fill="x", padx=15, pady=5)
        top_frame.pack_propagate(False)

        brand_lbl = tk.Label(top_frame, text="CASIO", font=("Arial", 16, "bold italic"), bg="#22252a", fg="#f3f4f6")
        brand_lbl.pack(side="left")

        model_lbl = tk.Label(top_frame, text="fx-991EX\nCLASSWIZ", font=("Arial", 8, "bold"), bg="#22252a", fg="#94a3b8", justify="left")
        model_lbl.pack(side="left", padx=10)

        # Solar Panel Casing
        solar_panel = tk.Frame(top_frame, bg="#4a2608", width=100, height=30, bd=2, relief="sunken")
        solar_panel.pack(side="right", pady=5)
        for _ in range(4):
            grid = tk.Frame(solar_panel, bg="#111", width=20, height=22)
            grid.pack(side="left", padx=1)

        # 2. Display LCD Panel
        display_frame = tk.Frame(self.root, bg="#1a1c1e", bd=5, relief="sunken")
        display_frame.pack(fill="x", padx=15, pady=5)

        # Status Bar inside LCD Display
        self.status_frame = tk.Frame(display_frame, bg="#a8c3a0", height=20)
        self.status_frame.pack(fill="x")
        self.status_frame.pack_propagate(False)

        self.lbl_shift = tk.Label(self.status_frame, text="S", font=("Courier", 8, "bold"), bg="#a8c3a0", fg="#a8c3a0")
        self.lbl_shift.pack(side="left", padx=5)

        self.lbl_alpha = tk.Label(self.status_frame, text="A", font=("Courier", 8, "bold"), bg="#a8c3a0", fg="#a8c3a0")
        self.lbl_alpha.pack(side="left", padx=5)

        self.lbl_mem = tk.Label(self.status_frame, text="M", font=("Courier", 8, "bold"), bg="#a8c3a0", fg="#a8c3a0")
        self.lbl_mem.pack(side="left", padx=5)

        self.lbl_angle = tk.Label(self.status_frame, text="DEG", font=("Courier", 8, "bold"), bg="#a8c3a0", fg="#1a2518")
        self.lbl_angle.pack(side="right", padx=10)

        # Text Screen
        self.expr_text = tk.Label(display_frame, text="", font=("Courier", 16), bg="#a8c3a0", fg="#1a2518", anchor="w", justify="left")
        self.expr_text.pack(fill="x", ipady=5, padx=10)

        self.result_text = tk.Label(display_frame, text="0", font=("Courier", 22, "bold"), bg="#a8c3a0", fg="#1a2518", anchor="e")
        self.result_text.pack(fill="x", ipady=5, padx=10)

        # Label helper for active setup
        lbl_natural = tk.Label(self.root, text="NATURAL-V.P.A.M.", font=("Arial", 6, "bold"), bg="#22252a", fg="#4a505e")
        lbl_natural.pack(pady=2)

        # 3. Keypad Matrix Frame
        keypad_frame = tk.Frame(self.root, bg="#22252a")
        keypad_frame.pack(fill="both", expand=True, padx=10, pady=10)

        # Configure button styles
        sci_btn_style = {"bg": "#3c4048", "fg": "#f3f4f6", "activebackground": "#4e525a", "activeforeground": "#fff", "font": ("Arial", 9, "bold"), "bd": 1, "relief": "raised"}
        digit_btn_style = {"bg": "#1e2124", "fg": "#f3f4f6", "activebackground": "#2c2f34", "activeforeground": "#fff", "font": ("Arial", 12, "bold"), "bd": 1, "relief": "raised"}
        action_btn_style = {"bg": "#db6a45", "fg": "#fff", "activebackground": "#e27b59", "activeforeground": "#fff", "font": ("Arial", 11, "bold"), "bd": 1, "relief": "raised"}
        equals_btn_style = {"bg": "#3b82f6", "fg": "#fff", "activebackground": "#4f90f7", "activeforeground": "#fff", "font": ("Arial", 12, "bold"), "bd": 1, "relief": "raised"}

        # Grid buttons definition: (text, row, col, style, value_passed)
        # Shift and Alpha functions printed above buttons can be mapped visually
        buttons = [
            # Row 0: Modifiers & Setup
            ("SHIFT", 0, 0, {"bg": "#3c4048", "fg": "#ffd700", "font": ("Arial", 8, "bold")}, "SHIFT"),
            ("ALPHA", 0, 1, {"bg": "#3c4048", "fg": "#ff4500", "font": ("Arial", 8, "bold")}, "ALPHA"),
            ("◀", 0, 2, sci_btn_style, "LEFT"),
            ("▶", 0, 3, sci_btn_style, "RIGHT"),
            ("DRG", 0, 4, sci_btn_style, "DRG"),
            # Row 1: Sci Block 1
            ("x⁻¹", 1, 0, sci_btn_style, "x^-1"),
            ("nCr", 1, 1, sci_btn_style, "nCr"),
            ("√", 1, 2, sci_btn_style, "sqrt"),
            ("x²", 1, 3, sci_btn_style, "x^2"),
            ("xʸ", 1, 4, sci_btn_style, "^"),
            # Row 2: Sci Block 2
            ("log", 2, 0, sci_btn_style, "log"),
            ("ln", 2, 1, sci_btn_style, "ln"),
            ("(-)", 2, 2, sci_btn_style, "(-)"),
            ("°′″", 2, 3, sci_btn_style, "dms"),
            ("sin", 2, 4, sci_btn_style, "sin"),
            # Row 3: Sci Block 3
            ("cos", 3, 0, sci_btn_style, "cos"),
            ("tan", 3, 1, sci_btn_style, "tan"),
            ("(", 3, 2, sci_btn_style, "("),
            (")", 3, 3, sci_btn_style, ")"),
            ("M+", 3, 4, sci_btn_style, "M+"),
            # Row 4: Digits 7,8,9, DEL, AC
            ("7", 4, 0, digit_btn_style, "7"),
            ("8", 4, 1, digit_btn_style, "8"),
            ("9", 4, 2, digit_btn_style, "9"),
            ("DEL", 4, 3, action_btn_style, "DEL"),
            ("AC", 4, 4, action_btn_style, "AC"),
            # Row 5: Digits 4,5,6, *, /
            ("4", 5, 0, digit_btn_style, "4"),
            ("5", 5, 1, digit_btn_style, "5"),
            ("6", 5, 2, digit_btn_style, "6"),
            ("×", 5, 3, digit_btn_style, "*"),
            ("÷", 5, 4, digit_btn_style, "/"),
            # Row 6: Digits 1,2,3, +, -
            ("1", 6, 0, digit_btn_style, "1"),
            ("2", 6, 1, digit_btn_style, "2"),
            ("3", 6, 2, digit_btn_style, "3"),
            ("+", 6, 3, digit_btn_style, "+"),
            ("−", 6, 4, digit_btn_style, "-"),
            # Row 7: 0, ., EXP, Ans, =
            ("0", 7, 0, digit_btn_style, "0"),
            (".", 7, 1, digit_btn_style, "."),
            ("×10ˣ", 7, 2, digit_btn_style, "*10^"),
            ("Ans", 7, 3, digit_btn_style, "Ans"),
            ("=", 7, 4, equals_btn_style, "=")
        ]

        # Config grid layout
        for col in range(5):
            keypad_frame.grid_columnconfigure(col, weight=1, pad=5)
        for row in range(8):
            keypad_frame.grid_rowconfigure(row, weight=1, pad=5)

        for text, row, col, style, val in buttons:
            btn = tk.Button(keypad_frame, text=text, command=lambda v=val: self.handle_btn(v), **style)
            btn.grid(row=row, column=col, sticky="nsew", padx=3, pady=3)

    def bind_keys(self):
        # Keyboard binds for calculation efficiency
        self.root.bind("<Key>", self.handle_keyboard)

    def handle_keyboard(self, event):
        char = event.char
        key = event.keysym
        
        if key == "Escape":
            self.handle_btn("AC")
        elif key == "Backspace":
            self.handle_btn("DEL")
        elif key in ["Return", "equal"]:
            self.handle_btn("=")
        elif key == "Left":
            self.handle_btn("LEFT")
        elif key == "Right":
            self.handle_btn("RIGHT")
        elif char in "0123456789.+-*/()":
            self.handle_btn(char)
        elif char == "s":
            self.handle_btn("sin")
        elif char == "c":
            self.handle_btn("cos")
        elif char == "t":
            self.handle_btn("tan")
        elif char == "r":
            self.handle_btn("sqrt")
        elif char == "l":
            self.handle_btn("ln")
        elif char == "g":
            self.handle_btn("log")

    def handle_btn(self, val):
        if val == "SHIFT":
            self.is_shift = not self.is_shift
            self.is_alpha = False
        elif val == "ALPHA":
            self.is_alpha = not self.is_alpha
            self.is_shift = False
        elif val == "LEFT":
            self.cursor_pos = max(0, self.cursor_pos - 1)
        elif val == "RIGHT":
            self.cursor_pos = min(len(self.expression), self.cursor_pos + 1)
        elif val == "DRG":
            self.angle_mode = "RAD" if self.angle_mode == "DEG" else "DEG"
            self.is_shift = False
            self.is_alpha = False
        elif val == "DEL":
            self.handle_del()
        elif val == "AC":
            self.expression = ""
            self.cursor_pos = 0
            self.result_text.config(text="0")
            self.is_shift = False
            self.is_alpha = False
        elif val == "=":
            self.evaluate_expression()
            self.is_shift = False
            self.is_alpha = False
        elif val == "M+":
            if self.is_shift:
                # M-
                try:
                    res_val = float(self.result_text.cget("text"))
                    self.variables['M'] -= res_val
                except ValueError:
                    pass
            elif self.is_alpha:
                self.insert_expr("M")
            else:
                # M+
                try:
                    res_val = float(self.result_text.cget("text"))
                    self.variables['M'] += res_val
                except ValueError:
                    pass
            self.is_shift = False
            self.is_alpha = False
        elif val == "sin":
            if self.is_shift: self.insert_expr("asin(")
            else: self.insert_expr("sin(")
            self.is_shift = False
        elif val == "cos":
            if self.is_shift: self.insert_expr("acos(")
            else: self.insert_expr("cos(")
            self.is_shift = False
        elif val == "tan":
            if self.is_shift: self.insert_expr("atan(")
            else: self.insert_expr("tan(")
            self.is_shift = False
        elif val == "sqrt":
            if self.is_shift:
                self.insert_expr("³√(")
            else:
                self.insert_expr("√(")
            self.is_shift = False
        elif val == "x^2":
            if self.is_shift:
                self.insert_expr("³")
            else:
                self.insert_expr("²")
            self.is_shift = False
        elif val == "^":
            self.insert_expr("^(")
        elif val == "x^-1":
            if self.is_shift: self.insert_expr("!")
            else: self.insert_expr("⁻¹")
            self.is_shift = False
        elif val == "nCr":
            if self.is_shift: self.insert_expr("P")
            else: self.insert_expr("C")
            self.is_shift = False
        elif val == "(-)":
            if self.is_alpha: self.insert_expr("A")
            else: self.insert_expr("-")
            self.is_alpha = False
        elif val == "dms":
            if self.is_alpha: self.insert_expr("B")
            else: self.insert_expr("°")
            self.is_alpha = False
        elif val == "*10^":
            if self.is_shift: self.insert_expr("π")
            elif self.is_alpha: self.insert_expr("e")
            else: self.insert_expr("*10^(")
            self.is_shift = False
            self.is_alpha = False
        else:
            # Check variables mappings inside numbers or variables
            if self.is_alpha and val in ["7", "8", "9", "4", "5", "6"]:
                mapping = {"7": "C", "8": "D", "9": "X", "4": "Y"}
                if val in mapping:
                    self.insert_expr(mapping[val])
                else:
                    self.insert_expr(val)
            else:
                self.insert_expr(val)
            self.is_shift = False
            self.is_alpha = False
            
        self.update_display()

    def insert_expr(self, text):
        self.expression = self.expression[:self.cursor_pos] + text + self.expression[self.cursor_pos:]
        self.cursor_pos += len(text)

    def handle_del(self):
        if self.cursor_pos > 0:
            size = 1
            check_str = self.expression[:self.cursor_pos]
            if check_str.endswith("sin(") or check_str.endswith("cos(") or check_str.endswith("tan("):
                size = 4
            elif check_str.endswith("asin(") or check_str.endswith("acos(") or check_str.endswith("atan("):
                size = 5
            elif check_str.endswith("log(") or check_str.endswith("Ans"):
                size = 4
            elif check_str.endswith("ln(") or check_str.endswith("√(") or check_str.endswith("³√("):
                size = 3
            elif check_str.endswith("*10^("):
                size = 5
            
            self.expression = self.expression[:self.cursor_pos - size] + self.expression[self.cursor_pos:]
            self.cursor_pos -= size

    def update_display(self):
        # Update S/A indicators colors
        self.lbl_shift.config(fg="#ffd700" if self.is_shift else "#a8c3a0")
        self.lbl_alpha.config(fg="#ff4500" if self.is_alpha else "#a8c3a0")
        self.lbl_mem.config(fg="#1a2518" if self.variables['M'] != 0.0 else "#a8c3a0")
        self.lbl_angle.config(text=self.angle_mode)

        # Cursor formatting
        if len(self.expression) == 0:
            self.expr_text.config(text="|")
        else:
            self.expr_text.config(text=self.expression[:self.cursor_pos] + "|" + self.expression[self.cursor_pos:])

    # ==========================================
    # Algebraic Parse Algorithm (Shunting-Yard)
    # ==========================================
    def tokenize(self, expr_str):
        # Replace display tags to standard tags
        normalized = expr_str.replace("×", "*").replace("÷", "/").replace("−", "-").replace("°", "")
        
        tokens = []
        i = 0
        while i < len(normalized):
            char = normalized[i]
            
            if char.isspace():
                i += 1
                continue
                
            # Numbers
            if char in "0123456789" or (char == '.' and i + 1 < len(normalized) and normalized[i+1] in "0123456789"):
                num_str = ""
                while i < len(normalized) and (normalized[i] in "0123456789" or normalized[i] == '.'):
                    num_str += normalized[i]
                    i += 1
                tokens.append(("NUMBER", float(num_str)))
                continue
                
            # Multi-character functions & constants
            match = re.match(r"^(sin|cos|tan|asin|acos|atan|log|ln|cbrt|Ans)", normalized[i:])
            if match:
                tokens.append(("FUNCTION", match.group(0)))
                i += len(match.group(0))
                continue
                
            if normalized[i:].startswith("³√"):
                tokens.append(("FUNCTION", "cbrt"))
                i += 2
                continue
                
            if char == "√":
                tokens.append(("FUNCTION", "sqrt"))
                i += 1
                continue
                
            if char == "²":
                tokens.append(("OPERATOR", "^"))
                tokens.append(("NUMBER", 2.0))
                i += 1
                continue

            if char == "³":
                tokens.append(("OPERATOR", "^"))
                tokens.append(("NUMBER", 3.0))
                i += 1
                continue
                
            if char == "⁻" and i + 1 < len(normalized) and normalized[i+1] == "¹":
                tokens.append(("OPERATOR", "^"))
                tokens.append(("NUMBER", -1.0))
                i += 2
                continue
                
            if char == "π":
                tokens.append(("CONSTANT", "pi"))
                i += 1
                continue
            if char == "e":
                tokens.append(("CONSTANT", "e"))
                i += 1
                continue
                
            # Context-sensitive C check: OPERATOR or VARIABLE
            if char == "C":
                is_operator = False
                if tokens:
                    prev = tokens[-1]
                    if prev[0] in ["NUMBER", "CONSTANT", "VARIABLE"] or prev[1] in [")", "!", "Ans"]:
                        is_operator = True
                if is_operator:
                    tokens.append(("OPERATOR", "C"))
                else:
                    tokens.append(("VARIABLE", "C"))
                i += 1
                continue

            # Variable tags (excluding C, which is handled above)
            if char in ["A", "B", "D", "X", "Y", "M"]:
                tokens.append(("VARIABLE", char))
                i += 1
                continue
                
            # Standard Operators & brackets
            if char in "+-*/^%!()PC":
                tokens.append(("BRACKET" if char in "()" else "OPERATOR", char))
                i += 1
                continue
                
            raise ValueError("Syntax ERROR")
        return tokens

    def process_implicit_multiplication(self, tokens):
        result = []
        for i in range(len(tokens)):
            curr = tokens[i]
            result.append(curr)
            
            if i < len(tokens) - 1:
                nxt = tokens[i+1]
                is_curr_operand = curr[0] in ["NUMBER", "CONSTANT", "VARIABLE"] or curr[1] in [")", "!", "Ans"]
                is_next_operand = nxt[0] in ["NUMBER", "CONSTANT", "VARIABLE", "FUNCTION"] or nxt[1] in ["(", "Ans"]
                
                if is_curr_operand and is_next_operand:
                    result.append(("OPERATOR", "*"))
        return result

    def shunting_yard(self, tokens):
        output = []
        operators = []
        precedence = {"+": 1, "-": 1, "*": 2, "/": 2, "%": 2, "U-": 3, "^": 4, "P": 4, "C": 4, "!": 5}
        
        for i, token in enumerate(tokens):
            t_type, t_val = token
            
            if t_type == "NUMBER":
                output.append(token)
            elif t_type in ["CONSTANT", "VARIABLE"] or t_val == "Ans":
                output.append(token)
            elif t_type == "FUNCTION":
                operators.append(token)
            elif t_type == "OPERATOR":
                # Unary Minus
                if t_val == "-":
                    is_unary = False
                    if i == 0:
                        is_unary = True
                    else:
                        prev = tokens[i-1]
                        if prev[0] == "OPERATOR" and prev[1] != "!":
                            is_unary = True
                        elif prev[0] == "BRACKET" and prev[1] == "(":
                            is_unary = True
                    if is_unary:
                        operators.append((t_type, "U-"))
                        continue
                
                while (operators and operators[-1][1] != "(" and 
                       (operators[-1][0] == "FUNCTION" or 
                        precedence.get(operators[-1][1], 0) > precedence.get(t_val, 0) or
                        (precedence.get(operators[-1][1], 0) == precedence.get(t_val, 0) and t_val != "^"))):
                    output.append(operators.pop())
                operators.append((t_type, t_val))
            elif t_val == "(":
                operators.append(token)
            elif t_val == ")":
                l_bracket = False
                while operators:
                    if operators[-1][1] == "(":
                        operators.pop()
                        l_bracket = True
                        break
                    else:
                        output.append(operators.pop())
                if not l_bracket:
                    raise ValueError("Syntax ERROR")
                    
        while operators:
            op = operators.pop()
            if op[1] == "(":
                raise ValueError("Syntax ERROR")
            output.append(op)
        return output

    def evaluate_rpn(self, rpn):
        stack = []
        for token in rpn:
            t_type, t_val = token
            
            if t_type == "NUMBER":
                stack.append(t_val)
            elif t_type == "CONSTANT":
                stack.append(math.pi if t_val == "pi" else math.e)
            elif t_type == "VARIABLE":
                stack.append(self.variables[t_val])
            elif t_val == "Ans":
                stack.append(self.prev_ans)
            elif t_val == "U-":
                if len(stack) < 1: raise ValueError("Syntax ERROR")
                stack.append(-stack.pop())
            elif t_val == "!":
                if len(stack) < 1: raise ValueError("Syntax ERROR")
                v = stack.pop()
                if v < 0 or not v.is_integer():
                    raise ValueError("Math ERROR")
                stack.append(float(math.factorial(int(v))))
            elif t_type == "OPERATOR":
                if len(stack) < 2: raise ValueError("Syntax ERROR")
                b = stack.pop()
                a = stack.pop()
                
                if t_val == "+": stack.append(a + b)
                elif t_val == "-": stack.append(a - b)
                elif t_val == "*": stack.append(a * b)
                elif t_val == "/":
                    if b == 0: raise ValueError("Math ERROR")
                    stack.append(a / b)
                elif t_val == "%": stack.append(a % b)
                elif t_val == "^": stack.append(math.pow(a, b))
                elif t_val == "C":
                    if a < 0 or b < 0 or b > a: raise ValueError("Math ERROR")
                    stack.append(float(math.comb(int(a), int(b))))
                elif t_val == "P":
                    if a < 0 or b < 0 or b > a: raise ValueError("Math ERROR")
                    stack.append(float(math.perm(int(a), int(b))))
            elif t_type == "FUNCTION":
                if len(stack) < 1: raise ValueError("Syntax ERROR")
                val = stack.pop()
                angle = val
                
                # Trigonometric conversions
                if t_val in ["sin", "cos", "tan"] and self.angle_mode == "DEG":
                    angle = math.radians(val)
                
                if t_val == "sin": stack.append(math.sin(angle))
                elif t_val == "cos": stack.append(math.cos(angle))
                elif t_val == "tan": stack.append(math.tan(angle))
                elif t_val == "asin": 
                    if val < -1 or val > 1: raise ValueError("Math ERROR")
                    res = math.asin(val)
                    stack.append(math.degrees(res) if self.angle_mode == "DEG" else res)
                elif t_val == "acos": 
                    if val < -1 or val > 1: raise ValueError("Math ERROR")
                    res = math.acos(val)
                    stack.append(math.degrees(res) if self.angle_mode == "DEG" else res)
                elif t_val == "atan": 
                    res = math.atan(val)
                    stack.append(math.degrees(res) if self.angle_mode == "DEG" else res)
                elif t_val == "log":
                    if val <= 0: raise ValueError("Math ERROR")
                    stack.append(math.log10(val))
                elif t_val == "ln":
                    if val <= 0: raise ValueError("Math ERROR")
                    stack.append(math.log(val))
                elif t_val == "sqrt":
                    if val < 0: raise ValueError("Math ERROR")
                    stack.append(math.sqrt(val))
                elif t_val == "cbrt":
                    stack.append(math.cbrt(val))
                    
        if len(stack) != 1:
            raise ValueError("Syntax ERROR")
        return stack[0]

    def evaluate_expression(self):
        if not self.expression:
            return
            
        try:
            tokens = self.tokenize(self.expression)
            processed = self.process_implicit_multiplication(tokens)
            rpn = self.shunting_yard(processed)
            result = self.evaluate_rpn(rpn)
            
            # Format and show results
            final_res = round(result, 10)
            if final_res.is_integer():
                final_res = int(final_res)
            self.result_text.config(text=str(final_res))
            self.prev_ans = float(final_res)
            
            # Save history
            self.history.append((self.expression, str(final_res)))
        except ValueError as ve:
            self.result_text.config(text=str(ve))
        except Exception:
            self.result_text.config(text="Syntax ERROR")

if __name__ == "__main__":
    root = tk.Tk()
    app = CasioCalculator(root)
    root.mainloop()
