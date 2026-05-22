/**
 * Casio Scientific Calculator Logic
 */

// Application States
let expression = "";
let cursorPos = 0;
let previousAns = 0;
let variables = {
  'A': 0, 'B': 0, 'C': 0, 'D': 0, 'X': 0, 'Y': 0, 'M': 0
};
let isShiftActive = false;
let isAlphaActive = false;
let angleMode = "DEG"; // DEG or RAD

// Elements
const exprDisplay = document.getElementById("expression-display");
const resultDisplay = document.getElementById("result-display");
const cursorEl = document.getElementById("cursor");

// Status Indicators
const indShift = document.getElementById("indicator-shift");
const indAlpha = document.getElementById("indicator-alpha");
const indMemory = document.getElementById("indicator-memory");
const indAngle = document.getElementById("indicator-angle");

// Hide the absolute positioning cursor to use our inline blinking cursor
if (cursorEl) {
  cursorEl.style.display = "none";
}

// History Tracker
let history = JSON.parse(localStorage.getItem("casio_history")) || [];

// Initialize UI
updateDisplay();
renderHistory();

// Setup Event Listeners
document.querySelectorAll(".calc-btn, .d-btn").forEach(btn => {
  btn.addEventListener("click", (e) => {
    e.stopPropagation();
    const val = btn.getAttribute("data-value") || btn.innerText;
    handleButtonPress(val);
  });
});

// Theme Toggle
document.getElementById("theme-toggle").addEventListener("click", () => {
  document.body.classList.toggle("light-theme");
});

// History drawer controls
const historyDrawer = document.getElementById("history-drawer");
document.getElementById("history-toggle").addEventListener("click", () => {
  historyDrawer.classList.toggle("open");
});
document.getElementById("history-close").addEventListener("click", () => {
  historyDrawer.classList.remove("open");
});
document.getElementById("clear-history").addEventListener("click", () => {
  history = [];
  localStorage.setItem("casio_history", JSON.stringify(history));
  renderHistory();
});

// Capture keyboard shortcuts
document.addEventListener("keydown", (e) => {
  const key = e.key;
  if (key === "Escape") {
    handleButtonPress("AC");
  } else if (key === "Backspace") {
    handleButtonPress("DEL");
  } else if (key === "Enter" || key === "=") {
    handleButtonPress("=");
  } else if (key === "ArrowLeft") {
    handleButtonPress("LEFT");
  } else if (key === "ArrowRight") {
    handleButtonPress("RIGHT");
  } else if (key === "Shift") {
    handleButtonPress("SHIFT");
  } else if (key === "Alt") {
    handleButtonPress("ALPHA");
  } else if ("0123456789.+-*/()".includes(key)) {
    handleButtonPress(key);
  } else if (key === "s" || key === "S") {
    handleButtonPress("sin");
  } else if (key === "c" || key === "C") {
    handleButtonPress("cos");
  } else if (key === "t" || key === "T") {
    handleButtonPress("tan");
  } else if (key === "r" || key === "R") {
    handleButtonPress("sqrt");
  } else if (key === "l" || key === "L") {
    handleButtonPress("ln");
  } else if (key === "g" || key === "G") {
    handleButtonPress("log");
  }
});

function handleButtonPress(val) {
  if (val === "SHIFT") {
    isShiftActive = !isShiftActive;
    isAlphaActive = false;
  } else if (val === "ALPHA") {
    isAlphaActive = !isAlphaActive;
    isShiftActive = false;
  } else if (val === "LEFT" || val === "◀") {
    cursorPos = Math.max(0, cursorPos - 1);
  } else if (val === "RIGHT" || val === "▶") {
    cursorPos = Math.min(expression.length, cursorPos + 1);
  } else if (val === "UP" || val === "DOWN" || val === "▲" || val === "▼") {
    // Open/Close history drawer as vertical scrolling proxy
    historyDrawer.classList.toggle("open");
  } else if (val === "DRG") {
    angleMode = angleMode === "DEG" ? "RAD" : "DEG";
    isShiftActive = false;
    isAlphaActive = false;
  } else if (val === "DEL") {
    handleDel();
  } else if (val === "AC") {
    expression = "";
    cursorPos = 0;
    resultDisplay.innerText = "0";
    isShiftActive = false;
    isAlphaActive = false;
  } else if (val === "=") {
    evaluateExpression();
    isShiftActive = false;
    isAlphaActive = false;
  } else if (val === "M+") {
    if (isShiftActive) {
      // M-
      try {
        const valNum = parseFloat(resultDisplay.innerText);
        if (!isNaN(valNum)) {
          variables['M'] -= valNum;
        }
      } catch (e) {}
    } else if (isAlphaActive) {
      insertExpr("M");
    } else {
      // M+
      try {
        const valNum = parseFloat(resultDisplay.innerText);
        if (!isNaN(valNum)) {
          variables['M'] += valNum;
        }
      } catch (e) {}
    }
    isShiftActive = false;
    isAlphaActive = false;
  } else if (val === "sin") {
    if (isShiftActive) insertExpr("asin(");
    else if (isAlphaActive) insertExpr("D");
    else insertExpr("sin(");
    isShiftActive = false;
    isAlphaActive = false;
  } else if (val === "cos") {
    if (isShiftActive) insertExpr("acos(");
    else if (isAlphaActive) insertExpr("E");
    else insertExpr("cos(");
    isShiftActive = false;
    isAlphaActive = false;
  } else if (val === "tan") {
    if (isShiftActive) insertExpr("atan(");
    else if (isAlphaActive) insertExpr("F");
    else insertExpr("tan(");
    isShiftActive = false;
    isAlphaActive = false;
  } else if (val === "sqrt") {
    if (isShiftActive) insertExpr("³√(");
    else insertExpr("√(");
    isShiftActive = false;
  } else if (val === "x^2") {
    if (isShiftActive) insertExpr("³");
    else insertExpr("²");
    isShiftActive = false;
  } else if (val === "^") {
    insertExpr("^(");
    isShiftActive = false;
  } else if (val === "x^-1") {
    if (isShiftActive) insertExpr("!");
    else insertExpr("⁻¹");
    isShiftActive = false;
  } else if (val === "nCr") {
    if (isShiftActive) insertExpr("P");
    else insertExpr("C");
    isShiftActive = false;
  } else if (val === "(-)") {
    if (isAlphaActive) insertExpr("A");
    else insertExpr("-");
    isAlphaActive = false;
  } else if (val === "dms") {
    if (isAlphaActive) insertExpr("B");
    else insertExpr("°");
    isAlphaActive = false;
  } else if (val === "hyp") {
    if (isShiftActive) insertExpr("abs(");
    else if (isAlphaActive) insertExpr("C");
    isShiftActive = false;
    isAlphaActive = false;
  } else if (val === "*10^x" || val === "*10^") {
    if (isShiftActive) insertExpr("π");
    else if (isAlphaActive) insertExpr("e");
    else insertExpr("*10^(");
    isShiftActive = false;
    isAlphaActive = false;
  } else if (val === "Ans") {
    insertExpr("Ans");
    isShiftActive = false;
    isAlphaActive = false;
  } else {
    // Normal digit buttons and operator buttons
    // Check variable mapping for digits
    if (isAlphaActive && (val === "7" || val === "8" || val === "9" || val === "4")) {
      const mapping = { "7": "C", "8": "D", "9": "X", "4": "Y" };
      insertExpr(mapping[val]);
    } else {
      insertExpr(val);
    }
    isShiftActive = false;
    isAlphaActive = false;
  }

  updateDisplay();
}

function insertExpr(text) {
  expression = expression.slice(0, cursorPos) + text + expression.slice(cursorPos);
  cursorPos += text.length;
}

function handleDel() {
  if (cursorPos > 0) {
    let size = 1;
    const checkStr = expression.slice(0, cursorPos);
    
    if (checkStr.endsWith("asin(") || checkStr.endsWith("acos(") || checkStr.endsWith("atan(")) {
      size = 5;
    } else if (checkStr.endsWith("sin(") || checkStr.endsWith("cos(") || checkStr.endsWith("tan(") || checkStr.endsWith("cbrt(") || checkStr.endsWith("log(") || checkStr.endsWith("abs(")) {
      size = 4;
    } else if (checkStr.endsWith("³√(")) {
      size = 3;
    } else if (checkStr.endsWith("ln(") || checkStr.endsWith("√(") || checkStr.endsWith("Ans")) {
      size = 3;
    } else if (checkStr.endsWith("*10^(")) {
      size = 5;
    }
    
    expression = expression.slice(0, cursorPos - size) + expression.slice(cursorPos);
    cursorPos -= size;
  }
}

function updateDisplay() {
  // Update Shift/Alpha visual classes
  if (indShift) indShift.classList.toggle("active", isShiftActive);
  if (indAlpha) indAlpha.classList.toggle("active", isAlphaActive);
  if (indMemory) indMemory.classList.toggle("active", variables['M'] !== 0);
  if (indAngle) indAngle.innerText = angleMode;

  // Render text containing blink cursor character
  if (exprDisplay) {
    if (expression.length === 0) {
      exprDisplay.innerHTML = `<span class="cursor-line" style="position:static;animation:blink 1s step-end infinite;">|</span>`;
    } else {
      const before = escapeHtml(expression.slice(0, cursorPos));
      const after = escapeHtml(expression.slice(cursorPos));
      exprDisplay.innerHTML = before + `<span class="cursor-line" style="position:static;animation:blink 1s step-end infinite;">|</span>` + after;
    }
  }
}

function escapeHtml(text) {
  return text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}

function renderHistory() {
  const listEl = document.getElementById("history-list");
  if (!listEl) return;

  if (history.length === 0) {
    listEl.innerHTML = `<p class="empty-history-msg">No calculations saved yet.</p>`;
    return;
  }

  listEl.innerHTML = history.map((item, idx) => `
    <div class="history-item" onclick="loadHistoryItem(${idx})">
      <div class="history-expr">${escapeHtml(item.expr)}</div>
      <div class="history-res">${escapeHtml(item.result)}</div>
    </div>
  `).join('');
}

window.loadHistoryItem = function(idx) {
  if (history[idx]) {
    expression = history[idx].expr;
    cursorPos = expression.length;
    resultDisplay.innerText = history[idx].result;
    updateDisplay();
  }
};

// ==========================================
// Shunting-Yard Algebraic Evaluator Engine
// ==========================================

function tokenize(exprStr) {
  // Normalize display characters
  let normalized = exprStr
    .replace(/×/g, "*")
    .replace(/÷/g, "/")
    .replace(/−/g, "-")
    .replace(/°/g, "");

  let tokens = [];
  let i = 0;
  let len = normalized.length;

  while (i < len) {
    let char = normalized[i];

    if (/\s/.test(char)) {
      i++;
      continue;
    }

    // Numbers
    if (/[0-9]/.test(char) || (char === '.' && i + 1 < len && /[0-9]/.test(normalized[i+1]))) {
      let numStr = "";
      while (i < len && /[0-9.]/.test(normalized[i])) {
        numStr += normalized[i];
        i++;
      }
      tokens.push({ type: "NUMBER", value: parseFloat(numStr) });
      continue;
    }

    // Multi-character functions & constants
    let sub = normalized.slice(i);
    let funcMatch = sub.match(/^(asin|acos|atan|sin|cos|tan|log|ln|Ans)/);
    if (funcMatch) {
      tokens.push({ type: "FUNCTION", value: funcMatch[0] });
      i += funcMatch[0].length;
      continue;
    }

    // Special Unicode symbols
    if (sub.startsWith("³√")) {
      tokens.push({ type: "FUNCTION", value: "cbrt" });
      i += 2;
      continue;
    }

    if (char === "√") {
      tokens.push({ type: "FUNCTION", value: "sqrt" });
      i++;
      continue;
    }

    if (char === "²") {
      tokens.push({ type: "OPERATOR", value: "^" });
      tokens.push({ type: "NUMBER", value: 2 });
      i++;
      continue;
    }

    if (char === "³") {
      tokens.push({ type: "OPERATOR", value: "^" });
      tokens.push({ type: "NUMBER", value: 3 });
      i++;
      continue;
    }

    if (char === "⁻" && normalized[i+1] === "¹") {
      tokens.push({ type: "OPERATOR", value: "^" });
      tokens.push({ type: "NUMBER", value: -1 });
      i += 2;
      continue;
    }

    if (char === "π") {
      tokens.push({ type: "CONSTANT", value: "pi" });
      i++;
      continue;
    }

    if (char === "e") {
      tokens.push({ type: "CONSTANT", value: "e" });
      i++;
      continue;
    }

    // Context-sensitive C check: OPERATOR or VARIABLE
    if (char === "C") {
      let isOperator = false;
      if (tokens.length > 0) {
        let prev = tokens[tokens.length - 1];
        if (["NUMBER", "CONSTANT", "VARIABLE"].includes(prev.type) || [")", "!", "Ans"].includes(prev.value)) {
          isOperator = true;
        }
      }
      if (isOperator) {
        tokens.push({ type: "OPERATOR", value: "C" });
      } else {
        tokens.push({ type: "VARIABLE", value: "C" });
      }
      i++;
      continue;
    }

    // Variables (excluding C, since it's handled above)
    if ("ABDXYM".includes(char)) {
      tokens.push({ type: "VARIABLE", value: char });
      i++;
      continue;
    }

    // Operators and brackets (excluding C, since it's handled above)
    if ("+-*/^%!()P".includes(char)) {
      tokens.push({ type: char === "(" || char === ")" ? "BRACKET" : "OPERATOR", value: char });
      i++;
      continue;
    }

    throw new Error("Syntax ERROR");
  }

  return tokens;
}

function processImplicitMultiplication(tokens) {
  let result = [];
  for (let i = 0; i < tokens.length; i++) {
    let curr = tokens[i];
    result.push(curr);

    if (i < tokens.length - 1) {
      let next = tokens[i+1];
      let isCurrOperand = ["NUMBER", "CONSTANT", "VARIABLE"].includes(curr.type) || [")", "!", "Ans"].includes(curr.value);
      let isNextOperand = ["NUMBER", "CONSTANT", "VARIABLE", "FUNCTION"].includes(next.type) || ["(", "Ans"].includes(next.value);

      if (isCurrOperand && isNextOperand) {
        result.push({ type: "OPERATOR", value: "*" });
      }
    }
  }
  return result;
}

function shuntingYard(tokens) {
  let output = [];
  let operators = [];
  let precedence = { "+": 1, "-": 1, "*": 2, "/": 2, "%": 2, "U-": 3, "^": 4, "P": 4, "C": 4, "!": 5 };

  for (let i = 0; i < tokens.length; i++) {
    let token = tokens[i];

    if (token.type === "NUMBER") {
      output.push(token);
    } else if (["CONSTANT", "VARIABLE"].includes(token.type) || token.value === "Ans") {
      output.push(token);
    } else if (token.type === "FUNCTION") {
      operators.push(token);
    } else if (token.type === "OPERATOR") {
      let opVal = token.value;

      // Unary Minus Detection
      if (opVal === "-") {
        let isUnary = false;
        if (i === 0) {
          isUnary = true;
        } else {
          let prev = tokens[i-1];
          if (prev.type === "OPERATOR" && prev.value !== "!") {
            isUnary = true;
          } else if (prev.type === "BRACKET" && prev.value === "(") {
            isUnary = true;
          }
        }
        if (isUnary) {
          operators.push({ type: "OPERATOR", value: "U-" });
          continue;
        }
      }

      while (operators.length > 0 && operators[operators.length - 1].value !== "(" &&
             (operators[operators.length - 1].type === "FUNCTION" ||
              precedence[operators[operators.length - 1].value] > precedence[opVal] ||
              (precedence[operators[operators.length - 1].value] === precedence[opVal] && opVal !== "^"))) {
        output.push(operators.pop());
      }
      operators.push({ type: "OPERATOR", value: opVal });
    } else if (token.value === "(") {
      operators.push(token);
    } else if (token.value === ")") {
      let lBracket = false;
      while (operators.length > 0) {
        if (operators[operators.length - 1].value === "(") {
          operators.pop();
          lBracket = true;
          break;
        } else {
          output.push(operators.pop());
        }
      }
      if (!lBracket) {
        throw new Error("Syntax ERROR");
      }
    }
  }

  while (operators.length > 0) {
    let op = operators.pop();
    if (op.value === "(") {
      throw new Error("Syntax ERROR");
    }
    output.push(op);
  }

  return output;
}

function evaluateRPN(rpn) {
  let stack = [];

  for (let token of rpn) {
    if (token.type === "NUMBER") {
      stack.push(token.value);
    } else if (token.type === "CONSTANT") {
      stack.push(token.value === "pi" ? Math.PI : Math.E);
    } else if (token.type === "VARIABLE") {
      stack.push(variables[token.value]);
    } else if (token.value === "Ans") {
      stack.push(previousAns);
    } else if (token.value === "U-") {
      if (stack.length < 1) throw new Error("Syntax ERROR");
      stack.push(-stack.pop());
    } else if (token.value === "!") {
      if (stack.length < 1) throw new Error("Syntax ERROR");
      let val = stack.pop();
      if (val < 0 || !Number.isInteger(val)) throw new Error("Math ERROR");
      stack.push(factorial(val));
    } else if (token.type === "OPERATOR") {
      if (stack.length < 2) throw new Error("Syntax ERROR");
      let b = stack.pop();
      let a = stack.pop();

      if (token.value === "+") stack.push(a + b);
      else if (token.value === "-") stack.push(a - b);
      else if (token.value === "*") stack.push(a * b);
      else if (token.value === "/") {
        if (b === 0) throw new Error("Math ERROR");
        stack.push(a / b);
      }
      else if (token.value === "%") stack.push(a % b);
      else if (token.value === "^") stack.push(Math.pow(a, b));
      else if (token.value === "C") stack.push(combinations(a, b));
      else if (token.value === "P") stack.push(permutations(a, b));
    } else if (token.type === "FUNCTION") {
      if (stack.length < 1) throw new Error("Syntax ERROR");
      let val = stack.pop();
      let angle = val;

      if (["sin", "cos", "tan"].includes(token.value) && angleMode === "DEG") {
        angle = (val * Math.PI) / 180;
      }

      let res = 0;
      if (token.value === "sin") res = Math.sin(angle);
      else if (token.value === "cos") res = Math.cos(angle);
      else if (token.value === "tan") res = Math.tan(angle);
      else if (token.value === "asin") {
        if (val < -1 || val > 1) throw new Error("Math ERROR");
        res = Math.asin(val);
        if (angleMode === "DEG") res = (res * 180) / Math.PI;
      }
      else if (token.value === "acos") {
        if (val < -1 || val > 1) throw new Error("Math ERROR");
        res = Math.acos(val);
        if (angleMode === "DEG") res = (res * 180) / Math.PI;
      }
      else if (token.value === "atan") {
        res = Math.atan(val);
        if (angleMode === "DEG") res = (res * 180) / Math.PI;
      }
      else if (token.value === "log") {
        if (val <= 0) throw new Error("Math ERROR");
        res = Math.log10(val);
      }
      else if (token.value === "ln") {
        if (val <= 0) throw new Error("Math ERROR");
        res = Math.log(val);
      }
      else if (token.value === "sqrt") {
        if (val < 0) throw new Error("Math ERROR");
        res = Math.sqrt(val);
      }
      else if (token.value === "cbrt") {
        res = Math.cbrt(val);
      }
      else if (token.value === "abs") {
        res = Math.abs(val);
      }
      stack.push(res);
    }
  }

  if (stack.length !== 1) {
    throw new Error("Syntax ERROR");
  }

  return stack[0];
}

function factorial(n) {
  if (n < 0) return 0;
  let res = 1;
  for (let i = 2; i <= n; i++) res *= i;
  return res;
}

function combinations(n, r) {
  if (n < 0 || r < 0 || r > n) throw new Error("Math ERROR");
  return factorial(n) / (factorial(r) * factorial(n - r));
}

function permutations(n, r) {
  if (n < 0 || r < 0 || r > n) throw new Error("Math ERROR");
  return factorial(n) / factorial(n - r);
}

function evaluateExpression() {
  if (!expression) return;

  try {
    let tokens = tokenize(expression);
    let processed = processImplicitMultiplication(tokens);
    let rpn = shuntingYard(processed);
    let result = evaluateRPN(rpn);

    // Format output decimal rounding (up to 10 decimals)
    let rounded = Math.round(result * 1e10) / 1e10;
    resultDisplay.innerText = rounded.toString();
    previousAns = rounded;

    // Save to history list
    history.push({ expr: expression, result: rounded.toString() });
    if (history.length > 50) history.shift(); // Limit history to last 50 items
    localStorage.setItem("casio_history", JSON.stringify(history));
    renderHistory();
  } catch (err) {
    resultDisplay.innerText = err.message || "Syntax ERROR";
  }
}