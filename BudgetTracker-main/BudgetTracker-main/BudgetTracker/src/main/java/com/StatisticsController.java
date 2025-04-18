package com;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class StatisticsController {

    @FXML private Pane headerContainer;
    @FXML private Pane midContainer;
    @FXML private PieChart incomePieChart;
    @FXML private PieChart expensePieChart;
    @FXML private Label netSpendLabel;
    @FXML private Label netSpendText;
    @FXML private Label currentMonthLabel;
    @FXML private Button prevMonthButton;
    @FXML private Button nextMonthButton;
    @FXML private HBox chartContainer;
    @FXML private BorderPane historyContainer;
    @FXML private Button HomeButton;
    @FXML private Button StatisticsButton;
    @FXML private Button HistoryButton;



    private List<String> availableMonths = new ArrayList<>();
    private int currentMonthIndex = 0;

    public void initialize() {
        loadAvailableMonths();
        updateMonthNavigationControls();
        loadDataForCurrentMonth();
        incomePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
        expensePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
        chartContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (incomePieChart != null && expensePieChart != null) {
                incomePieChart.setPrefWidth(chartContainer.getWidth());
                expensePieChart.setPrefWidth(chartContainer.getWidth());
            }
        });
    }

    private void loadAvailableMonths() {

        Map<String, Double> monthlyNet = SQLiteDatabase.getMonthlyNetTotals();
        availableMonths = new ArrayList<>(monthlyNet.keySet());


        availableMonths.sort(Comparator.reverseOrder());


        currentMonthIndex = 0;
    }
    private void setPieChartLabelTextColor(PieChart chart, String color) {
        for (javafx.scene.Node node : chart.lookupAll(".chart-pie-label")) {
            if (node instanceof javafx.scene.text.Text) {
                ((javafx.scene.text.Text) node).setFill(javafx.scene.paint.Paint.valueOf(color));
            }
        }
    }

    private void updateMonthNavigationControls() {
        if (availableMonths.isEmpty()) {
            currentMonthLabel.setText("No Data");
            netSpendLabel.setText("€0.00");
            prevMonthButton.setDisable(true);
            nextMonthButton.setDisable(true);
            return;
        }


        String currentMonth = availableMonths.get(currentMonthIndex);


        YearMonth ym = YearMonth.parse(currentMonth);
        String formattedMonth = ym.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        currentMonthLabel.setText(formattedMonth);


        prevMonthButton.setDisable(currentMonthIndex >= availableMonths.size() - 1);
        nextMonthButton.setDisable(currentMonthIndex <= 0);
    }

    @FXML
    public void handlePrevMonthClick() {
        if (currentMonthIndex < availableMonths.size() - 1) {
            currentMonthIndex++;
            updateMonthNavigationControls();
            loadDataForCurrentMonth();
            refreshChartsAfterNavigation();
        }
    }

    @FXML
    public void handleNextMonthClick() {
        if (currentMonthIndex > 0) {
            currentMonthIndex--;
            updateMonthNavigationControls();
            loadDataForCurrentMonth();
            refreshChartsAfterNavigation();

        }
    }

    private void loadDataForCurrentMonth() {
        if (availableMonths.isEmpty()) {
            clearAllCharts();
            return;
        }

        String selectedMonth = availableMonths.get(currentMonthIndex);
        loadNetSpendForMonth(selectedMonth);
        loadCategoryPieChartsForMonth(selectedMonth);

    }

    private void loadNetSpendForMonth(String month) {
        Map<String, Double> monthlyNet = SQLiteDatabase.getMonthlyNetTotals();
        Double amount = monthlyNet.get(month);

        if (amount != null) {
            String prefix = amount >= 0 ? "+" : "";
            netSpendLabel.setText(prefix + String.format("%.2f€", amount));
        } else {
            netSpendLabel.setText("€0.00");
        }

    }
    private void refreshChartsAfterNavigation() {
        // Force layout calculations on next pulse
        Platform.runLater(() -> {
            // Force layout calculation
            chartContainer.layout();

            // Schedule another layout update after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    Platform.runLater(() -> {
                        // Get current window size
                        Stage stage = (Stage) chartContainer.getScene().getWindow();
                        double width = stage.getWidth();
                        double height = stage.getHeight();

                        // Force resize
                        stage.setWidth(width + 1);
                        stage.setHeight(height + 1);

                        // Return to original size
                        Platform.runLater(() -> {
                            stage.setWidth(width);
                            stage.setHeight(height);
                        });
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
    private void loadCategoryPieChartsForMonth(String month) {
        // Create new chart instances every time
        incomePieChart = new PieChart();
        expensePieChart = new PieChart();

        // Set styles and properties for the new charts
        incomePieChart.setPrefHeight(350);
        incomePieChart.setPrefWidth(500);
        expensePieChart.setPrefHeight(348);
        expensePieChart.setPrefWidth(1358);
        incomePieChart.setStyle("-fx-background-color: transparent;");
        expensePieChart.setStyle("-fx-background-color: transparent;");

        // Load income data
        Map<String, Double> incomeCategories = SQLiteDatabase.getCategoryTotalsForMonth(true, month);
        if (!incomeCategories.isEmpty()) {
            incomeCategories.forEach((category, amount) -> {
                incomePieChart.getData().add(new PieChart.Data(
                        category + " (" + String.format("%.2f€", amount) + ")",
                        amount
                ));
            });
            incomePieChart.setTitle("Income Breakdown");
        } else {
            incomePieChart.setTitle("No Income Data");
        }

        // Load expense data
        Map<String, Double> expenseCategories = SQLiteDatabase.getCategoryTotalsForMonth(false, month);
        if (!expenseCategories.isEmpty()) {
            expenseCategories.forEach((category, amount) -> {
                expensePieChart.getData().add(new PieChart.Data(
                        category + " (" + String.format("%.2f€", amount) + ")",
                        amount
                ));
            });
            expensePieChart.setTitle("Expense Breakdown");
        } else {
            expensePieChart.setTitle("No Expense Data");
        }

        // Apply text styling
        incomePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
        expensePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
        setPieChartLabelTextColor(incomePieChart, "white");
        setPieChartLabelTextColor(expensePieChart, "white");

        // Update the view
        Platform.runLater(() -> {
            chartContainer.getChildren().clear();
            chartContainer.getChildren().addAll(expensePieChart, incomePieChart);

            // Force layout pass
            chartContainer.layout();

            // Schedule another layout pass after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    Platform.runLater(() -> chartContainer.layout());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
    private void clearAllCharts() {
        incomePieChart.getData().clear();
        expensePieChart.getData().clear();
        incomePieChart.setTitle("No Income Data");
        expensePieChart.setTitle("No Expense Data");
        netSpendLabel.setText("€0.00");
        incomePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
        expensePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
    }


    @FXML
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


    @FXML
    private void handleStatisticsButtonClick() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/statistics-view.fxml"));
            Scene statisticsScene = new Scene(loader.load());


            Stage stage = (Stage) StatisticsButton.getScene().getWindow();
            stage.setScene(statisticsScene);


            Button statisticsHomeButton = (Button) statisticsScene.lookup("#HomeButton");
            statisticsHomeButton.setDisable(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHistoryButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/history-view.fxml"));
            Scene historyScene = new Scene(loader.load());
            Stage stage = (Stage) HistoryButton.getScene().getWindow();
            stage.setScene(historyScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}