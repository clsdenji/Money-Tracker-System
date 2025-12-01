import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;   


public class MoneyTrackerGUI extends JFrame {

    private static final String USER_FILE_PATH = "C:\\EXPMNG\\user.txt";

    // card layout (LOGIN / REGISTER / DASHBOARD)
    private CardLayout cardLayout;
    private JPanel rootPanel;

    // LOGIN fields
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    // REGISTER fields
    private JTextField regNameField;
    private JTextField regUsernameField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;

    // DASHBOARD fields
    private JTextField amountField;
    private JTextField categoryField;
    private JComboBox<String> typeCombo;
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JLabel balanceLabel;
    private DefaultTableModel tableModel;

    // in-memory transactions for current session
    private final List<Transaction> transactions = new ArrayList<>();

    // Theme: green financial vibe
    private final Color PRIMARY_GREEN = new Color(46, 125, 50);     // main brand color
    private final Color ACCENT_GREEN = new Color(102, 187, 106);    // lighter accent
    private final Color LIGHT_BG = Color.WHITE;                      // main background (use white)
    private final Color DARK_TEXT = new Color(27, 94, 32);
    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 16);
    private final Font NORMAL_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private void styleButton(JButton b) {
        b.setBackground(PRIMARY_GREEN);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(NORMAL_FONT);
    }

    private void styleLabel(JLabel l, boolean isTitle) {
        l.setForeground(DARK_TEXT);
        l.setFont(isTitle ? TITLE_FONT : NORMAL_FONT);
    }

    private ImageIcon loadIcon(String relPath, int w, int h) {
        try {
            File f = new File(relPath);
            if (!f.exists()) return null;
            BufferedImage img = ImageIO.read(f);
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception ex) {
            return null;
        }
    }

    private BufferedImage loadImage(String relPath) {
        try {
            File f = new File(relPath);
            if (!f.exists()) return null;
            return ImageIO.read(f);
        } catch (Exception ex) {
            return null;
        }
    }

    // Panel that draws a BufferedImage preserving its aspect ratio (no compression)
    private static class ImagePanel extends JPanel {
        private final BufferedImage img;

        ImagePanel(BufferedImage img) {
            this.img = img;
            setOpaque(true);
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int panelW = getWidth();
            int panelH = getHeight();
            int imgW = img.getWidth();
            int imgH = img.getHeight();
            double panelRatio = (double) panelW / panelH;
            double imgRatio = (double) imgW / imgH;
            int drawW, drawH;
            if (imgRatio > panelRatio) {
                // image is wider: fit width
                drawW = panelW;
                drawH = (int) (panelW / imgRatio);
            } else {
                // image is taller: fit height
                drawH = panelH;
                drawW = (int) (panelH * imgRatio);
            }
            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;
            g2.drawImage(img, x, y, drawW, drawH, null);
            g2.dispose();
        }
    }

    public MoneyTrackerGUI() {
        setTitle("Money Tracker System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 480);
        setLocationRelativeTo(null);

        ensureUserFileExists();

        // try to set the application/window icon from assets
        ImageIcon appIcon = loadIcon("src/assets/images/manage.png", 32, 32);
        if (appIcon != null) setIconImage(appIcon.getImage());

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);

        rootPanel.add(createLoginPanel(), "LOGIN");
        rootPanel.add(createRegisterPanel(), "REGISTER");
        rootPanel.add(createDashboardPanel(), "DASHBOARD");

        setContentPane(rootPanel);
        cardLayout.show(rootPanel, "LOGIN");
    }

    // ---------- LOGIN PANEL ----------
    private JPanel createLoginPanel() {
        // build the left form first (GridBag)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(LIGHT_BG);
        GridBagConstraints gbc = baseGbc();

        JLabel title = new JLabel("Money Tracker - Login", SwingConstants.CENTER);
        styleLabel(title, true);

        loginUsernameField = new JTextField(15);
        loginPasswordField = new JPasswordField(15);
        // constrain width to avoid stretching across the window
        Dimension smallField = new Dimension(220, 24);
        loginUsernameField.setPreferredSize(smallField);
        loginPasswordField.setPreferredSize(smallField);
        JButton loginBtn = new JButton("Login");
        JButton goToRegisterBtn = new JButton("Create Account");

        styleButton(loginBtn);
        styleButton(goToRegisterBtn);

        loginBtn.addActionListener(this::handleLogin);
        goToRegisterBtn.addActionListener(e -> cardLayout.show(rootPanel, "REGISTER"));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(title, gbc);

        gbc.gridwidth = 1; gbc.gridy++;
        JLabel userLbl = new JLabel("Username:");
        styleLabel(userLbl, false);
        formPanel.add(userLbl, gbc);
        gbc.gridx = 1;
        formPanel.add(loginUsernameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel passLbl = new JLabel("Password:");
        styleLabel(passLbl, false);
        formPanel.add(passLbl, gbc);
        gbc.gridx = 1;
        formPanel.add(loginPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(loginBtn, gbc);
        gbc.gridx = 1;
        formPanel.add(goToRegisterBtn, gbc);

        // (no filler here) keep form compact; centering handled by left wrapper

        // container: left = centered form, right = image
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_BG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        formPanel.setPreferredSize(new Dimension(320, 380));

        // left wrapper to center the form vertically and horizontally within the left column
        JPanel leftWrapper = new JPanel(new GridBagLayout());
        leftWrapper.setBackground(LIGHT_BG);
        GridBagConstraints lg = new GridBagConstraints();
        lg.gridx = 0; lg.gridy = 0; lg.weightx = 1.0; lg.weighty = 1.0; lg.anchor = GridBagConstraints.CENTER;
        leftWrapper.add(formPanel, lg);
        leftWrapper.setPreferredSize(new Dimension(340, 420));
        panel.add(leftWrapper, BorderLayout.WEST);

        BufferedImage rightImg = loadImage("src/assets/images/manage.png");
        JPanel imgPanel = new JPanel(new BorderLayout());
        imgPanel.setBackground(Color.WHITE);
        if (rightImg != null) {
            ImagePanel ip = new ImagePanel(rightImg);
            ip.setPreferredSize(new Dimension(340, 420));
            imgPanel.add(ip, BorderLayout.CENTER);
        }
        panel.add(imgPanel, BorderLayout.CENTER);

        return panel;
    }

    private void handleLogin(ActionEvent e) {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean ok = User.userLogin(username, password);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Login successful!");
            transactions.clear();              // new session
            refreshTableAndSummary();
            cardLayout.show(rootPanel, "DASHBOARD");
        } else {
            JOptionPane.showMessageDialog(this, "Login failed. Please try again.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- REGISTER PANEL ----------
    private JPanel createRegisterPanel() {
        // build the left form first (GridBag)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(LIGHT_BG);
        GridBagConstraints gbc = baseGbc();

        JLabel title = new JLabel("Create New Account", SwingConstants.CENTER);
        styleLabel(title, true);

        regNameField = new JTextField(15);
        regUsernameField = new JTextField(15);
        regPasswordField = new JPasswordField(15);
        regConfirmPasswordField = new JPasswordField(15);
        Dimension smallField = new Dimension(220, 24);
        regNameField.setPreferredSize(smallField);
        regUsernameField.setPreferredSize(smallField);
        regPasswordField.setPreferredSize(smallField);
        regConfirmPasswordField.setPreferredSize(smallField);
        JButton registerBtn = new JButton("Sign Up");
        JButton backBtn = new JButton("Back to Login");

        registerBtn.addActionListener(this::handleRegister);
        backBtn.addActionListener(e -> cardLayout.show(rootPanel, "LOGIN"));

        styleButton(registerBtn);
        styleButton(backBtn);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(title, gbc);

        gbc.gridwidth = 1; gbc.gridy++;
        JLabel nameLbl = new JLabel("Name:");
        styleLabel(nameLbl, false);
        formPanel.add(nameLbl, gbc);
        gbc.gridx = 1;
        formPanel.add(regNameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel userLbl = new JLabel("Username:");
        styleLabel(userLbl, false);
        formPanel.add(userLbl, gbc);
        gbc.gridx = 1;
        formPanel.add(regUsernameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel passLbl = new JLabel("Password:");
        styleLabel(passLbl, false);
        formPanel.add(passLbl, gbc);
        gbc.gridx = 1;
        formPanel.add(regPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel confirmLbl = new JLabel("Confirm Password:");
        styleLabel(confirmLbl, false);
        formPanel.add(confirmLbl, gbc);
        gbc.gridx = 1;
        formPanel.add(regConfirmPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(registerBtn, gbc);
        gbc.gridx = 1;
        formPanel.add(backBtn, gbc);

        // (no filler here) keep form compact; centering handled by left wrapper

        // container: left = centered form, right = image
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_BG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        formPanel.setPreferredSize(new Dimension(320, 380));

        // left wrapper to center the form vertically and horizontally within the left column
        JPanel leftWrapper = new JPanel(new GridBagLayout());
        leftWrapper.setBackground(LIGHT_BG);
        GridBagConstraints lg = new GridBagConstraints();
        lg.gridx = 0; lg.gridy = 0; lg.weightx = 1.0; lg.weighty = 1.0; lg.anchor = GridBagConstraints.CENTER;
        leftWrapper.add(formPanel, lg);
        leftWrapper.setPreferredSize(new Dimension(340, 420));
        panel.add(leftWrapper, BorderLayout.WEST);

        BufferedImage rightImg = loadImage("src/assets/images/manage.png");
        JPanel imgPanel = new JPanel(new BorderLayout());
        imgPanel.setBackground(Color.WHITE);
        if (rightImg != null) {
            ImagePanel ip = new ImagePanel(rightImg);
            ip.setPreferredSize(new Dimension(340, 420));
            imgPanel.add(ip, BorderLayout.CENTER);
        }
        panel.add(imgPanel, BorderLayout.CENTER);

        return panel;
    }

    private void handleRegister(ActionEvent e) {
        String name = regNameField.getText().trim();
        String username = regUsernameField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String confirm = new String(regConfirmPasswordField.getPassword());

        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.",
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.",
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (userExists(username)) {
            JOptionPane.showMessageDialog(this, "Username already exists.",
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (writeUserToFile(name, username, password)) {
            JOptionPane.showMessageDialog(this, "Account created! You can now log in.");
            regNameField.setText("");
            regUsernameField.setText("");
            regPasswordField.setText("");
            regConfirmPasswordField.setText("");
            cardLayout.show(rootPanel, "LOGIN");
        } else {
            JOptionPane.showMessageDialog(this, "Error saving user file.",
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- DASHBOARD PANEL ----------
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_BG);

        // top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(PRIMARY_GREEN);
        JLabel title = new JLabel("Money Tracker Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(TITLE_FONT);
        // no icon beside dashboard title (text-only header)
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            transactions.clear();
            refreshTableAndSummary();
            cardLayout.show(rootPanel, "LOGIN");
        });
        // logout uses accent so it stands out on the dark top bar
        logoutBtn.setBackground(ACCENT_GREEN);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setFont(NORMAL_FONT);
        top.add(title, BorderLayout.WEST);
        top.add(logoutBtn, BorderLayout.EAST);

        // table
        tableModel = new DefaultTableModel(
                new Object[]{"Date", "Type", "Category", "Amount", "Balance"}, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // table styling
        table.setRowHeight(24);
        table.setFont(NORMAL_FONT);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(200, 230, 201));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setBackground(PRIMARY_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(NORMAL_FONT.deriveFont(Font.BOLD));

        // bottom: form + summary
        JPanel bottom = new JPanel(new BorderLayout());

        // form
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();

        typeCombo = new JComboBox<>(new String[]{"Expense", "Income"});
        categoryField = new JTextField(10);
        amountField = new JTextField(10);
        JButton addBtn = new JButton("Add Transaction");
        addBtn.addActionListener(this::handleAddTransaction);
        styleButton(addBtn);

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel typeLbl = new JLabel("Type:");
        styleLabel(typeLbl, false);
        form.add(typeLbl, gbc);
        gbc.gridx = 1;
        form.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel catLbl = new JLabel("Category:");
        styleLabel(catLbl, false);
        form.add(catLbl, gbc);
        gbc.gridx = 1;
        form.add(categoryField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel amtLbl = new JLabel("Amount:");
        styleLabel(amtLbl, false);
        form.add(amtLbl, gbc);
        gbc.gridx = 1;
        form.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        form.add(addBtn, gbc);

        // summary
        JPanel summary = new JPanel(new GridLayout(1, 3));
        summary.setBackground(LIGHT_BG);
        incomeLabel = new JLabel("Total Income: 0.00");
        expenseLabel = new JLabel("Total Expense: 0.00");
        balanceLabel = new JLabel("Balance: 0.00");
        styleLabel(incomeLabel, false);
        styleLabel(expenseLabel, false);
        styleLabel(balanceLabel, false);
        incomeLabel.setForeground(PRIMARY_GREEN.darker());
        expenseLabel.setForeground(Color.RED.darker());
        balanceLabel.setForeground(DARK_TEXT);
        summary.add(incomeLabel);
        summary.add(expenseLabel);
        summary.add(balanceLabel);

        bottom.add(form, BorderLayout.CENTER);
        bottom.add(summary, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void handleAddTransaction(ActionEvent e) {
        String typeStr = (String) typeCombo.getSelectedItem();
        String category = categoryField.getText().trim();
        String amountText = amountField.getText().trim();

        if (category.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter category and amount.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount must be a number.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int transactionType = "Expense".equalsIgnoreCase(typeStr) ? 1 : 2;

        // compute current balance based on previous transactions
        double income = 0;
        double expense = 0;
        for (Transaction t : transactions) {
            double a = Double.parseDouble(t.getAmount());
            if (t.getTransactionType() == 1) {
                expense += a;
            } else {
                income += a;
            }
        }
        if (transactionType == 1) {
            expense += amount;
        } else {
            income += amount;
        }
        double balance = income - expense;

        Transaction t = new Transaction(new Date(), category, amountText, transactionType, balance);
        t.setTotalAmount(balance); // reuse this field as running balance
        transactions.add(t);

        categoryField.setText("");
        amountField.setText("");

        refreshTableAndSummary();
    }

    // ---------- HELPERS ----------
    private void refreshTableAndSummary() {
        tableModel.setRowCount(0);
        double income = 0;
        double expense = 0;

        for (Transaction t : transactions) {
            String typeStr = (t.getTransactionType() == 1) ? "Expense" : "Income";
            double amt = Double.parseDouble(t.getAmount());
            if (t.getTransactionType() == 1) expense += amt;
            else income += amt;

            tableModel.addRow(new Object[]{
                    t.getDate(), typeStr, t.getCategory(),
                    String.format("%.2f", amt),
                    String.format("%.2f", t.getBalance())
            });
        }

        double balance = income - expense;
        incomeLabel.setText(String.format("Total Income: %.2f", income));
        expenseLabel.setText(String.format("Total Expense: %.2f", expense));
        balanceLabel.setText(String.format("Balance: %.2f", balance));
    }

    private static GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private static void ensureUserFileExists() {
        try {
            File file = new File(USER_FILE_PATH);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error ensuring user file: " + e.getMessage());
        }
    }

    private static boolean userExists(String username) {
        try (Scanner fileScanner = new Scanner(new File(USER_FILE_PATH))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[1].equals(username)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            System.out.println("Error reading user file: " + ex.getMessage());
        }
        return false;
    }

    private static boolean writeUserToFile(String name, String username, String password) {
        try (FileWriter writer = new FileWriter(USER_FILE_PATH, true)) {
            writer.write(name + "," + username + "," + password + "\n");
            return true;
        } catch (IOException e) {
            System.out.println("Error writing user file: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MoneyTrackerGUI gui = new MoneyTrackerGUI();
            gui.setVisible(true);
        });
    }
}
