package sample.login;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.function.Consumer;

public class RegisterController {

    @FXML
    private TextField pharmacyNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    private AuthService authService;
    private Consumer<UserAccount> onRegistered;

    public void configure(
            AuthService authService,
            Consumer<UserAccount> onRegistered
    ) {
        this.authService = authService;
        this.onRegistered = onRegistered;
    }

    @FXML
    private void handleRegister() {
        String pharmacyName = pharmacyNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmation = confirmPasswordField.getText();

        if (pharmacyName.isEmpty() || username.isEmpty()
                || password.isEmpty() || confirmation.isEmpty()) {
            messageLabel.setText("Complete all fields to create your account.");
            return;
        }

        if (password.length() < 8) {
            messageLabel.setText("Password must be at least 8 characters.");
            return;
        }

        if (!password.equals(confirmation)) {
            messageLabel.setText("The passwords do not match.");
            return;
        }

        try {
            UserAccount account = authService.register(pharmacyName, username, password);
            if (account == null) {
                messageLabel.setText("That username is already registered.");
                return;
            }

            if (onRegistered != null) {
                onRegistered.accept(account);
            }
        } catch (IOException | GeneralSecurityException e) {
            messageLabel.setText("Could not save the account. Please try again.");
        }
    }
}
