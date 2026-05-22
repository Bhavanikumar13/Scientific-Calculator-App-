package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class CalculatorApp extends JFrame {

    private String expression = "";
    private int cursorPos = 0;
    private double prevAns = 0.0;
    private final Map<String, Double> variables = new HashMap<>();
    private final HistoryManager history = new HistoryManager();

    private boolean isShift = false;
    private boolean isAlpha = false;
    private String angleMode = "DEG"; // DEG or RAD

    // Swing elements
    private JLabel lblShift;
    private JLabel lblAlpha;
    private JLabel lblMem;
    private JLabel lblAngle;
    private JLabel lblExpr;
    private JLabel lblResult;

    public CalculatorApp() {
        // Reset variables
        variables.put("A", 0.0);
        variables.put("B", 0.0);
        variables.put("C", 0.0);
        variables.put("D", 0.0);
        variables.put("X", 0.0);
        variables.put("Y", 0.0);
        variables.put("M", 0.0);

        setTitle("Casio fx-991EX Swing Desktop");
        setSize(450, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(0x22, 0x25, 0x2a));
        setLayout(new BorderLayout(10, 10));

        createWidgets();
        bindKeys();
        updateDisplay();
    }

    private void createWidgets() {
        // 1. Top Panel: Solar Panel mockup and brand labels
        JPanel pnlTop = new JPanel(new BorderLayout(5, 5));
        pnlTop.setBackground(new Color(0x22, 0x25, 0x2a));
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        JPanel pnlBrand = new JPanel(new GridLayout(2, 1));
        pnlBrand.setBackground(new Color(0x22, 0x25, 0x2a));
        JLabel lblBrandName = new JLabel("CASIO");
        lblBrandName.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 18));
        lblBrandName.setForeground(new Color(0xf3, 0xf4, 0xf6));
        JLabel lblModel = new JLabel("fx-991EX CLASSWIZ");
        lblModel.setFont(new Font("Arial", Font.BOLD, 9));
        lblModel.setForeground(new Color(0x94, 0xa3, 0xb8));
        pnlBrand.add(lblBrandName);
        pnlBrand.add(lblModel);
        pnlTop.add(pnlBrand, BorderLayout.WEST);

        // Solar Grid representation
        JPanel pnlSolar = new JPanel(new GridLayout(1, 4, 2, 2));
        pnlSolar.setBackground(new Color(0x4a, 0x26, 0x08));
        pnlSolar.setBorder(BorderFactory.createLineBorder(new Color(0x1c, 0x1c, 0x1f), 2));
        pnlSolar.setPreferredSize(new Dimension(100, 30));
        for (int i = 0; i < 4; i++) {
            JPanel grid = new JPanel();
            grid.setBackground(new Color(0x11, 0x11, 0x11));
            pnlSolar.add(grid);
        }
        pnlTop.add(pnlSolar, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // 2. Display LCD Panel
        JPanel pnlDisplayFrame = new JPanel(new BorderLayout());
        pnlDisplayFrame.setBackground(new Color(0x1a, 0x1c, 0x1e));
        pnlDisplayFrame.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 15, 5, 15),
                BorderFactory.createLineBorder(new Color(0x2d, 0x30, 0x35), 5)
        ));

        JPanel pnlLcd = new JPanel(new BorderLayout());
        pnlLcd.setBackground(new Color(0xa8, 0xc3, 0xa0));
        pnlLcd.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Indicators
        JPanel pnlIndicators = new JPanel(new BorderLayout());
        pnlIndicators.setBackground(new Color(0xa8, 0xc3, 0xa0));
        
        JPanel pnlLeftInd = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLeftInd.setBackground(new Color(0xa8, 0xc3, 0xa0));
        lblShift = new JLabel("S");
        lblShift.setFont(new Font("Courier New", Font.BOLD, 10));
        lblAlpha = new JLabel("A");
        lblAlpha.setFont(new Font("Courier New", Font.BOLD, 10));
        lblMem = new JLabel("M");
        lblMem.setFont(new Font("Courier New", Font.BOLD, 10));
        pnlLeftInd.add(lblShift);
        pnlLeftInd.add(lblAlpha);
        pnlLeftInd.add(lblMem);

        lblAngle = new JLabel("DEG");
        lblAngle.setFont(new Font("Courier New", Font.BOLD, 10));
        lblAngle.setForeground(new Color(0x1a, 0x25, 0x18));

        pnlIndicators.add(pnlLeftInd, BorderLayout.WEST);
        pnlIndicators.add(lblAngle, BorderLayout.EAST);
        pnlLcd.add(pnlIndicators, BorderLayout.NORTH);

        // Lines
        JPanel pnlLines = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlLines.setBackground(new Color(0xa8, 0xc3, 0xa0));

        lblExpr = new JLabel("");
        lblExpr.setFont(new Font("Courier New", Font.PLAIN, 18));
        lblExpr.setForeground(new Color(0x1a, 0x25, 0x18));

        lblResult = new JLabel("0");
        lblResult.setFont(new Font("Courier New", Font.BOLD, 24));
        lblResult.setForeground(new Color(0x1a, 0x25, 0x18));
        lblResult.setHorizontalAlignment(SwingConstants.RIGHT);

        pnlLines.add(lblExpr);
        pnlLines.add(lblResult);
        pnlLcd.add(pnlLines, BorderLayout.CENTER);
        pnlDisplayFrame.add(pnlLcd, BorderLayout.CENTER);

        // Wrap middle section
        JPanel pnlCenterWrap = new JPanel(new BorderLayout());
        pnlCenterWrap.setBackground(new Color(0x22, 0x25, 0x2a));
        pnlCenterWrap.add(pnlDisplayFrame, BorderLayout.NORTH);

        // 3. Keypad Grid Frame
        JPanel pnlKeypad = new JPanel(new GridLayout(8, 5, 6, 6));
        pnlKeypad.setBackground(new Color(0x22, 0x25, 0x2a));
        pnlKeypad.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        // Colors
        Color colorSci = new Color(0x3c, 0x40, 0x48);
        Color colorDigit = new Color(0x1e, 0x21, 0x24);
        Color colorAction = new Color(0xdb, 0x6a, 0x45);
        Color colorEquals = new Color(0x3b, 0x82, 0xf6);
        Color colorText = new Color(0xf3, 0xf4, 0xf6);

        // Custom Buttons layout mapping
        String[][] btnLabels = {
                {"SHIFT", "ALPHA", "◀", "▶", "DRG"},
                {"x⁻¹", "nCr", "√", "x²", "xʸ"},
                {"log", "ln", "(-)", "°′″", "sin"},
                {"cos", "tan", "(", ")", "M+"},
                {"7", "8", "9", "DEL", "AC"},
                {"4", "5", "6", "×", "÷"},
                {"1", "2", "3", "+", "−"},
                {"0", ".", "×10ˣ", "Ans", "="}
        };

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 5; c++) {
                String label = btnLabels[r][c];
                JButton btn = new JButton(label);
                btn.setFocusable(false);
                btn.setBorder(BorderFactory.createRaisedBevelBorder());
                btn.setFont(new Font("Arial", Font.BOLD, 12));
                btn.setForeground(colorText);

                // Set backgrounds based on row/column rules
                if (label.equals("SHIFT")) {
                    btn.setForeground(Color.YELLOW);
                    btn.setBackground(colorSci);
                } else if (label.equals("ALPHA")) {
                    btn.setForeground(Color.ORANGE);
                    btn.setBackground(colorSci);
                } else if (r <= 3) {
                    btn.setBackground(colorSci);
                    btn.setFont(new Font("Arial", Font.BOLD, 10));
                } else if (label.equals("DEL") || label.equals("AC")) {
                    btn.setBackground(colorAction);
                } else if (label.equals("=")) {
                    btn.setBackground(colorEquals);
                } else {
                    btn.setBackground(colorDigit);
                    btn.setFont(new Font("Arial", Font.BOLD, 14));
                }

                // Add actions
                btn.addActionListener(e -> handleBtn(label));
                pnlKeypad.add(btn);
            }
        }

        pnlCenterWrap.add(pnlKeypad, BorderLayout.CENTER);
        add(pnlCenterWrap, BorderLayout.CENTER);
    }

    private void bindKeys() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char charKey = e.getKeyChar();
                int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_ESCAPE) {
                    handleBtn("AC");
                } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
                    handleBtn("DEL");
                } else if (keyCode == KeyEvent.VK_ENTER || charKey == '=') {
                    handleBtn("=");
                } else if (keyCode == KeyEvent.VK_LEFT) {
                    handleBtn("◀");
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    handleBtn("▶");
                } else if ("0123456789.+-()".indexOf(charKey) >= 0) {
                    handleBtn(String.valueOf(charKey));
                } else if (charKey == '*') {
                    handleBtn("×");
                } else if (charKey == '/') {
                    handleBtn("÷");
                } else if (charKey == 's') {
                    handleBtn("sin");
                } else if (charKey == 'c') {
                    handleBtn("cos");
                } else if (charKey == 't') {
                    handleBtn("tan");
                } else if (charKey == 'r') {
                    handleBtn("√");
                } else if (charKey == 'l') {
                    handleBtn("ln");
                } else if (charKey == 'g') {
                    handleBtn("log");
                }
            }
        });
        setFocusable(true);
        requestFocusInWindow();
    }

    private void handleBtn(String label) {
        // Reset keyboard focus to key listener capture
        requestFocusInWindow();

        if (label.equals("SHIFT")) {
            isShift = !isShift;
            isAlpha = false;
        } else if (label.equals("ALPHA")) {
            isAlpha = !isAlpha;
            isShift = false;
        } else if (label.equals("◀")) {
            cursorPos = Math.max(0, cursorPos - 1);
        } else if (label.equals("▶")) {
            cursorPos = Math.min(expression.length(), cursorPos + 1);
        } else if (label.equals("DRG")) {
            angleMode = angleMode.equals("DEG") ? "RAD" : "DEG";
            isShift = false;
            isAlpha = false;
        } else if (label.equals("DEL")) {
            handleDel();
        } else if (label.equals("AC")) {
            expression = "";
            cursorPos = 0;
            lblResult.setText("0");
            isShift = false;
            isAlpha = false;
        } else if (label.equals("=")) {
            evaluateExpression();
            isShift = false;
            isAlpha = false;
        } else if (label.equals("M+")) {
            if (isShift) {
                // M-
                try {
                    double val = Double.parseDouble(lblResult.getText());
                    variables.put("M", variables.get("M") - val);
                } catch (NumberFormatException ignored) {}
            } else if (isAlpha) {
                insertExpr("M");
            } else {
                // M+
                try {
                    double val = Double.parseDouble(lblResult.getText());
                    variables.put("M", variables.get("M") + val);
                } catch (NumberFormatException ignored) {}
            }
            isShift = false;
            isAlpha = false;
        } else if (label.equals("sin")) {
            if (isShift) insertExpr("asin(");
            else insertExpr("sin(");
            isShift = false;
        } else if (label.equals("cos")) {
            if (isShift) insertExpr("acos(");
            else insertExpr("cos(");
            isShift = false;
        } else if (label.equals("tan")) {
            if (isShift) insertExpr("atan(");
            else insertExpr("tan(");
            isShift = false;
        } else if (label.equals("√")) {
            if (isShift) {
                insertExpr("³√(");
            } else {
                insertExpr("√(");
            }
            isShift = false;
        } else if (label.equals("x²")) {
            if (isShift) {
                insertExpr("³");
            } else {
                insertExpr("²");
            }
            isShift = false;
        } else if (label.equals("xʸ")) {
            insertExpr("^(");
        } else if (label.equals("x⁻¹")) {
            if (isShift) insertExpr("!");
            else insertExpr("⁻¹");
            isShift = false;
        } else if (label.equals("nCr")) {
            if (isShift) insertExpr("P");
            else insertExpr("C");
            isShift = false;
        } else if (label.equals("(-)")) {
            if (isAlpha) insertExpr("A");
            else insertExpr("-");
            isAlpha = false;
        } else if (label.equals("°′″")) {
            if (isAlpha) insertExpr("B");
            else insertExpr("°");
            isAlpha = false;
        } else if (label.equals("×10ˣ")) {
            if (isShift) insertExpr("π");
            else if (isAlpha) insertExpr("e");
            else insertExpr("*10^(");
            isShift = false;
            isAlpha = false;
        } else if (label.equals("Ans")) {
            insertExpr("Ans");
        } else {
            // Map Variable triggers for numbers
            if (isAlpha && (label.equals("7") || label.equals("8") || label.equals("9") || label.equals("4"))) {
                Map<String, String> mapping = Map.of("7", "C", "8", "D", "9", "X", "4", "Y");
                insertExpr(mapping.get(label));
            } else {
                insertExpr(label);
            }
            isShift = false;
            isAlpha = false;
        }

        updateDisplay();
    }

    private void insertExpr(String text) {
        expression = expression.substring(0, cursorPos) + text + expression.substring(cursorPos);
        cursorPos += text.length();
    }

    private void handleDel() {
        if (cursorPos > 0) {
            int deleteSize = 1;
            String checkStr = expression.substring(0, cursorPos);
            if (checkStr.endsWith("sin(") || checkStr.endsWith("cos(") || checkStr.endsWith("tan(")) {
                deleteSize = 4;
            } else if (checkStr.endsWith("asin(") || checkStr.endsWith("acos(") || checkStr.endsWith("atan(")) {
                deleteSize = 5;
            } else if (checkStr.endsWith("log(") || checkStr.endsWith("Ans")) {
                deleteSize = 4;
            } else if (checkStr.endsWith("ln(") || checkStr.endsWith("√(") || checkStr.endsWith("³√(")) {
                deleteSize = 3;
            } else if (checkStr.endsWith("*10^(")) {
                deleteSize = 5;
            }

            expression = expression.substring(0, cursorPos - deleteSize) + expression.substring(cursorPos);
            cursorPos -= deleteSize;
        }
    }

    private void updateDisplay() {
        lblShift.setForeground(isShift ? Color.YELLOW : new Color(0xa8, 0xc3, 0xa0));
        lblAlpha.setForeground(isAlpha ? Color.ORANGE : new Color(0xa8, 0xc3, 0xa0));
        lblMem.setForeground(variables.get("M") != 0.0 ? new Color(0x1a, 0x25, 0x18) : new Color(0xa8, 0xc3, 0xa0));
        lblAngle.setText(angleMode);

        if (expression.isEmpty()) {
            lblExpr.setText("|");
        } else {
            lblExpr.setText(expression.substring(0, cursorPos) + "|" + expression.substring(cursorPos));
        }
    }

    private void evaluateExpression() {
        if (expression.isEmpty()) return;

        try {
            double result = MathEngine.evaluate(expression, angleMode, prevAns, variables);
            
            // Format output decimal roundings
            double rounded = Math.round(result * 1e10) / 1e10;
            String strRes;
            if (rounded == (long) rounded) {
                strRes = String.valueOf((long) rounded);
            } else {
                strRes = String.valueOf(rounded);
            }

            lblResult.setText(strRes);
            prevAns = rounded;
            history.addRecord(expression, strRes);
        } catch (IllegalArgumentException | ArithmeticException e) {
            lblResult.setText(e.getMessage());
        } catch (Exception e) {
            lblResult.setText("Syntax ERROR");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalculatorApp app = new CalculatorApp();
            app.setVisible(true);
        });
    }
}
