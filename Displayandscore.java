package org.oosd.milstone2;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Displayandscore extends Application {

    private int score = 0;
    private int level = 1;
    private int linesErased = 0;
    private String playerType = "Player 1";

    private Label playerLabel;
    private Label levelLabel;
    private Label scoreLabel;
    private Label linesLabel;

    @Override
    public void start(Stage stage) {

        Label title = new Label("Score & Display");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        playerLabel = new Label();
        levelLabel = new Label();
        scoreLabel = new Label();
        linesLabel = new Label();

        updateDisplay();

        
        Button oneLine = new Button("Clear 1 Line");
        Button twoLines = new Button("Clear 2 Lines");
        Button threeLines = new Button("Clear 3 Lines");
        Button fourLines = new Button("Clear 4 Lines");
        Button reset = new Button("Reset");

        oneLine.setOnAction(e -> addScore(1));
        twoLines.setOnAction(e -> addScore(2));
        threeLines.setOnAction(e -> addScore(3));
        fourLines.setOnAction(e -> addScore(4));
        reset.setOnAction(e -> resetScore());

        VBox root = new VBox(15,
                title,
                playerLabel,
                levelLabel,
                scoreLabel,
                linesLabel,
                oneLine,
                twoLines,
                threeLines,
                fourLines,
                reset
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 400, 500);

        stage.setTitle("Score & Display");
        stage.setScene(scene);
        stage.show();
    }

    private void addScore(int linesCleared) {

        switch (linesCleared) {
            case 1 -> score += 100;
            case 2 -> score += 300;
            case 3 -> score += 600;
            case 4 -> score += 1000;
        }

        linesErased += linesCleared;
        level = (linesErased / 10) + 1;

        updateDisplay();
    }

    private void updateDisplay() {
        playerLabel.setText("Player Type: " + playerType);
        levelLabel.setText("Current Level: " + level);
        scoreLabel.setText("Score: " + score);
        linesLabel.setText("Lines Erased: " + linesErased);
    }

    private void resetScore() {
        score = 0;
        level = 1;
        linesErased = 0;
        updateDisplay();
    }

    public static void main(String[] args) {
        launch(args);
    }
}