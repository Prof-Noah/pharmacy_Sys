package sample.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;

import sample.model.Medicine;

import java.util.List;

public class ReportsView {
    private final BorderPane root;
    private Label totalMedicinesValue;
    private Label totalUnitsValue;
    private Label lowStockValue;
    private Label outOfStockValue;
    private Label inventoryValue;

    private PieChart stockChart;
    private BarChart<String, Number> quantityChart;
    private TableView<Medicine> reportTable;
    private Label insightsLabel;

    public ReportsView() {
        root = new BorderPane();
        createLayout();
    }
    // Main Layout
    private void createLayout() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        Label title = new Label("Inventory Reports");
        title.setStyle("-fx-font-size: 25px;" + "-fx-font-weight: bold;");
        Label subtitle = new Label("Pharmacy inventory analytics and insghts");
        VBox heading = new VBox(5);
        heading.getChildren().addAll(title, subtitle);
        HBox cards = createStatisticsCards();
        HBox charts = createChartsSection();
        Label tableTitle = new Label("Detailed Inventory Report");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        reportTable = createReportTable();
        Label insightsTitle = new Label("Business Insights");
        insightsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        insightsLabel = new Label();
        insightsLabel.setWrapText(true);
        insightsLabel.setStyle("-fx-font-size: 18px;" + "-fx-padding: 15px;" + "-fx-background-color: #eef7f1;" + "-fx-background-radius: 8px;");
        VBox insightsBox = new VBox(10);
        insightsBox.getChildren().addAll(insightsTitle, insightsLabel);
        content.getChildren().addAll(heading,cards,charts,insightsBox,tableTitle,reportTable);
        root.setCenter(content);

    }
    // KPI Cards
    private HBox createStatisticsCards() {
        totalMedicinesValue = new Label("0");
        totalUnitsValue = new Label("0");
        lowStockValue = new Label("0");
        outOfStockValue = new Label("0");
        HBox cards = new HBox(15);
        cards.getChildren().addAll(
                createCard("Total Medicines", totalMedicinesValue),
                createCard("Total Stock Units", totalUnitsValue),
                createCard("Low Stock", lowStockValue),
                createCard("Out of Stock", outOfStockValue)
        );
        return cards;
    }
    private VBox createCard(String title, Label value) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #666666;"
        );
        value.setStyle(
                "-fx-font-size: 25px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #167d55;"
        );
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMinWidth(150);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #dddddd;" +
                        "-fx-border-radius: 10;"
        );
        card.getChildren().addAll(titleLabel,value);
        return card;
    }
    // Charts
    private HBox createChartsSection() {
        stockChart = new PieChart();
        stockChart.setTitle("Stock Status Distribution");
        stockChart.setPrefSize(350, 280);
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Medicine");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Quantity");
        quantityChart = new BarChart<>(xAxis, yAxis);
        quantityChart.setTitle("Medicine Quantity Analysis");
        quantityChart.setLegendVisible(false);
        quantityChart.setPrefSize(550, 280);
        HBox charts = new HBox(20);
        charts.getChildren().addAll(stockChart,quantityChart);
        return charts;
    }
    // Report Table
    private TableView<Medicine> createReportTable() {
        TableView<Medicine> table = new TableView<>();
        TableColumn<Medicine, String> nameColumn =
                new TableColumn<>("Medicine");
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );
        TableColumn<Medicine, Double> priceColumn =
                new TableColumn<>("Price");
        priceColumn.setCellValueFactory(
                new PropertyValueFactory<>("price")
        );
        TableColumn<Medicine, Number> quantityColumn =
                new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(
                cell -> new ReadOnlyDoubleWrapper(
                        cell.getValue().getQuantity()
                )
        );
        TableColumn<Medicine, Number> valueColumn =
                new TableColumn<>("Stock Value");
        valueColumn.setCellValueFactory(
                cell -> new ReadOnlyDoubleWrapper(
                        cell.getValue().getPrice()
                                * cell.getValue().getQuantity()
                )
        );
        TableColumn<Medicine, String> statusColumn =
                new TableColumn<>("Status");
        statusColumn.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(
                        getStatus(cell.getValue().getQuantity())
                )
        );
        table.getColumns().addAll(
                nameColumn,
                priceColumn,
                quantityColumn,
                valueColumn,
                statusColumn
        );
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        table.setPrefHeight(250);
        return table;
    }
    // Update Report Data
    public void updateReport(List<Medicine> medicines) {
        int totalMedicines = medicines.size();
        int totalUnits = 0;
        int lowStock = 0;
        int outOfStock = 0;
        double totalValue = 0;
        int available = 0;
        for (Medicine medicine : medicines) {
            int quantity = medicine.getQuantity();
            totalUnits += quantity;
            totalValue +=
                    medicine.getPrice() * quantity;
            if (quantity == 0) {
                outOfStock++;
            } else if (quantity <= 10) {
                lowStock++;
            } else {
                available++;
            }
        }
        totalMedicinesValue.setText(String.valueOf(totalMedicines));
        totalUnitsValue.setText(String.valueOf(totalUnits));
        lowStockValue.setText(String.valueOf(lowStock));
        outOfStockValue.setText(String.valueOf(outOfStock));
        inventoryValue = new Label();

        // Update Charts
        stockChart.setData(
                FXCollections.observableArrayList(
                        new PieChart.Data("Available", available),
                        new PieChart.Data("Low Stock", lowStock),
                        new PieChart.Data("Out of Stock", outOfStock)
                )
        );
        javafx.scene.chart.XYChart.Series<String, Number> series =
                new javafx.scene.chart.XYChart.Series<>();
        for (Medicine medicine : medicines) {
            series.getData().add(
                    new javafx.scene.chart.XYChart.Data<>(
                            medicine.getName(),
                            medicine.getQuantity()
                    )
            );
        }
        quantityChart.getData().clear();
        quantityChart.getData().add(series);

        // Update Table
        reportTable.setItems(FXCollections.observableArrayList(medicines));

        // Update Insights
        insightsLabel.setText(
                "Total inventory value: "
                        + String.format("%.2f", totalValue)
                        + " EGP\n"
                        + "Available medicines: " + available + "\n"
                        + "Medicines requiring restocking: " + lowStock + "\n"
                        + "Out of stock medicines: " + outOfStock
        );
    }
    // Medicines Status
    private String getStatus(int quantity) {
        if(quantity == 0) { return "Out of stock"; }
        if(quantity <= 10) { return "Low Stock"; }
        return "Available medicines: " + quantity;
    }

    // Get Root
    public BorderPane getRoot() { return root; }
}
