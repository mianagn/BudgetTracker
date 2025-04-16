package com;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    @FXML
    private TableView<Transaction> allTransactionsTable;
    @FXML
    private TableColumn<Transaction, String> dateColumn;
    @FXML
    private TableColumn<Transaction, String> categoryColumn;
    @FXML
    private TableColumn<Transaction, Double> amountColumn;
    @FXML
    private LineChart<String, Number> balanceTrendChart;
    @FXML
    public Label dateTime;

    public void initialize() {
        setupTransactionTable();
        setupBalanceTrendChart();
        DateTimeUpdater.start(dateTime);

        // Style the chart axis labels to be white
        balanceTrendChart.lookupAll(".axis-label").forEach(node ->
                node.setStyle("-fx-text-fill: white;"));

        // You can also style the chart title if needed
        balanceTrendChart.lookup(".chart-title").setStyle("-fx-text-fill: white;");

        // Style the transaction table
        styleTransactionTable();
    }

    private void styleTransactionTable() {
        // Apply CSS styling to the table
        String tableStyle =
                "-fx-background-color: rgba(255, 255, 255, 0.1);" +  // Semi-transparent background
                        "-fx-text-fill: white;" +                            // Text color
                        "-fx-font-size: 14px;";                              // Font size

        String headerStyle =
                "-fx-background-color: rgba(0, 0, 0, 0.5);" +        // Darker header background
                        "-fx-text-fill: white;" +                            // Header text color
                        "-fx-font-weight: bold;";                            // Bold headers

        String cellStyle =
                "-fx-text-fill: white;";                             // Cell text color

        // Apply styles
        allTransactionsTable.setStyle(tableStyle);

        // Style column headers
        for (TableColumn<?, ?> column : allTransactionsTable.getColumns()) {
            column.setStyle(headerStyle);
        }

        // Apply cell styling (this requires a CSS file)
        // Create a file called styles.css in your resources folder with the content from the CSS artifact below
        Scene scene = allTransactionsTable.getScene();
        if (scene != null) {
            scene.getStylesheets().add(getClass().getResource("/com/styles.css").toExternalForm());
        } else {
            // If the table is not yet in a scene, we need to wait
            allTransactionsTable.sceneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    newValue.getStylesheets().add(getClass().getResource("/com/styles.css").toExternalForm());
                }
            });
        }
    }
        private void setupTransactionTable () {
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

            List<Transaction> allTransactions = SQLiteDatabase.getAllTransactions();
            ObservableList<Transaction> observableTransactions = FXCollections.observableArrayList(allTransactions);
            allTransactionsTable.setItems(observableTransactions);
        }

        private void setupBalanceTrendChart () {
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
        private void handleStatisticsButtonClick () {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/statistics-view.fxml"));
                Scene statisticsScene = new Scene(loader.load());
                Stage stage = (Stage) StatisticsButton.getScene().getWindow();
                stage.setScene(statisticsScene);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void handleHomeButtonClick () {
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
