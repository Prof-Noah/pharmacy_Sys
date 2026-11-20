package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import sample.model.CartItem;
import sample.model.Medicine;
import sample.service.MedicineService;
import sample.view.SalesView;

public class SalesController {

    private final SalesView view;
    private final MedicineService service;
    private final ObservableList<Medicine> allMedicines;
    private final ObservableList<CartItem> cartItems;

    public SalesController(
            ObservableList<Medicine> allMedicines,
            MedicineService service) {

        view =
                new SalesView();

        this.service =
                service;

        this.allMedicines =
                allMedicines;

        cartItems =
                FXCollections.observableArrayList();

        view.getMedicineTable()
                .setItems(allMedicines);

        view.getCartTable()
                .setItems(cartItems);

        setupSearch();

        setupMedicineSelection();

        setupAddToCartButton();

        setupCheckoutButton();

        setupCancelButton();
    }

    // =========================================================
    // SEARCH
    // =========================================================

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

        view.getSearchButton()
                .setOnAction(event -> {

                    filterMedicines(
                            view.getSearchField()
                                    .getText()
                    );
                });
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
    // MEDICINE SELECTION
    // =========================================================

    private void setupMedicineSelection() {

        view.getMedicineTable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) -> {

                            if (newValue == null) {

                                view.getSelectedMedicineLabel()
                                        .setText(
                                                "Selected: None"
                                        );

                                view.getUnitPriceLabel()
                                        .setText(
                                                "Unit Price: 0.0"
                                        );

                            } else {

                                view.getSelectedMedicineLabel()
                                        .setText(
                                                "Selected: "
                                                        + newValue
                                                        .getName()
                                        );

                                view.getUnitPriceLabel()
                                        .setText(
                                                "Unit Price: "
                                                        + newValue
                                                        .getPrice()
                                        );
                            }
                        }
                );
    }

    // =========================================================
    // ADD TO CART
    // =========================================================

    private void setupAddToCartButton() {

        view.getAddToCartButton()
                .setOnAction(event -> {

                    addSelectedMedicineToCart();
                });
    }

    private void addSelectedMedicineToCart() {

        Medicine selectedMedicine =
                view.getMedicineTable()
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedMedicine == null) {

            showAlert(
                    "Invalid Sale",
                    "Please select a medicine first."
            );

            return;
        }

        try {

            int quantity =
                    Integer.parseInt(
                            view.getQuantityField()
                                    .getText()
                                    .trim()
                    );

            if (quantity <= 0) {

                showAlert(
                        "Invalid Quantity",
                        "Quantity must be greater than zero."
                );

                return;
            }

            CartItem existingItem =
                    findCartItem(
                            selectedMedicine
                    );

            int cartQuantity =
                    existingItem == null
                            ? 0
                            : existingItem.getQuantity();

            if (cartQuantity + quantity
                    > selectedMedicine.getQuantity()) {

                showAlert(
                        "Insufficient Stock",
                        "The requested quantity is not available."
                );

                return;
            }

            if (existingItem == null) {

                cartItems.add(
                        new CartItem(
                                selectedMedicine,
                                quantity
                        )
                );

            } else {

                existingItem.increaseQuantity(
                        quantity
                );
            }

            view.getCartTable()
                    .refresh();

            updateTotal();

            view.getQuantityField()
                    .clear();

        } catch (NumberFormatException e) {

            showAlert(
                    "Invalid Quantity",
                    "Quantity must be a whole number."
            );
        }
    }

    private CartItem findCartItem(
            Medicine medicine) {

        for (CartItem item :
                cartItems) {

            if (item.getMedicine()
                    .getName()
                    .equals(medicine.getName())) {

                return item;
            }
        }

        return null;
    }

    private void updateTotal() {

        double total =
                0;

        for (CartItem item :
                cartItems) {

            total += item.getSubtotal();
        }

        view.getTotalLabel()
                .setText(
                        "Total: "
                                + total
                );
    }

    // =========================================================
    // CHECKOUT
    // =========================================================

    private void setupCheckoutButton() {

        view.getCheckoutButton()
                .setOnAction(event -> {

                    checkout();
                });
    }

    private void checkout() {

        if (cartItems.isEmpty()) {

            showAlert(
                    "Empty Cart",
                    "Please add medicine to the cart first."
            );

            return;
        }

        for (CartItem item :
                cartItems) {

            Medicine medicine =
                    findMedicine(
                            item.getMedicine()
                                    .getName()
                    );

            if (medicine == null
                    || item.getQuantity()
                    > medicine.getQuantity()) {

                showAlert(
                        "Insufficient Stock",
                        "The stock changed. Please review the cart."
                );

                return;
            }
        }

        for (CartItem item :
                cartItems) {

            Medicine oldMedicine =
                    findMedicine(
                            item.getMedicine()
                                    .getName()
                    );

            Medicine updatedMedicine =
                    new Medicine(
                            oldMedicine.getName(),
                            oldMedicine.getPrice(),
                            oldMedicine.getQuantity()
                                    - item.getQuantity()
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
        }

        filterMedicines(
                view.getSearchField()
                        .getText()
        );

        view.getMedicineTable()
                .refresh();

        cartItems.clear();

        updateTotal();

        showAlert(
                "Success",
                "Sale completed successfully."
        );
    }

    private Medicine findMedicine(
            String name) {

        for (Medicine medicine :
                allMedicines) {

            if (medicine.getName()
                    .equals(name)) {

                return medicine;
            }
        }

        return null;
    }

    // =========================================================
    // CANCEL
    // =========================================================

    private void setupCancelButton() {

        view.getCancelButton()
                .setOnAction(event -> {

                    view.getStage()
                            .close();
                });
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
    // SHOW VIEW
    // =========================================================

    public void show() {

        view.show();
    }
}
