package com;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.Map;

public class StatisticsController {

    public Pane headerContainer;
    public ImageView profilePic;
    public Text welcome;
    public Pane midContainer;
    public PieChart incomePieChart;
    public PieChart expensePieChart;
    public Label netSpendLabel;
    public TableView topSpendingTable;
    public TableColumn categoryCol;
    public TableColumn amountCol;
    public Label netSpendText;
    public Label dateTime;
    @FXML private Button HomeButton;
    @FXML private Button StatisticsButton;
    @FXML private Button HistoryButton;
    public void initialize() {
        loadMonthlyNetSpend();
        loadCategoryPieCharts();
    }
    private void loadMonthlyNetSpend() {
        Map<String, Double> monthlyNet = SQLiteDatabase.getMonthlyNetTotals();
        StringBuilder netText = new StringBuilder();

        monthlyNet.forEach((month, amount) -> {
            String formattedAmount = String.format("%.2f€", amount);
            netText.append(month).append(": ")
                    .append(amount >= 0 ? "+" : "")
                    .append(formattedAmount).append("\n");
        });

        netSpendLabel.setText(netText.toString());
    }

    private void loadCategoryPieCharts() {
        // Income pie chart
        Map<String, Double> incomeCategories = SQLiteDatabase.getCategoryTotals(true);
        incomePieChart.getData().clear();
        incomeCategories.forEach((category, amount) -> {
            incomePieChart.getData().add(new PieChart.Data(
                    category + " (" + String.format("%.2f€", amount) + ")",
                    amount
            ));
        });
        incomePieChart.setTitle("Income Breakdown");

        // Expense pie chart
        Map<String, Double> expenseCategories = SQLiteDatabase.getCategoryTotals(false);
        expensePieChart.getData().clear();
        expenseCategories.forEach((category, amount) -> {
            expensePieChart.getData().add(new PieChart.Data(
                    category + " (" + String.format("%.2f€", amount) + ")",
                    amount
            ));
        });
        expensePieChart.setTitle("Expense Breakdown");
    }
    // Handle click on Home Button to go back to the Home view
    @FXML
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
    // Navigate to the Statistics screen
    @FXML
    private void handleStatisticsButtonClick() {
        try {
            // Disable the current buttons on Home screen


            // Load and display the statistics screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/statistics-view.fxml"));
            Scene statisticsScene = new Scene(loader.load());

            // Set up the statistics window
            Stage stage = (Stage) StatisticsButton.getScene().getWindow();
            stage.setScene(statisticsScene);

            // Enable Home button on the Statistics screen
            Button statisticsHomeButton = (Button) statisticsScene.lookup("#HomeButton"); // This should be the Home button in the Statistics view
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
