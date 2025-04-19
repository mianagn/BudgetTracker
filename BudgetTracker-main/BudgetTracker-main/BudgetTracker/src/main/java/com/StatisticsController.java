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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;

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
    @FXML private PieChart incomePieChartVertical;
    @FXML private PieChart expensePieChartVertical;
    @FXML private Label netSpendLabel;
    @FXML private Label netSpendText;
    @FXML private Label currentMonthLabel;
    @FXML private Button prevMonthButton;
    @FXML private Button nextMonthButton;
    @FXML private HBox chartHorizontalContainer;
    @FXML private VBox chartVerticalContainer;
    @FXML private StackPane chartsStackPane;
    @FXML private BorderPane historyContainer;
    @FXML private Button HomeButton;
    @FXML private Button StatisticsButton;
    @FXML private Button HistoryButton;

    private List<String> availableMonths = new ArrayList<>();
    private int currentMonthIndex = 0;
    private boolean isVerticalLayoutActive = false;
    private static final double SMALL_SCREEN_THRESHOLD = 900;
    private static final double MEDIUM_SCREEN_THRESHOLD = 1100;

    public void initialize() {
        loadAvailableMonths();
        updateMonthNavigationControls();
        setupResponsiveCharts();
        loadDataForCurrentMonth();

        // Apply styling to chart titles
        applyChartTitleStyling();

        // Add listener to window width for responsive design
        historyContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            resizeCharts(newVal.doubleValue());
        });
    }

    private void applyChartTitleStyling() {
        if (incomePieChart != null) {
            incomePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
        }
        if (expensePieChart != null) {
            expensePieChart.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
        }
        if (incomePieChartVertical != null) {
            incomePieChartVertical.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
        }
        if (expensePieChartVertical != null) {
            expensePieChartVertical.lookupAll(".chart-title").forEach(node -> node.setStyle("-fx-text-fill: white;"));
        }
    }

    private void resizeCharts(double width) {
        if (width <= SMALL_SCREEN_THRESHOLD) {
            // Small screen - move legends to bottom for better space usage
            if (incomePieChart != null) incomePieChart.setLegendSide(Side.BOTTOM);
            if (expensePieChart != null) expensePieChart.setLegendSide(Side.BOTTOM);
            if (incomePieChartVertical != null) incomePieChartVertical.setLegendSide(Side.BOTTOM);
            if (expensePieChartVertical != null) expensePieChartVertical.setLegendSide(Side.BOTTOM);
        } else {
            // For medium and large screens - move legends to bottom (changed from RIGHT)
            if (incomePieChart != null) incomePieChart.setLegendSide(Side.BOTTOM);
            if (expensePieChart != null) expensePieChart.setLegendSide(Side.BOTTOM);
            if (incomePieChartVertical != null) incomePieChartVertical.setLegendSide(Side.BOTTOM);
            if (expensePieChartVertical != null) expensePieChartVertical.setLegendSide(Side.BOTTOM);
        }
    }

    private void setupResponsiveCharts() {
        // Monitor window width for responsive layout changes
        chartsStackPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double width = newValue.doubleValue();

                // Toggle between horizontal and vertical layouts based on width
                boolean shouldBeVertical = width < SMALL_SCREEN_THRESHOLD;
                if (shouldBeVertical != isVerticalLayoutActive) {
                    isVerticalLayoutActive = shouldBeVertical;
                    updateLayoutVisibility();
                }

                // Update chart legend positions based on width
                resizeCharts(width);
            }
        });

        // Initial layout setup
        updateLayoutVisibility();
    }

    private void updateLayoutVisibility() {
        if (chartHorizontalContainer != null && chartVerticalContainer != null) {
            chartHorizontalContainer.setVisible(!isVerticalLayoutActive);
            chartHorizontalContainer.setManaged(!isVerticalLayoutActive);
            chartVerticalContainer.setVisible(isVerticalLayoutActive);
            chartVerticalContainer.setManaged(isVerticalLayoutActive);
        }
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
        // Create and configure pie charts
        PieChart newExpensePieChart = createExpensePieChart(month);
        PieChart newIncomePieChart = createIncomePieChart(month);
        PieChart newExpensePieChartVertical = createExpensePieChart(month);
        PieChart newIncomePieChartVertical = createIncomePieChart(month);

        // Configure chart size properties
        configureChartSize(newExpensePieChart, false);
        configureChartSize(newIncomePieChart, false);
        configureChartSize(newExpensePieChartVertical, true);
        configureChartSize(newIncomePieChartVertical, true);

        // Update UI on JavaFX thread
        Platform.runLater(() -> {
            // Clear and update horizontal container
            if (chartHorizontalContainer != null) {
                chartHorizontalContainer.getChildren().clear();

                VBox expenseBox = createChartContainer(newExpensePieChart);
                VBox incomeBox = createChartContainer(newIncomePieChart);

                // Add equal growth to both containers
                HBox.setHgrow(expenseBox, Priority.ALWAYS);
                HBox.setHgrow(incomeBox, Priority.ALWAYS);

                chartHorizontalContainer.getChildren().addAll(expenseBox, incomeBox);
            }

            // Clear and update vertical container
            if (chartVerticalContainer != null) {
                chartVerticalContainer.getChildren().clear();

                VBox expenseBoxVert = createChartContainer(newExpensePieChartVertical);
                VBox incomeBoxVert = createChartContainer(newIncomePieChartVertical);

                // Add equal growth to both containers
                VBox.setVgrow(expenseBoxVert, Priority.ALWAYS);
                VBox.setVgrow(incomeBoxVert, Priority.ALWAYS);

                chartVerticalContainer.getChildren().addAll(expenseBoxVert, incomeBoxVert);
            }

            // Update references to the new charts
            this.expensePieChart = newExpensePieChart;
            this.incomePieChart = newIncomePieChart;
            this.expensePieChartVertical = newExpensePieChartVertical;
            this.incomePieChartVertical = newIncomePieChartVertical;

            // Apply styling and adapt to current window size
            applyChartTitleStyling();
            resizeCharts(historyContainer.getWidth());

            // Force layout pass
            chartsStackPane.layout();
        });
    }

    private VBox createChartContainer(PieChart chart) {
        VBox container = new VBox();
        container.getStyleClass().addAll("card", "chart-card");
        container.setAlignment(javafx.geometry.Pos.CENTER);
        container.getChildren().add(chart);

        // Make the chart fill the available space
        VBox.setVgrow(chart, Priority.ALWAYS);

        // Center chart and let it grow to fill space
        chart.setMaxWidth(Double.MAX_VALUE);
        chart.setMaxHeight(Double.MAX_VALUE);

        // Explicitly set fill width to true for the container
        container.setFillWidth(true);

        return container;
    }
    // Also update the configureChartSize method to ensure consistency
    private void configureChartSize(PieChart chart, boolean isVertical) {
        // Set growth priority for the chart to fill available space
        chart.setMaxWidth(Double.MAX_VALUE);
        chart.setMaxHeight(Double.MAX_VALUE);

        // Center the chart in its space
        chart.setMinSize(350, 350);

        if (isVertical) {
            // Vertical layout - slightly smaller charts
            chart.setPrefSize(400, 400);
        } else {
            // Horizontal layout - larger charts
            chart.setPrefSize(500, 500);
        }

        // Improve chart responsiveness
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setLabelLineLength(20);

        // Always set legend to BOTTOM regardless of screen size
        chart.setLegendSide(Side.BOTTOM);
    }

    private PieChart createExpensePieChart(String month) {
        PieChart chart = new PieChart();
        chart.setStyle("-fx-background-color: transparent;");
        chart.setAnimated(false);
        chart.setLegendVisible(true);

        // Make chart responsive
        chart.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        chart.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        chart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Load expense data
        Map<String, Double> expenseCategories = SQLiteDatabase.getCategoryTotalsForMonth(false, month);
        if (!expenseCategories.isEmpty()) {
            // Format labels with shorter text to avoid layout issues
            expenseCategories.forEach((category, amount) -> {
                String label = formatCategoryLabel(category, amount);
                chart.getData().add(new PieChart.Data(label, amount));
            });
            chart.setTitle("Expense Breakdown");
        } else {
            chart.setTitle("No Expense Data");
        }

        setPieChartLabelTextColor(chart, "white");
        return chart;
    }

    private PieChart createIncomePieChart(String month) {
        PieChart chart = new PieChart();
        chart.setStyle("-fx-background-color: transparent;");
        chart.setAnimated(false);
        chart.setLegendVisible(true);

        // Make chart responsive
        chart.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        chart.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        chart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Load income data
        Map<String, Double> incomeCategories = SQLiteDatabase.getCategoryTotalsForMonth(true, month);
        if (!incomeCategories.isEmpty()) {
            // Format labels with shorter text to avoid layout issues
            incomeCategories.forEach((category, amount) -> {
                String label = formatCategoryLabel(category, amount);
                chart.getData().add(new PieChart.Data(label, amount));
            });
            chart.setTitle("Income Breakdown");
        } else {
            chart.setTitle("No Income Data");
        }

        setPieChartLabelTextColor(chart, "white");
        return chart;
    }

    // Format category labels to be shorter but still informative
    private String formatCategoryLabel(String category, double amount) {
        // Shorten category name if too long
        String shortCategory = category.length() > 15 ?
                category.substring(0, 12) + "..." :
                category;

        return shortCategory + "\n" + String.format("%.2f€", amount);
    }

    private void clearAllCharts() {
        if (incomePieChart != null) incomePieChart.getData().clear();
        if (expensePieChart != null) expensePieChart.getData().clear();
        if (incomePieChartVertical != null) incomePieChartVertical.getData().clear();
        if (expensePieChartVertical != null) expensePieChartVertical.getData().clear();

        if (incomePieChart != null) incomePieChart.setTitle("No Income Data");
        if (expensePieChart != null) expensePieChart.setTitle("No Expense Data");
        if (incomePieChartVertical != null) incomePieChartVertical.setTitle("No Income Data");
        if (expensePieChartVertical != null) expensePieChartVertical.setTitle("No Expense Data");

        netSpendLabel.setText("€0.00");
        applyChartTitleStyling();
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