package org.example;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public final class ExitConfirmation {

    private ExitConfirmation() {
        // Prevents creation of ExitConfirmation objects.
    }

    public static void show(Stage stage) {

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Exit Confirmation");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to exit?");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");

        confirmation.getButtonTypes().setAll(
                noButton,
                yesButton
        );

        confirmation.initOwner(stage);

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            Platform.exit();
        }
    }
}