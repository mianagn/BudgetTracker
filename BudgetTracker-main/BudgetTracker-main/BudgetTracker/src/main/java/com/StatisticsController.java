package com;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class StatisticsController {

    public Pane headerContainer;
    public Pane midContainer;
    public PieChart incomePieChart;
    public PieChart expensePieChart;
    public Label netSpendLabel;

    public Label netSpendText;
    public Label dateTime;
    public Label currentMonthLabel;
    public Button prevMonthButton;
    public Button nextMonthButton;

    @FXML private Button HomeButton;
    @FXML private Button StatisticsButton;
    @FXML private Button HistoryButton;


    private List<String> availableMonths = new ArrayList<>();
    private int currentMonthIndex = 0;

    public void initialize() {
        loadAvailableMonths();
        updateMonthNavigationControls();
        loadDataForCurrentMonth();
        DateTimeUpdater.start(dateTime);
        incomePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
        expensePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
    }

    private void loadAvailableMonths() {

        Map<String, Double> monthlyNet = SQLiteDatabase.getMonthlyNetTotals();
        availableMonths = new ArrayList<>(monthlyNet.keySet());


        availableMonths.sort((m1, m2) -> m2.compareTo(m1));


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
        }
    }

    @FXML
    public void handleNextMonthClick() {
        if (currentMonthIndex > 0) {
            currentMonthIndex--;
            updateMonthNavigationControls();
            loadDataForCurrentMonth();
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

    private void loadCategoryPieChartsForMonth(String month) {
        // Income pie chart
        Map<String, Double> incomeCategories = SQLiteDatabase.getCategoryTotalsForMonth(true, month);
        incomePieChart.getData().clear();
        if (!incomeCategories.isEmpty()) {
            incomeCategories.forEach((category, amount) -> {
                incomePieChart.getData().add(new PieChart.Data(
                        category + " (" + String.format("%.2f€", amount) + ")",
                        amount
                ));
            });
            incomePieChart.setTitle("Income Breakdown");
            incomePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
            setPieChartLabelTextColor(incomePieChart, "white");
        } else {
            incomePieChart.setTitle("No Income Data");
            expensePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));

        }


        Map<String, Double> expenseCategories = SQLiteDatabase.getCategoryTotalsForMonth(false, month);
        expensePieChart.getData().clear();
        if (!expenseCategories.isEmpty()) {
            expenseCategories.forEach((category, amount) -> {
                expensePieChart.getData().add(new PieChart.Data(
                        category + " (" + String.format("%.2f€", amount) + ")",
                        amount
                ));
            });
            expensePieChart.setTitle("Expense Breakdown");
            incomePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
            setPieChartLabelTextColor(expensePieChart, "white");

        } else {
            expensePieChart.setTitle("No Expense Data");
            incomePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
        }
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