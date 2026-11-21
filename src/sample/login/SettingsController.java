package sample.login;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.function.Consumer;

public class SettingsController {

    @FXML
    private TextField pharmacyNameField;

    @FXML
    private TextField lowStockThresholdField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    private AuthService authService;
    private UserAccount account;
    private Stage stage;
    private Consumer<UserAccount> onSaved;
    private Runnable onDeleted;

    public void configure(
            AuthService authService,
            UserAccount account,
            Stage stage,
            Consumer<UserAccount> onSaved,
            Runnable onDeleted
    ) {
        this.authService = authService;
        this.account = account;
        this.stage = stage;
        this.onSaved = onSaved;
        this.onDeleted = onDeleted;

        pharmacyNameField.setText(account.getPharmacyName());
        lowStockThresholdField.setText(String.valueOf(account.getLowStockThreshold()));
        usernameField.setText(account.getUsername());
    }

    @FXML
    private void handleSave() {
        String pharmacyName = pharmacyNameField.getText().trim();
        String thresholdText = lowStockThresholdField.getText().trim();
        String username = usernameField.getText().trim();
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmation = confirmPasswordField.getText();

        if (pharmacyName.isEmpty() || thresholdText.isEmpty()
                || username.isEmpty() || currentPassword.isEmpty()) {
            messageLabel.setText("Pharmacy name, threshold, username, and current password are required.");
            return;
        }

        int lowStockThreshold;
        try {
            lowStockThreshold = Integer.parseInt(thresholdText);
        } catch (NumberFormatException e) {
            messageLabel.setText("Low-stock threshold must be a whole number from 0 to 1,000,000.");
            return;
        }
        if (lowStockThreshold < 0 || lowStockThreshold > 1_000_000) {
            messageLabel.setText("Low-stock threshold must be from 0 to 1,000,000.");
            return;
        }

        boolean changingPassword = !newPassword.isEmpty() || !confirmation.isEmpty();
        if (changingPassword && !newPassword.equals(confirmation)) {
            messageLabel.setText("The new passwords do not match.");
            return;
        }
        if (changingPassword && newPassword.length() < 8) {
            messageLabel.setText("New password must be at least 8 characters.");
            return;
        }

        try {
            AuthService.UpdateStatus result = authService.updateAccount(
                    account,
                    currentPassword,
                    pharmacyName,
                    username,
                    lowStockThreshold,
                    changingPassword ? newPassword : ""
            );

            if (result == AuthService.UpdateStatus.INVALID_CREDENTIALS) {
                messageLabel.setText("Current password is incorrect.");
                return;
            }
            if (result == AuthService.UpdateStatus.USERNAME_TAKEN) {
                messageLabel.setText("That username is already in use.");
                return;
            }
            if (result != AuthService.UpdateStatus.UPDATED) {
                messageLabel.setText("Account was not found. Please sign in again.");
                return;
            }

            account = new UserAccount(username, pharmacyName, lowStockThreshold);
            pharmacyNameField.setText(pharmacyName);
            lowStockThresholdField.setText(String.valueOf(lowStockThreshold));
            usernameField.setText(username);
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            messageLabel.setText("Settings saved.");
            if (onSaved != null) {
                onSaved.accept(account);
            }
        } catch (IOException | GeneralSecurityException e) {
            messageLabel.setText("Could not save settings. Please try again.");
        }
    }

    @FXML
    private void handleDelete() {
        String currentPassword = currentPasswordField.getText();
        if (currentPassword.isEmpty()) {
            messageLabel.setText("Enter your current password to delete the account.");
            return;
        }

        ButtonType deleteButton = new ButtonType(
                "Delete account",
                ButtonBar.ButtonData.OK_DONE
        );
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.initOwner(stage);
        confirmation.setTitle("Delete Account");
        confirmation.setHeaderText("Delete this account permanently?");
        confirmation.setContentText(
                "This removes the account login. The pharmacy medicine data file will remain unchanged."
        );
        confirmation.getButtonTypes().setAll(deleteButton, ButtonType.CANCEL);

        Optional<ButtonType> choice = confirmation.showAndWait();
        if (!choice.isPresent() || choice.get() != deleteButton) {
            return;
        }

        try {
            if (!authService.deleteAccount(account, currentPassword)) {
                messageLabel.setText("Current password is incorrect or the account no longer exists.");
                return;
            }

            stage.close();
            if (onDeleted != null) {
                onDeleted.run();
            }
        } catch (IOException | GeneralSecurityException e) {
            messageLabel.setText("Could not delete the account. Please try again.");
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }
}
