import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

// Improved Expense Class
class Expense {
    private int amount;
    private String description;
    private LocalDate date;
    private String category;

    public Expense(int amount, String description, LocalDate date, String category) {
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.category = category;
    }

    public int getAmount() { return amount; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }

    public void setAmount(int amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
}

// Improved Backend with better data structure
class ExpenseManager {
    private static int expenseIDCounter = 1;
    private static Map<Integer, Expense> expenses = new LinkedHashMap<>();

    public static int addExpense(int amount, String description, String category) {
        int id = expenseIDCounter++;
        expenses.put(id, new Expense(amount, description, LocalDate.now(), category));
        return id;
    }

    public static boolean updateExpense(int id, int amount, String description, String category) {
        Expense expense = expenses.get(id);
        if (expense != null) {
            expense.setAmount(amount);
            expense.setDescription(description);
            expense.setCategory(category);
            return true;
        }
        return false;
    }

    public static boolean deleteExpense(int id) {
        return expenses.remove(id) != null;
    }

    public static Map<Integer, Expense> getAllExpenses() {
        return new LinkedHashMap<>(expenses);
    }

    public static int getTotalExpenses() {
        return expenses.values().stream().mapToInt(Expense::getAmount).sum();
    }

    public static Map<String, Integer> getCategoryWiseExpenses() {
        Map<String, Integer> categoryTotals = new HashMap<>();
        for (Expense e : expenses.values()) {
            categoryTotals.put(e.getCategory(), 
                categoryTotals.getOrDefault(e.getCategory(), 0) + e.getAmount());
        }
        return categoryTotals;
    }

    public static List<Map.Entry<Integer, Expense>> getExpensesByDateRange(MonthDay from, MonthDay to) {
        List<Map.Entry<Integer, Expense>> filtered = new ArrayList<>();
        for (Map.Entry<Integer, Expense> entry : expenses.entrySet()) {
            MonthDay expenseMD = MonthDay.from(entry.getValue().getDate());
            if (!expenseMD.isBefore(from) && !expenseMD.isAfter(to)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }
}

// Main GUI Application
public class ExpenseTrackerGUI extends JFrame {
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JLabel countLabel;
    
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private static final Color ACCENT_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color BG_COLOR = new Color(236, 240, 241);
    
    private String[] categories = {"Food", "Transport", "Entertainment", "Shopping", 
                                   "Bills", "Health", "Education", "Other"};

    public ExpenseTrackerGUI() {
        setTitle("Expense Tracker Pro");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BG_COLOR);

        // Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Center Panel with Table
        add(createTablePanel(), BorderLayout.CENTER);
        
        // Bottom Panel with Buttons
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        // Side Panel with Summary
        add(createSummaryPanel(), BorderLayout.EAST);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Expense Tracker");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Manage your finances efficiently");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 180));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

        // Table
        String[] columns = {"ID", "Amount (₹)", "Description", "Category", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        expenseTable = new JTable(tableModel);
        expenseTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        expenseTable.setRowHeight(30);
        expenseTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        expenseTable.getTableHeader().setBackground(SECONDARY_COLOR);
        expenseTable.getTableHeader().setForeground(Color.WHITE);
        expenseTable.setSelectionBackground(new Color(41, 128, 185, 100));
        expenseTable.setGridColor(new Color(189, 195, 199));
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        expenseTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(expenseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton addBtn = createStyledButton(" Add Expense", ACCENT_COLOR);
        JButton updateBtn = createStyledButton(" Update", PRIMARY_COLOR);
        JButton deleteBtn = createStyledButton(" Delete", DANGER_COLOR);
        JButton viewRangeBtn = createStyledButton(" Date Range", SECONDARY_COLOR);
        JButton refreshBtn = createStyledButton("Refresh", new Color(155, 89, 182));

        addBtn.addActionListener(e -> showAddExpenseDialog());
        updateBtn.addActionListener(e -> showUpdateExpenseDialog());
        deleteBtn.addActionListener(e -> deleteSelectedExpense());
        viewRangeBtn.addActionListener(e -> showDateRangeDialog());
        refreshBtn.addActionListener(e -> refreshTable());

        panel.add(addBtn);
        panel.add(updateBtn);
        panel.add(deleteBtn);
        panel.add(viewRangeBtn);
        panel.add(refreshBtn);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 20),
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1)
        ));
        panel.setPreferredSize(new Dimension(280, 0));

        JLabel summaryTitle = new JLabel(" Summary");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        summaryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryTitle.setBorder(BorderFactory.createEmptyBorder(15, 15, 20, 15));

        totalLabel = new JLabel("Total: ₹0");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalLabel.setForeground(PRIMARY_COLOR);
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));

        countLabel = new JLabel("Entries: 0");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        countLabel.setForeground(SECONDARY_COLOR);
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        countLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 20, 15));

        panel.add(summaryTitle);
        panel.add(totalLabel);
        panel.add(countLabel);
        panel.add(Box.createVerticalStrut(20));

        JButton categoryBtn = createStyledButton(" Category Wise", new Color(155, 89, 182));
        categoryBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        categoryBtn.addActionListener(e -> showCategoryWiseSummary());
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);
        btnPanel.add(categoryBtn);
        panel.add(btnPanel);

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(160, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private void showAddExpenseDialog() {
        JDialog dialog = new JDialog(this, "Add New Expense", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel amountLabel = new JLabel("Amount (₹):");
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField amountField = new JTextField(20);
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField descField = new JTextField(20);
        descField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("Add New Expense");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(PRIMARY_COLOR);
        dialog.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        dialog.add(amountLabel, gbc);
        gbc.gridx = 1;
        dialog.add(amountField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        dialog.add(descLabel, gbc);
        gbc.gridx = 1;
        dialog.add(descField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        dialog.add(categoryLabel, gbc);
        gbc.gridx = 1;
        dialog.add(categoryCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        
        JButton saveBtn = createStyledButton(" Save", ACCENT_COLOR);
        JButton cancelBtn = createStyledButton(" Cancel", DANGER_COLOR);

        saveBtn.addActionListener(e -> {
            try {
                int amount = Integer.parseInt(amountField.getText().trim());
                String desc = descField.getText().trim();
                String category = (String) categoryCombo.getSelectedItem();

                if (amount <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Amount must be positive!", 
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (desc.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Description cannot be empty!", 
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int id = ExpenseManager.addExpense(amount, desc, category);
                JOptionPane.showMessageDialog(dialog, 
                    "Expense added successfully with ID: " + id, 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid amount!", 
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    private void showUpdateExpenseDialog() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to update!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        Expense expense = ExpenseManager.getAllExpenses().get(id);

        JDialog dialog = new JDialog(this, "Update Expense", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel amountLabel = new JLabel("Amount (₹):");
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField amountField = new JTextField(String.valueOf(expense.getAmount()), 20);
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField descField = new JTextField(expense.getDescription(), 20);
        descField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setSelectedItem(expense.getCategory());
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("Update Expense #" + id);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(PRIMARY_COLOR);
        dialog.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        dialog.add(amountLabel, gbc);
        gbc.gridx = 1;
        dialog.add(amountField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        dialog.add(descLabel, gbc);
        gbc.gridx = 1;
        dialog.add(descField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        dialog.add(categoryLabel, gbc);
        gbc.gridx = 1;
        dialog.add(categoryCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        
        JButton updateBtn = createStyledButton(" Update", PRIMARY_COLOR);
        JButton cancelBtn = createStyledButton(" Cancel", DANGER_COLOR);

        updateBtn.addActionListener(e -> {
            try {
                int amount = Integer.parseInt(amountField.getText().trim());
                String desc = descField.getText().trim();
                String category = (String) categoryCombo.getSelectedItem();

                if (amount <= 0 || desc.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Invalid input!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (ExpenseManager.updateExpense(id, amount, desc, category)) {
                    JOptionPane.showMessageDialog(dialog, "Expense updated successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshTable();
                    dialog.dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid amount!", 
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(updateBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    private void deleteSelectedExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to delete!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete expense #" + id + "?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (ExpenseManager.deleteExpense(id)) {
                JOptionPane.showMessageDialog(this, "Expense deleted successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            }
        }
    }

    private void showDateRangeDialog() {
        JDialog dialog = new JDialog(this, "Filter by Date Range", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel fromLabel = new JLabel("From (DD/MM):");
        fromLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField fromField = new JTextField(10);
        fromField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel toLabel = new JLabel("To (DD/MM):");
        toLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField toField = new JTextField(10);
        toField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("Filter by Date Range");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(PRIMARY_COLOR);
        dialog.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        dialog.add(fromLabel, gbc);
        gbc.gridx = 1;
        dialog.add(fromField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        dialog.add(toLabel, gbc);
        gbc.gridx = 1;
        dialog.add(toField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        
        JButton filterBtn = createStyledButton(" Filter", PRIMARY_COLOR);
        JButton cancelBtn = createStyledButton(" Cancel", DANGER_COLOR);

        filterBtn.addActionListener(e -> {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
                MonthDay from = MonthDay.parse(fromField.getText().trim(), formatter);
                MonthDay to = MonthDay.parse(toField.getText().trim(), formatter);

                List<Map.Entry<Integer, Expense>> filtered = 
                    ExpenseManager.getExpensesByDateRange(from, to);

                tableModel.setRowCount(0);
                for (Map.Entry<Integer, Expense> entry : filtered) {
                    Expense exp = entry.getValue();
                    tableModel.addRow(new Object[]{
                        entry.getKey(),
                        exp.getAmount(),
                        exp.getDescription(),
                        exp.getCategory(),
                        exp.getDate()
                    });
                }

                JOptionPane.showMessageDialog(dialog, 
                    "Found " + filtered.size() + " expenses in date range", 
                    "Filter Results", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Invalid date format! Use DD/MM", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(filterBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    private void showCategoryWiseSummary() {
        Map<String, Integer> categoryTotals = ExpenseManager.getCategoryWiseExpenses();
        
        StringBuilder message = new StringBuilder("<html><body style='width: 300px; font-family: Segoe UI;'>");
        message.append("<h2 style='color: #2980b9;'>Category-wise Summary</h2>");
        message.append("<table style='width: 100%; border-collapse: collapse;'>");
        
        for (Map.Entry<String, Integer> entry : categoryTotals.entrySet()) {
            message.append("<tr style='border-bottom: 1px solid #ddd;'>");
            message.append("<td style='padding: 8px;'><b>").append(entry.getKey()).append("</b></td>");
            message.append("<td style='padding: 8px; text-align: right;'>₹").append(entry.getValue()).append("</td>");
            message.append("</tr>");
        }
        
        message.append("</table></body></html>");
        
        JOptionPane.showMessageDialog(this, message.toString(), 
            "Category Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        Map<Integer, Expense> expenses = ExpenseManager.getAllExpenses();
        
        for (Map.Entry<Integer, Expense> entry : expenses.entrySet()) {
            Expense exp = entry.getValue();
            tableModel.addRow(new Object[]{
                entry.getKey(),
                exp.getAmount(),
                exp.getDescription(),
                exp.getCategory(),
                exp.getDate()
            });
        }

        totalLabel.setText("Total: ₹" + ExpenseManager.getTotalExpenses());
        countLabel.setText("Entries: " + expenses.size());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new ExpenseTrackerGUI());
    }
}