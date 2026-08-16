package org.oosd;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class HighScore {

    public static void show(Stage stage) {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");

        // Title
        Label title = new Label("HIGH SCORES");
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-size: 42px; -fx-font-weight: bold;");

        VBox top = new VBox(title);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(30));
        root.setTop(top);

        // Scores
        VBox scores = new VBox(15);
        scores.setAlignment(Pos.CENTER);

        String[] highScores = {
                "Aksa        9800",
                "Sukhdeep    9200",
                "Taj         8700",
                "Havana      8300",
                "Emma        7900",
                "Lima        7500",
                "John        7100",
                "Peria       6800",
                "Olivia      6400",
                "Max         6000"
        };

        for (String score : highScores) {
            Label label = new Label(score);
            label.setTextFill(Color.WHITE);
            label.setStyle("-fx-font-size: 26px; -fx-font-family: Consolas;");
            scores.getChildren().add(label);
        }

        root.setCenter(scores);

        // Back button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 20px;");

        backButton.setOnAction(e -> stage.close());

        VBox bottom = new VBox(backButton);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(30));

        root.setBottom(bottom);

        Scene scene = new Scene(root, 1000, 700);

        stage.setTitle("High Scores");
        stage.setScene(scene);
        stage.show();
    }
}