package sample.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import sample.model.CartItem;
import sample.model.Medicine;

public class SalesView {

    private final Stage stage;

    private final TextField searchField;
    private final TableView<Medicine> medicineTable;
    private final TextField quantityField;
    private final Label selectedMedicineLabel;
    private final Label unitPriceLabel;

    private final TableView<CartItem> cartTable;
    private final Label totalLabel;

    private final Button searchButton;
    private final Button addToCartButton;
    private final Button checkoutButton;
    private final Button cancelButton;

    public SalesView() {

        stage = new Stage();

        stage.setTitle("Sales");

        stage.initModality(
                Modality.APPLICATION_MODAL
        );

        Label title =
                new Label("Medicine Sales");

        Label searchLabel =
                new Label("Search Medicine");

        searchField =
                new TextField();

        searchField.setPromptText(
                "Search medicine name..."
        );

        searchButton =
                new Button("Search");

        HBox searchBox =
                new HBox(10);

        searchBox.setAlignment(
                Pos.CENTER_LEFT
        );

        searchBox.getChildren().addAll(
                searchLabel,
                searchField,
                searchButton
        );

        medicineTable =
                new TableView<>();

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

        TableColumn<Medicine, Integer> stockColumn =
                new TableColumn<>("Stock");

        stockColumn.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        medicineTable.getColumns().addAll(
                nameColumn,
                priceColumn,
                stockColumn
        );

        nameColumn.setPrefWidth(220);
        priceColumn.setPrefWidth(120);
        stockColumn.setPrefWidth(120);

        medicineTable.setPrefHeight(220);

        selectedMedicineLabel =
                new Label("Selected: None");

        unitPriceLabel =
                new Label("Unit Price: 0.0");

        quantityField =
                new TextField();

        quantityField.setPromptText(
                "Quantity"
        );

        quantityField.setPrefWidth(100);

        addToCartButton =
                new Button("Add to Cart");

        HBox itemBox =
                new HBox(15);

        itemBox.setAlignment(
                Pos.CENTER_LEFT
        );

        itemBox.getChildren().addAll(
                selectedMedicineLabel,
                unitPriceLabel,
                quantityField,
                addToCartButton
        );

        Label cartTitle =
                new Label("Cart");

        cartTable =
                new TableView<>();

        TableColumn<CartItem, String> cartNameColumn =
                new TableColumn<>("Medicine");

        cartNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        TableColumn<CartItem, Integer> cartQuantityColumn =
                new TableColumn<>("Quantity");

        cartQuantityColumn.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        TableColumn<CartItem, Double> cartPriceColumn =
                new TableColumn<>("Unit Price");

        cartPriceColumn.setCellValueFactory(
                new PropertyValueFactory<>("unitPrice")
        );

        TableColumn<CartItem, Double> subtotalColumn =
                new TableColumn<>("Subtotal");

        subtotalColumn.setCellValueFactory(
                new PropertyValueFactory<>("subtotal")
        );

        cartTable.getColumns().addAll(
                cartNameColumn,
                cartQuantityColumn,
                cartPriceColumn,
                subtotalColumn
        );

        cartNameColumn.setPrefWidth(180);
        cartQuantityColumn.setPrefWidth(100);
        cartPriceColumn.setPrefWidth(120);
        subtotalColumn.setPrefWidth(120);

        cartTable.setPrefHeight(180);

        totalLabel =
                new Label("Total: 0.0");

        checkoutButton =
                new Button("Checkout");

        cancelButton =
                new Button("Cancel");

        HBox bottomBox =
                new HBox(15);

        bottomBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        bottomBox.getChildren().addAll(
                totalLabel,
                checkoutButton,
                cancelButton
        );

        VBox root =
                new VBox(15);

        root.setPadding(
                new Insets(25)
        );

        root.getChildren().addAll(
                title,
                searchBox,
                medicineTable,
                itemBox,
                cartTitle,
                cartTable,
                bottomBox
        );

        Scene scene =
                new Scene(
                        root,
                        800,
                        700
                );

        stage.setScene(scene);
    }

    public Stage getStage() {

        return stage;
    }

    public TextField getSearchField() {

        return searchField;
    }

    public Button getSearchButton() {

        return searchButton;
    }

    public TableView<Medicine> getMedicineTable() {

        return medicineTable;
    }

    public TextField getQuantityField() {

        return quantityField;
    }

    public Label getSelectedMedicineLabel() {

        return selectedMedicineLabel;
    }

    public Label getUnitPriceLabel() {

        return unitPriceLabel;
    }

    public Button getAddToCartButton() {

        return addToCartButton;
    }

    public TableView<CartItem> getCartTable() {

        return cartTable;
    }

    public Label getTotalLabel() {

        return totalLabel;
    }

    public Button getCheckoutButton() {

        return checkoutButton;
    }

    public Button getCancelButton() {

        return cancelButton;
    }

    public void show() {

        stage.showAndWait();
    }
}
