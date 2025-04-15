package com;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HistoryController {
    @FXML private TableView<Transaction> allTransactionsTable;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private LineChart<String, Number> balanceTrendChart;

    public void initialize() {
        setupTransactionTable();
        setupBalanceTrendChart();
    }

    private void setupTransactionTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        List<Transaction> allTransactions = SQLiteDatabase.getAllTransactions();
        ObservableList<Transaction> observableTransactions = FXCollections.observableArrayList(allTransactions);
        allTransactionsTable.setItems(observableTransactions);
    }

    private void setupBalanceTrendChart() {
        balanceTrendChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Balance Over Time");

        List<Transaction> transactions = SQLiteDatabase.getAllTransactions();
        Map<String, Double> dailyBalances = new TreeMap<>();
        double runningBalance = 0;

        // Process transactions in chronological order
        for (Transaction t : transactions) {
            runningBalance += t.getAmount();
            dailyBalances.put(t.getDate(), runningBalance);
        }

        // Add data points to chart
        dailyBalances.forEach((date, balance) -> {
            series.getData().add(new XYChart.Data<>(date, balance));
        });

        balanceTrendChart.getData().add(series);
    }
}