package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import sample.login.AuthService;
import sample.login.LoginController;
import sample.login.SettingsController;
import sample.login.UserAccount;
import sample.controller.MedicineController;
import javafx.stage.Modality;

import java.io.IOException;
import java.util.function.Consumer;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/sample/login/Login.fxml")
        );
        Parent loginRoot = loader.load();
        LoginController loginController = loader.getController();
        AuthService authService = new AuthService();

        Scene loginScene = new Scene(loginRoot, 1000, 700);
        loginScene.getStylesheets().add(
                getClass().getResource("/sample/login/Login.css").toExternalForm()
        );

        loginController.configure(
                authService,
                account -> showDashboard(stage, loginScene, loginController, authService, account)
        );

        stage.setTitle("Pharmacy Management System");
        stage.setScene(loginScene);
        stage.show();
    }

    private void showDashboard(
            Stage stage,
            Scene loginScene,
            LoginController loginController,
            AuthService authService,
            UserAccount account
    ) {
        MedicineController controller = new MedicineController();
        UserAccount[] activeAccount = {account};
        controller.getView().setAccountDetails(
                account.getPharmacyName(),
                account.getUsername(),
                account.getLowStockThreshold()
        );

        controller.setOnSettings(() -> openSettings(
                stage,
                authService,
                activeAccount[0],
                updatedAccount -> {
                    activeAccount[0] = updatedAccount;
                    controller.getView().setAccountDetails(
                            updatedAccount.getPharmacyName(),
                            updatedAccount.getUsername(),
                            updatedAccount.getLowStockThreshold()
                    );
                    stage.setTitle(windowTitle(updatedAccount));
                },
                () -> {
                    loginController.clearForm();
                    stage.setTitle("Pharmacy Management System");
                    stage.setScene(loginScene);
                }
        ));

        controller.setOnLogout(() -> {
            loginController.clearForm();
            stage.setTitle("Pharmacy Management System");
            stage.setScene(loginScene);
        });

        stage.setTitle(windowTitle(account));
        stage.setScene(new Scene(controller.getView().getRoot(), 1000, 700));
    }

    private void openSettings(
            Stage owner,
            AuthService authService,
            UserAccount account,
            Consumer<UserAccount> onSaved,
            Runnable onDeleted
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sample/login/Settings.fxml")
            );
            Parent settingsRoot = loader.load();
            SettingsController settingsController = loader.getController();

            Stage settingsStage = new Stage();
            settingsStage.setTitle("Account Settings");
            settingsStage.initOwner(owner);
            settingsStage.initModality(Modality.APPLICATION_MODAL);

            Scene settingsScene = new Scene(settingsRoot, 580, 800);
            settingsScene.getStylesheets().add(
                    getClass().getResource("/sample/login/Login.css").toExternalForm()
            );
            settingsStage.setScene(settingsScene);
            settingsController.configure(
                    authService,
                    account,
                    settingsStage,
                    onSaved,
                    onDeleted
            );
            settingsStage.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(owner);
            alert.setTitle("Settings Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not open account settings.");
            alert.showAndWait();
        }
    }

    private String windowTitle(UserAccount account) {
        return account.getPharmacyName() + " - Pharmacy Management System";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
