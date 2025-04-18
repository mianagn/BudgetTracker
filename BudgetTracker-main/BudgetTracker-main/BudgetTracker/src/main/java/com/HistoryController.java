package com;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.*;

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
    private TableColumn<Transaction, Void> deleteColumn;
    @FXML
    private LineChart<String, Number> balanceTrendChart;


    public void initialize() {
        setupTransactionTable();
        setupBalanceTrendChart();


        addDeleteButtonToTable();

        balanceTrendChart.lookupAll(".axis-label").forEach(node ->
                node.setStyle("-fx-text-fill: white;"));

        balanceTrendChart.lookup(".chart-title").setStyle("-fx-text-fill: white;");

        // Style the transaction table
        styleTransactionTable();
    }

    private void styleTransactionTable() {
        String tableStyle =
                "-fx-background-color: rgba(255, 255, 255, 0.1);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;";

        String headerStyle =
                "-fx-background-color: rgba(0, 0, 0, 0.5);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;";


        allTransactionsTable.setStyle(tableStyle);


        for (TableColumn<?, ?> column : allTransactionsTable.getColumns()) {
            column.setStyle(headerStyle);
        }


        Scene scene = allTransactionsTable.getScene();
        if (scene != null) {
            scene.getStylesheets().add(getClass().getResource("/com/styles.css").toExternalForm());
        } else {
            allTransactionsTable.sceneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    newValue.getStylesheets().add(getClass().getResource("/com/styles.css").toExternalForm());
                }
            });
        }
    }

    private void setupTransactionTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // Format the amount column to show currency
        amountColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    // Format with currency symbol and color based on positive/negative
                    String formattedAmount = String.format("%.2f €", amount);
                    setText(formattedAmount);

                    if (amount < 0) {
                        setStyle("-fx-text-fill: #ff6666;"); // Red for negative amounts
                    } else {
                        setStyle("-fx-text-fill: #66ff66;"); // Green for positive amounts
                    }
                }
            }
        });

        refreshTransactionTable();
    }

    private void refreshTransactionTable() {
        List<Transaction> allTransactions = SQLiteDatabase.getAllTransactions();
        allTransactions.sort(Comparator.comparing(Transaction::getDate)); // Sort by date descending
        ObservableList<Transaction> observableTransactions = FXCollections.observableArrayList(allTransactions);
        allTransactionsTable.setItems(observableTransactions);
    }

    private void setupBalanceTrendChart() {
        balanceTrendChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Balance Over Time");

        List<Transaction> allTransactions = SQLiteDatabase.getAllTransactions();
        allTransactions.sort(Comparator.comparing(Transaction::getDate)); // Sort by date descending
        double runningBalance = 0;


        for (int i = 0; i < allTransactions.size(); i++) {
            Transaction t = allTransactions.get(i);
            runningBalance += t.getAmount();
            String label = t.getDate() + " #" + (i + 1);
            series.getData().add(new XYChart.Data<>(label, runningBalance));
        }

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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Home-view.fxml"));
            Parent homeView = loader.load();


            Stage stage = (Stage) HomeButton.getScene().getWindow();
            stage.setScene(new Scene(homeView));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addDeleteButtonToTable() {
        deleteColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("X");
            private final HBox centeredBox = new HBox(deleteButton);

            {
                // Button styling
                deleteButton.setStyle(
                        "-fx-background-color: #ff3333; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 5; " +
                                "-fx-background-radius: 3;"
                );

                // Center the HBox
                centeredBox.setAlignment(Pos.CENTER);

                // Action handler
                deleteButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    int transactionId = transaction.getId();

                    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmDialog.setTitle("Delete Transaction");
                    confirmDialog.setHeaderText("Delete Transaction");
                    confirmDialog.setContentText("Are you sure you want to delete this transaction?");

                    DialogPane dialogPane = confirmDialog.getDialogPane();
                    dialogPane.getStylesheets().add(getClass().getResource("/com/styles.css").toExternalForm());

                    Optional<ButtonType> result = confirmDialog.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        boolean success = SQLiteDatabase.deleteTransaction(transactionId);
                        if (success) {
                            refreshTransactionTable();
                            setupBalanceTrendChart();

                            Alert successNotification = new Alert(Alert.AlertType.INFORMATION);
                            successNotification.setTitle("Success");
                            successNotification.setHeaderText(null);
                            successNotification.setContentText("Transaction deleted successfully!");
                            successNotification.showAndWait();
                        } else {
                            Alert errorNotification = new Alert(Alert.AlertType.ERROR);
                            errorNotification.setTitle("Error");
                            errorNotification.setHeaderText(null);
                            errorNotification.setContentText("Failed to delete transaction. Please try again.");
                            errorNotification.showAndWait();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(centeredBox);
                }
            }
        });
    }

}
