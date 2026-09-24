package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainMenu {

    /*
     * Load the Main Menu background image only once.
     */
    private static final Image BG_IMAGE =
            new Image(MainMenu.class.getResource("/bg.jpg").toExternalForm());

    /*
     * Keep one Main Menu scene instead of rebuilding the whole
     * Main Menu every time the user presses Back.
     */
    private static Scene mainMenuScene;

    private enum MenuOption {
        PLAY,
        HIGH_SCORE,
        CONFIGURATION,
        EXIT
    }

    public static void show(Stage stage) {

        /*
         * Build the Main Menu only the first time.
         * When returning from another screen, reuse the existing scene.
         */
        if (mainMenuScene == null) {
            mainMenuScene = createMainMenuScene(stage);
        }

        stage.setTitle("Tetris - Main Menu");

        /*
         * Reuse the already-created Main Menu scene.
         * This avoids rebuilding the background, buttons and layout
         * whenever Back is pressed.
         */
        if (stage.getScene() != mainMenuScene) {
            stage.setScene(mainMenuScene);
        }

        /*
         * Only show the Stage when the application first starts.
         */
        if (!stage.isShowing()) {
            stage.show();
        }
    }

    /*
     * Creates the Main Menu scene once.
     */
    private static Scene createMainMenuScene(Stage stage) {

        // ---------------------------------------------------------
        // BUTTONS
        // ---------------------------------------------------------

        Button PlayBtn = new Button("Play");
        Button HScoreBtn = new Button("High Score");
        Button ConfigurationBtn = new Button("Configuration");
        Button ExitBtn = new Button("Exit");

        PlayBtn.setOnAction(
                e -> handleMenuSelection(MenuOption.PLAY, stage)
        );

        HScoreBtn.setOnAction(
                e -> handleMenuSelection(MenuOption.HIGH_SCORE, stage)
        );

        ConfigurationBtn.setOnAction(
                e -> handleMenuSelection(MenuOption.CONFIGURATION, stage)
        );

        ExitBtn.setOnAction(
                e -> handleMenuSelection(MenuOption.EXIT, stage)
        );

        // ---------------------------------------------------------
        // TITLE
        // ---------------------------------------------------------

        Label titleLabel =
                new Label("TETRIS PG-39 - MAIN MENU");

        titleLabel.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #f3f0f0;" +
                        "-fx-background-color: #000000;"
        );

        VBox.setMargin(
                titleLabel,
                new Insets(0, 0, 20, 0)
        );

        // ---------------------------------------------------------
        // MAIN LAYOUT
        // ---------------------------------------------------------

        VBox root = new VBox(
                20,
                titleLabel,
                PlayBtn,
                HScoreBtn,
                ConfigurationBtn,
                ExitBtn
        );

        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 40");

        // ---------------------------------------------------------
        // BACKGROUND
        // ---------------------------------------------------------

        BackgroundSize bgSize =
                new BackgroundSize(
                        100,
                        100,
                        true,
                        true,
                        false,
                        true
                );

        BackgroundImage bgImg =
                new BackgroundImage(
                        BG_IMAGE,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        bgSize
                );

        root.setBackground(
                new Background(bgImg)
        );

        // ---------------------------------------------------------
        // BUTTON SIZES
        // ---------------------------------------------------------

        for (Button b : new Button[]{
                PlayBtn,
                HScoreBtn,
                ConfigurationBtn,
                ExitBtn
        }) {
            b.setPrefWidth(200);
        }

        // ---------------------------------------------------------
        // BUTTON COLOURS
        // ---------------------------------------------------------

        PlayBtn.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-weight: 900;" +
                        "-fx-background-color: #3a86ff;"
        );

        HScoreBtn.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-weight: 900;" +
                        "-fx-background-color: #8338ec;"
        );

        ConfigurationBtn.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-weight: 900;" +
                        "-fx-background-color: #c02ec2;"
        );

        ExitBtn.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-weight: 900;" +
                        "-fx-background-color: #e63946;"
        );

        // ---------------------------------------------------------
        // SCENE
        // ---------------------------------------------------------

        return new Scene(root, 1000, 700);
    }

    // ---------------------------------------------------------
    // MENU NAVIGATION
    // ---------------------------------------------------------

    private static void handleMenuSelection(
            MenuOption option,
            Stage stage
    ) {

        switch (option) {

            case PLAY ->
                    startGame(stage);

            case HIGH_SCORE ->
                    showHighScores(stage);

            case CONFIGURATION ->
                    showConfigurations(stage);

            case EXIT ->
                    exitGame(stage);
        }
    }

    private static void startGame(Stage stage) {
        PlayScreen.show(stage);
    }

    private static void showHighScores(Stage stage) {
        HighScore.show(stage);
    }

    private static void showConfigurations(Stage stage) {
        ConfigurationScreen.show(stage);
    }

    private static void exitGame(Stage stage) {
        ExitConfirmation.show(stage);
    }
}