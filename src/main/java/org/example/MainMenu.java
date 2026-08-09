package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;     // clickable button UI element
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;


public class MainMenu {

    private enum MenuOption{
        PLAY, HIGH_SCORE, CONFIGURATION, EXIT
    }

    public static void show(Stage stage) {
        //creates main menu buttons
        Button PlayBtn = new Button("Play");
        Button HScoreBtn = new Button("High Score");
        Button ConfigurationBtn = new Button("Configuration");
        Button ExitBtn = new Button("Exit");


        //sets buttons to their cases
        PlayBtn.setOnAction(e -> handleMenuSelection(MenuOption.PLAY, stage));
        HScoreBtn.setOnAction(e -> handleMenuSelection(MenuOption.HIGH_SCORE, stage));
        ConfigurationBtn.setOnAction(e -> handleMenuSelection(MenuOption.CONFIGURATION, stage));
        ExitBtn.setOnAction(e -> handleMenuSelection(MenuOption.EXIT, stage));

        //main menu title
        Label titleLabel = new Label("TETRIS PG-39 - MAIN MENU");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f3f0f0; -fx-background-color: #000000;");
        VBox.setMargin(titleLabel, new Insets(0, 0, 20, 0));

        // vertical box sets 20 pixel gap between buttons and centers everything
        VBox root = new VBox(20, titleLabel,PlayBtn, HScoreBtn, ConfigurationBtn, ExitBtn);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 40");

        //background
        Image bgImage = new Image(MainMenu.class.getResourceAsStream("/bg.jpg"));
        BackgroundSize bgSize = new BackgroundSize(100, 100, true, true, false, true);
        BackgroundImage bgImg = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bgSize);
        root.setBackground(new Background(bgImg));

        //sets width of all buttons to 400
        for (Button b : new Button[]{PlayBtn, HScoreBtn, ConfigurationBtn, ExitBtn}) {
            b.setPrefWidth(200);
        }

        //button colors
        PlayBtn.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff; -fx-font-weight: 900; -fx-background-color: #3a86ff;");
        HScoreBtn.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff; -fx-font-weight: 900; -fx-background-color: #8338ec;");
        ConfigurationBtn.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff; -fx-font-weight: 900; -fx-background-color: #c02ec2;");
        ExitBtn.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff; -fx-font-weight: 900; -fx-background-color: #e63946;");

        Scene scene = new Scene(root, 800, 600); //fixed window size

        stage.setTitle("Tetris - Main Menu"); // sets window title text
        stage.setScene(scene);
        stage.show();

    }

    //uses switch to redirect when buttons are pressed
    private  static void handleMenuSelection(MenuOption option, Stage stage){
        switch(option){
            case PLAY ->startGame(stage);
            case HIGH_SCORE -> showHighScores(stage);
            case CONFIGURATION -> showConfigurations(stage);
            case EXIT -> exitGame(stage);
        }
    }

    //scenes for each button
    private static void startGame(Stage stage) {
        System.out.println("TODO: launch game screen");
    }

    private static void showHighScores(Stage stage) {
        System.out.println("TODO: show high score screen");
    }

    private static void showConfigurations(Stage stage) {
        ConfigurationScreen.show(stage);
    }

    private static void exitGame(Stage stage) {
        ExitConfirmation.show(stage);
    }

}
