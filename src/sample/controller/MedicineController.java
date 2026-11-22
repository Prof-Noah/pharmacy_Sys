package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.collections.ListChangeListener;

import javafx.stage.Stage;
import sample.model.Medicine;
import sample.service.MedicineService;
import sample.view.AddMedicineView;
import sample.view.EditMedicineView;
import sample.view.MedicineView;
import sample.view.ReportsView;

public class MedicineController {

    private final MedicineView view;
    private final MedicineService service;

    private final ObservableList<Medicine>
            allMedicines;

    private Runnable onLogout;

    private Runnable onSettings;

    public MedicineController() {

        view =
                new MedicineView();

        service =
                new MedicineService();

        allMedicines =
                FXCollections.observableArrayList(
                        service.loadMedicines()
                );
        allMedicines.addListener(
                (ListChangeListener<Medicine>) change -> updateStatistics()
        );

        view.getMedicineTable()
                .setItems(allMedicines);
        updateStatistics();
        setupSearch();

        setupSearchShortcuts();

        setupAddButton();

        setupEditButton();

        setupLogoutButton();

        setupSettingsButton();

        setupSalesButton();

        setupHomeButton();

        setupMedicinesButton();

        setupReportsButton();
    }



    // =========================================================
    // SEARCH
    // =========================================================

    private void setupSearchShortcuts() {

        view.getSearchField()
                .setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        view.getSearchField().clear();
                    }
                });

        view.getSearchField()
                .setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        view.getSearchField().clear();
                    }
                });
    }

    private void setupSearch() {

        view.getSearchField()
                .textProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) -> {

                            filterMedicines(
                                    newValue
                            );
                        }
                );
    }

    private void updateStatistics() {

        int total = allMedicines.size();
        int available = 0;
        int outOfStock = 0;

        for (Medicine medicine : allMedicines) {
            if (medicine.getQuantity() > 0) {
                available++;
            } else {
                outOfStock++;
            }
        }

        view.updateStatistics(total, available, outOfStock);
    }

    private void setupHomeButton() {

        view.getHomeButton()
                .setOnAction(event -> {
                    updateStatistics();
                    view.showHome();
                });
    }

    private void setupMedicinesButton() {

        view.getMedicinesButton()
                .setOnAction(event -> view.showMedicines());
    }

    private void filterMedicines(
            String searchText) {

        ObservableList<Medicine>
                filtered =
                FXCollections.observableArrayList();

        String search =
                searchText
                        .toLowerCase()
                        .trim();

        for (Medicine medicine :
                allMedicines) {

            if (medicine.getName()
                    .toLowerCase()
                    .startsWith(search)) {

                filtered.add(medicine);
            }
        }

        view.getMedicineTable()
                .setItems(filtered);
    }

    // =========================================================
    // ADD MEDICINE
    // =========================================================

    private void setupAddButton() {

        view.getAddButton()
                .setOnAction(event -> {

                    addMedicine();
                });
    }

    private void addMedicine() {

        AddMedicineView addView =
                new AddMedicineView();

        addView.getSaveButton()
                .setOnAction(event -> {

                    saveNewMedicine(
                            addView
                    );
                });

        addView.getCancelButton()
                .setOnAction(event -> {

                    addView.getStage()
                            .close();
                });

        addView.show();
    }

    private void saveNewMedicine(
                    AddMedicineView addView) {

                try {

                    String name =
                            addView.getNameField()
                                    .getText()
                                    .trim();

                    String priceText =
                            addView.getPriceField()
                                    .getText()
                                    .trim();

                    String quantityText =
                            addView.getQuantityField()
                                    .getText()
                                    .trim();

                    // -------------------------
                    // Validate Name
                    // -------------------------

                    if (name.isEmpty()) {

                        showAlert(
                                "Invalid Data",
                                "Medicine name cannot be empty."
                        );

                        return;
                    }

                    // -------------------------
                    // Validate Price
                    // -------------------------

                    double price =
                            Double.parseDouble(
                                    priceText
                            );

                    if (price < 0) {

                        showAlert(
                                "Invalid Data",
                                "Price cannot be negative."
                        );

                        return;
                    }

            // -------------------------
            // Validate Quantity
            // -------------------------

            int quantity =
                    Integer.parseInt(
                            quantityText
                    );

            if (quantity < 0) {

                showAlert(
                        "Invalid Data",
                        "Quantity cannot be negative."
                );

                return;
            }

            // -------------------------
            // Create Medicine
            // -------------------------

            Medicine medicine =
                    new Medicine(
                            name,
                            price,
                            quantity
                    );

            // -------------------------
            // Save to File
            // -------------------------

            service.addMedicine(
                    medicine
            );

            // -------------------------
            // Add to ObservableList
            // -------------------------

            allMedicines.add(
                    medicine
            );
            // -------------------------
            // Refresh Table
            // -------------------------

            view.getMedicineTable()
                    .setItems(allMedicines);

            view.getMedicineTable()
                    .refresh();

            // -------------------------
            // Close Window
            // -------------------------

            addView.getStage()
                    .close();

            // -------------------------
            // Success Message
            // -------------------------

            showAlert(
                    "Success",
                    "Medicine added successfully."
            );

        } catch (NumberFormatException e) {

            showAlert(
                    "Invalid Data",
                    "Price must be a number and quantity must be an integer."
            );
        }
    }

    // =========================================================
    // EDIT MEDICINE
    // =========================================================

    private void setupEditButton() {

        view.getEditButton()
                .setOnAction(event -> {

                    editSelectedMedicine();
                });
    }

    private void setupLogoutButton() {

        view.getLogoutButton()
                .setOnAction(event -> {
                    if (onLogout != null) {
                        onLogout.run();
                    }
                });
    }

    public void setOnLogout(Runnable onLogout) {

        this.onLogout = onLogout;
    }

    private void setupSettingsButton() {

        view.getSettingsButton()
                .setOnAction(event -> {
                    if (onSettings != null) {
                        onSettings.run();
                    }
                });
    }

    public void setOnSettings(Runnable onSettings) {

        this.onSettings = onSettings;
    }

    // =========================================================
    // SALES
    // =========================================================

    private void setupSalesButton() {

        view.getSalesButton()
                .setOnAction(event -> {

                    SalesController salesController =
                            new SalesController(
                                    allMedicines,
                                    service
                            );

                    salesController.show();
                });
    }

    private void editSelectedMedicine() {

        Medicine selectedMedicine =
                view.getMedicineTable()
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedMedicine == null) {

            showAlert(
                    "No Medicine Selected",
                    "Please select a medicine first."
            );

            return;
        }

        EditMedicineView editView =
                new EditMedicineView(
                        selectedMedicine
                );

        editView.getSaveButton()
                .setOnAction(event -> {

                    saveEditedMedicine(
                            selectedMedicine,
                            editView
                    );
                });

        editView.getCancelButton()
                .setOnAction(event -> {

                    editView.getStage()
                            .close();
                });

        editView.show();
    }

    private void saveEditedMedicine(
            Medicine oldMedicine,
            EditMedicineView editView) {

        try {

            String name =
                    editView.getNameField()
                            .getText()
                            .trim();

            double price =
                    Double.parseDouble(
                            editView
                                    .getPriceField()
                                    .getText()
                                    .trim()
                    );

            int quantity =
                    Integer.parseInt(
                            editView
                                    .getQuantityField()
                                    .getText()
                                    .trim()
                    );

            if (name.isEmpty()) {

                showAlert(
                        "Invalid Data",
                        "Medicine name cannot be empty."
                );

                return;
            }

            if (price < 0) {

                showAlert(
                        "Invalid Data",
                        "Price cannot be negative."
                );

                return;
            }

            if (quantity < 0) {

                showAlert(
                        "Invalid Data",
                        "Quantity cannot be negative."
                );

                return;
            }

            Medicine updatedMedicine =
                    new Medicine(
                            name,
                            price,
                            quantity
                    );

            service.updateMedicine(
                    oldMedicine,
                    updatedMedicine
            );

            int index =
                    allMedicines.indexOf(
                            oldMedicine
                    );

            allMedicines.set(
                    index,
                    updatedMedicine
            );

            view.getMedicineTable()
                    .refresh();

            editView.getStage()
                    .close();

            showAlert(
                    "Success",
                    "Medicine updated successfully."
            );

        } catch (NumberFormatException e) {

            showAlert(
                    "Invalid Data",
                    "Price must be a number and quantity must be an integer."
            );
        }
    }

    // Reports
    private void setupReportsButton() {
        view.getReportsButton()
                .setOnAction(event -> {

                    openReports();
                });
    }
    private void openReports() {
        ReportsView reportsView = new ReportsView();
        // Display current data
        reportsView.updateReport(allMedicines);
        // Listen for changes in medicines
        ListChangeListener<Medicine> listener = change -> {
            reportsView.updateReport(allMedicines);
            System.out.println("Reports Updated Successfully!");
        };
        allMedicines.addListener(listener);
        // Create Reports Window
        Stage reportsStage = new Stage();
        Scene scene = new Scene(
                reportsView.getRoot(),
                1200,
                800
        );
        reportsStage.setTitle("Pharmacy Reports");
        reportsStage.setScene(scene);
        // Remove listener when window closes
        reportsStage.setOnHidden(event -> {
            allMedicines.removeListener(listener);
        });
        reportsStage.show();
    }

    // =========================================================
    // ALERT
    // =========================================================

    private void showAlert(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    // =========================================================
    // GET VIEW
    // =========================================================

    public MedicineView getView() {

        return view;
    }
}
