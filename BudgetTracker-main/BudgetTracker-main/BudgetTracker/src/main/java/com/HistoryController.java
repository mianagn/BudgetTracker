package com;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HistoryController {
    public Button StatisticsButton;
    public Button HomeButton;
    @FXML private TableView<Transaction> allTransactionsTable;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private LineChart<String, Number> balanceTrendChart;
    @FXML public Label dateTime;

    public void initialize() {
        setupTransactionTable();
        setupBalanceTrendChart();
        DateTimeUpdater.start(dateTime);
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

    @FXML
    private void handleStatisticsButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/statistics-view.fxml"));
            Scene statisticsScene = new Scene(loader.load());
            Stage stage = (Stage) StatisticsButton.getScene().getWindow();
            stage.setScene(statisticsScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void handleHomeButtonClick() {
        try {
            // Load Home view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Home-view.fxml"));
            Parent homeView = loader.load();

            // Get the current stage and set the new scene
            Stage stage = (Stage) HomeButton.getScene().getWindow();
            stage.setScene(new Scene(homeView));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}