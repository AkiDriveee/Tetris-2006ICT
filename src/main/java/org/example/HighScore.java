package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

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

        // Title
        Label title = new Label("HIGH SCORES");
        title.setStyle("""
                -fx-font-size: 34px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                """);

        VBox.setMargin(title, new Insets(0, 0, 20, 0));

        // Score container
        VBox scoreContainer = new VBox(5);
        scoreContainer.setMaxWidth(520);
        scoreContainer.setPadding(new Insets(20));

        scoreContainer.setStyle("""
                -fx-background-color: rgba(255,255,255,0.08);
                -fx-background-radius: 20;
                -fx-border-radius: 20;
                -fx-border-color: rgba(255,255,255,0.20);
                -fx-border-width: 1;
                """);

        String[][] scores = {
                {"1", "Aksa", "9800"},
                {"2", "Sukhdeep", "9200"},
                {"3", "Taj", "8700"},
                {"4", "Havana", "8300"},
                {"5", "Emma", "7900"},
                {"6", "Lima", "7500"},
                {"7", "John", "7100"},
                {"8", "Peria", "6800"},
                {"9", "Olivia", "6400"},
                {"10", "Max", "6000"}
        };

        for (String[] score : scores) {

            Label rank = new Label(score[0]);
            rank.setPrefWidth(45);
            rank.setStyle("""
                    -fx-text-fill: #00d9ff;
                    -fx-font-size: 18px;
                    -fx-font-weight: bold;
                    """);

            Label name = new Label(score[1]);
            name.setPrefWidth(260);
            name.setStyle("""
                    -fx-text-fill: white;
                    -fx-font-size: 18px;
                    -fx-font-weight: bold;
                    """);

            Label points = new Label(score[2]);
            points.setStyle("""
                    -fx-text-fill: #00d9ff;
                    -fx-font-size: 18px;
                    -fx-font-weight: bold;
                    """);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(12, rank, name, spacer, points);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 18, 6, 18));

            row.setStyle("""
                    -fx-background-color: rgba(255,255,255,0.10);
                    -fx-background-radius: 12;
                    """);

            scoreContainer.getChildren().add(row);
        }

        // Back button
        Button backButton = new Button("Back");
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

        // Correct Milestone 1 behaviour:
        // return to the main menu instead of closing the application
        backButton.setOnAction(e -> MainMenu.show(stage));

        VBox.setMargin(backButton, new Insets(15, 0, 0, 0));

        root.getChildren().addAll(
                title,
                scoreContainer,
                backButton
        );

        Scene scene = new Scene(root, 1000, 700);

        stage.setTitle("Tetris - High Scores");
        stage.setScene(scene);
    }
}