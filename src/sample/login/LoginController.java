package sample.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.function.Consumer;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private AuthService authService;
    private Consumer<UserAccount> onAuthenticated;

    public void configure(
            AuthService authService,
            Consumer<UserAccount> onAuthenticated
    ) {
        this.authService = authService;
        this.onAuthenticated = onAuthenticated;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Enter your username and password.");
            return;
        }

        try {
            UserAccount account = authService.authenticate(username, password);
            if (account != null) {
                if (onAuthenticated != null) {
                    onAuthenticated.accept(account);
                }
            } else {
                messageLabel.setText("Incorrect username or password.");
            }
        } catch (IOException | GeneralSecurityException e) {
            messageLabel.setText("Could not read account data. Please try again.");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sample/login/Register.fxml")
            );
            Parent registerRoot = loader.load();
            RegisterController registerController = loader.getController();

            Stage registerStage = new Stage();
            registerStage.setTitle("Create Account");
            registerStage.initOwner(usernameField.getScene().getWindow());
            registerStage.initModality(Modality.APPLICATION_MODAL);

            Scene registerScene = new Scene(registerRoot, 520, 700);
            registerScene.getStylesheets().add(
                    getClass().getResource("/sample/login/Login.css").toExternalForm()
            );
            registerStage.setScene(registerScene);

            registerController.configure(authService, account -> {
                registerStage.close();
                if (onAuthenticated != null) {
                    onAuthenticated.accept(account);
                }
            });

            messageLabel.setText(" ");
            registerStage.showAndWait();
        } catch (IOException e) {
            messageLabel.setText("Could not open account registration.");
        }
    }

    public void clearForm() {
        usernameField.clear();
        passwordField.clear();
        messageLabel.setText(" ");
    }
}
