package org.example;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class SplashScreen extends StackPane {

    public SplashScreen() {

        Image splashImage = new Image(
                getClass().getResourceAsStream(
                        "/images/tetris-splash.png"
                )
        );

        ImageView imageView = new ImageView(splashImage);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Fit inside the splash window
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());

        setAlignment(Pos.CENTER);

        getChildren().add(imageView);
    }
}