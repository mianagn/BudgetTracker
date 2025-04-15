package com;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDatabase {
    private static final String DB_URL = "jdbc:sqlite:budgettracker.db";

    // Initialize database with required tables
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create transactions table if it doesn't exist
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS transactions (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "category TEXT NOT NULL," +
                            "amount REAL NOT NULL," +
                            "date TEXT NOT NULL," +
                            "type TEXT NOT NULL" + // 'income' or 'expense'
                            ")"
            );

            // Create balance table if it doesn't exist
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS balance (" +
                            "id INTEGER PRIMARY KEY CHECK (id = 1)," +
                            "balance REAL NOT NULL" +
                            ")"
            );

            // Check if balance record exists, if not insert initial balance of 0
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM balance");
            if (rs.next() && rs.getInt("count") == 0) {
                stmt.execute("INSERT INTO balance (id, balance) VALUES (1, 0.0)");
            }

            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Connect to SQLite database
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Insert transaction into the database
    public static boolean insertTransaction(String category, double amount, String date, boolean isIncome) {
        String type = isIncome ? "income" : "expense";
        String query = "INSERT INTO transactions (category, amount, date, type) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, category);
            stmt.setDouble(2, Math.abs(amount)); // Store positive values
            stmt.setString(3, date);
            stmt.setString(4, type);

            int rowsAffected = stmt.executeUpdate();

            // Update balance
            if (rowsAffected > 0) {
                updateBalanceForTransaction(Math.abs(amount), isIncome);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error inserting transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get current balance
    public static double getCurrentBalance() {
        double balance = 0.0;
        String query = "SELECT balance FROM balance WHERE id = 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                balance = rs.getDouble("balance");
            }
        } catch (SQLException e) {
            System.err.println("Error getting current balance: " + e.getMessage());
            e.printStackTrace();
        }
        return balance;
    }

    // Update balance
    public static boolean updateBalance(double newBalance) {
        String query = "UPDATE balance SET balance = ? WHERE id = 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, newBalance);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating balance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to update balance based on transaction
    private static void updateBalanceForTransaction(double amount, boolean isIncome) {
        double currentBalance = getCurrentBalance();
        double newBalance = isIncome ? currentBalance + amount : currentBalance - amount;
        updateBalance(newBalance);
    }

    // Get all transactions
    public static List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT category, amount, date, type FROM transactions ORDER BY date DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String category = rs.getString("category");
                double amount = rs.getDouble("amount");
                String date = rs.getString("date");
                String type = rs.getString("type");

                // For expense transactions, make amount negative for display
                if ("expense".equals(type)) {
                    amount = -amount;
                }

                transactions.add(new Transaction(category, amount, date));
            }
        } catch (SQLException e) {
            System.err.println("Error getting transactions: " + e.getMessage());
            e.printStackTrace();
        }

        return transactions;
    }

    // Optional: Method to get transactions by type (income or expense)
    public static List<Transaction> getTransactionsByType(boolean isIncome) {
        List<Transaction> transactions = new ArrayList<>();
        String type = isIncome ? "income" : "expense";
        String query = "SELECT category, amount, date FROM transactions WHERE type = ? ORDER BY date DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, type);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("category");
                    double amount = rs.getDouble("amount");
                    String date = rs.getString("date");

                    // For expense transactions, make amount negative for display
                    if (!isIncome) {
                        amount = -amount;
                    }

                    transactions.add(new Transaction(category, amount, date));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting transactions by type: " + e.getMessage());
            e.printStackTrace();
        }

        return transactions;
    }

    // Delete a transaction by ID (useful for future functionality)
    public static boolean deleteTransaction(int id) {
        // First get the transaction details to adjust the balance
        String getQuery = "SELECT amount, type FROM transactions WHERE id = ?";
        String deleteQuery = "DELETE FROM transactions WHERE id = ?";

        try (Connection conn = getConnection()) {
            // Begin transaction
            conn.setAutoCommit(false);

            // Get transaction details
            try (PreparedStatement getStmt = conn.prepareStatement(getQuery)) {
                getStmt.setInt(1, id);

                try (ResultSet rs = getStmt.executeQuery()) {
                    if (rs.next()) {
                        double amount = rs.getDouble("amount");
                        boolean isIncome = "income".equals(rs.getString("type"));

                        // Delete the transaction
                        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                            deleteStmt.setInt(1, id);
                            deleteStmt.executeUpdate();

                            // Adjust the balance (reverse the original transaction effect)
                            double currentBalance = getCurrentBalance();
                            double newBalance = isIncome ?
                                    currentBalance - amount : // Subtract if was income
                                    currentBalance + amount;  // Add back if was expense

                            // Update the balance
                            try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE balance SET balance = ? WHERE id = 1")) {
                                updateStmt.setDouble(1, newBalance);
                                updateStmt.executeUpdate();
                            }
                        }
                    }
                }
            }

            // Commit transaction
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}