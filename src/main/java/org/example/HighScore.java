package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class HighScore {

    public static void show(Stage stage) {

        // Main background
        VBox root = new VBox(15);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(35, 80, 30, 80));

        root.setStyle("""
                -fx-background-color: linear-gradient(
                    to bottom,
                    #07142f,
                    #15104f,
                    #2b1068
                );
                """);

        // ---------------------------------------------------------
        // TITLE
        // ---------------------------------------------------------

        Label title = new Label("HIGH SCORES");

        title.setStyle("""
                -fx-font-size: 34px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                """);

        // ---------------------------------------------------------
        // CLEAR HIGH SCORE BUTTON
        // ---------------------------------------------------------

        Button clearButton = new Button("Clear High Score");

        clearButton.setPrefWidth(200);
        clearButton.setPrefHeight(38);

        clearButton.setStyle("""
                -fx-background-color: #e63946;
                -fx-background-radius: 10;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """);

        clearButton.setOnAction(e -> {
            HighScoreManager.clear();

            // Refresh the screen after clearing the saved scores.
            show(stage);
        });

        // Push the Clear button to the right side.
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(
                title,
                spacer,
                clearButton
        );

        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setMaxWidth(650);

        VBox.setMargin(
                topRow,
                new Insets(0, 0, 20, 0)
        );

        // ---------------------------------------------------------
        // SCORE CONTAINER
        // ---------------------------------------------------------

        VBox scoreContainer = new VBox(5);

        scoreContainer.setMaxWidth(650);
        scoreContainer.setPadding(new Insets(20));

        scoreContainer.setStyle("""
                -fx-background-color: rgba(255,255,255,0.08);
                -fx-background-radius: 20;
                -fx-border-radius: 20;
                -fx-border-color: rgba(255,255,255,0.20);
                -fx-border-width: 1;
                """);

        // ---------------------------------------------------------
        // COLUMN HEADINGS
        // ---------------------------------------------------------

        Label rankHeading = new Label("#");
        rankHeading.setPrefWidth(40);

        Label nameHeading = new Label("Name");
        nameHeading.setPrefWidth(160);

        Label scoreHeading = new Label("Score");
        scoreHeading.setPrefWidth(100);

        Label configHeading = new Label("Config");
        configHeading.setPrefWidth(200);

        String headingStyle = """
                -fx-text-fill: #00d9ff;
                -fx-font-size: 15px;
                -fx-font-weight: bold;
                """;

        rankHeading.setStyle(headingStyle);
        nameHeading.setStyle(headingStyle);
        scoreHeading.setStyle(headingStyle);
        configHeading.setStyle(headingStyle);

        HBox headingRow = new HBox(
                12,
                rankHeading,
                nameHeading,
                scoreHeading,
                configHeading
        );

        headingRow.setAlignment(Pos.CENTER_LEFT);
        headingRow.setPadding(
                new Insets(0, 18, 8, 18)
        );

        scoreContainer.getChildren().add(headingRow);

        // ---------------------------------------------------------
        // LOAD HIGH SCORES
        // ---------------------------------------------------------

        /*
         * Load the real saved scores and fill any unused positions
         * so that the High Score screen always displays 10 ranks.
         */
        List<ScoreEntry> scoreList =
                HighScoreManager.load();

        ScoreEntry[] scores = new ScoreEntry[10];

        for (int i = 0; i < 10; i++) {

            if (i < scoreList.size()) {
                scores[i] = scoreList.get(i);

            } else {
                scores[i] =
                        new ScoreEntry(
                                "----",
                                0,
                                "----"
                        );
            }
        }

        // ---------------------------------------------------------
        // SCORE ROWS
        // ---------------------------------------------------------

        for (int i = 0; i < scores.length; i++) {

            ScoreEntry score = scores[i];

            // Rank
            Label rank =
                    new Label(String.valueOf(i + 1));

            rank.setPrefWidth(40);

            rank.setStyle("""
                    -fx-text-fill: #00d9ff;
                    -fx-font-size: 16px;
                    -fx-font-weight: bold;
                    """);

            // Player name
            Label name =
                    new Label(score.name());

            name.setPrefWidth(160);

            name.setStyle("""
                    -fx-text-fill: white;
                    -fx-font-size: 16px;
                    -fx-font-weight: bold;
                    """);

            // Score
            Label points =
                    new Label(
                            String.valueOf(score.score())
                    );

            points.setPrefWidth(100);

            points.setStyle("""
                    -fx-text-fill: #00d9ff;
                    -fx-font-size: 16px;
                    -fx-font-weight: bold;
                    """);

            // Configuration used for the score
            Label config =
                    new Label(score.config());

            config.setPrefWidth(200);

            config.setStyle("""
                    -fx-text-fill: rgba(255,255,255,0.7);
                    -fx-font-size: 14px;
                    """);

            HBox row = new HBox(
                    12,
                    rank,
                    name,
                    points,
                    config
            );

            row.setAlignment(Pos.CENTER_LEFT);

            row.setPadding(
                    new Insets(6, 18, 6, 18)
            );

            row.setStyle("""
                    -fx-background-color: rgba(255,255,255,0.10);
                    -fx-background-radius: 12;
                    """);

            scoreContainer
                    .getChildren()
                    .add(row);
        }

        // ---------------------------------------------------------
        // BACK BUTTON
        // ---------------------------------------------------------

        Button backButton =
                new Button("Back");

        backButton.setPrefWidth(220);
        backButton.setPrefHeight(45);

        backButton.setStyle("""
                -fx-background-color: #8d2df5;
                -fx-background-radius: 10;
                -fx-text-fill: white;
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """);

        // Return to the Main Menu.
        backButton.setOnAction(
                e -> MainMenu.show(stage)
        );

        VBox.setMargin(
                backButton,
                new Insets(15, 0, 0, 0)
        );

        // ---------------------------------------------------------
        // FINAL LAYOUT
        // ---------------------------------------------------------

        root.getChildren().addAll(
                topRow,
                scoreContainer,
                backButton
        );

        Scene scene =
                new Scene(root, 1000, 700);

        stage.setTitle(
                "Tetris - High Scores"
        );

        stage.setScene(scene);
    }
}