package org.oosd;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class HighScoreTest {

    @BeforeAll
    static void initToolkit() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new AssertionError("JavaFX toolkit did not start");
        }
    }

    @Test
    void show_setsTitleAndSceneAndAddsScoreLabels() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Stage[] stageRef = new Stage[1];

        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                HighScore.show(stage);
                stageRef[0] = stage;
                latch.countDown();
            } catch (Exception ex) {
                latch.countDown();
                throw new RuntimeException(ex);
            }
        });

        Assertions.assertTrue(latch.await(10, TimeUnit.SECONDS), "HighScore.show did not complete");

        Stage stage = stageRef[0];
        Assertions.assertNotNull(stage);
        Assertions.assertEquals("High Scores", stage.getTitle());
        Scene scene = stage.getScene();
        Assertions.assertNotNull(scene);
        Assertions.assertEquals(1000.0, scene.getWidth());
        Assertions.assertEquals(700.0, scene.getHeight());

        BorderPane root = (BorderPane) scene.getRoot();
        VBox top = (VBox) root.getTop();
        Label title = (Label) top.getChildren().get(0);
        Assertions.assertEquals("HIGH SCORES", title.getText());

        VBox center = (VBox) root.getCenter();
        Assertions.assertEquals(10, center.getChildren().size());
        Assertions.assertEquals("Aksa        9800", ((Label) center.getChildren().get(0)).getText());

        VBox bottom = (VBox) root.getBottom();
        Button backButton = (Button) bottom.getChildren().get(0);
        Assertions.assertEquals("Back", backButton.getText());
    }

    @Test
    void backButton_closesStage() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Stage[] stageRef = new Stage[1];

        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                HighScore.show(stage);
                stageRef[0] = stage;
                Button backButton = (Button) ((VBox) ((BorderPane) stage.getScene().getRoot()).getBottom()).getChildren().get(0);
                backButton.fire();
                latch.countDown();
            } catch (Exception ex) {
                latch.countDown();
                throw new RuntimeException(ex);
            }
        });

        Assertions.assertTrue(latch.await(10, TimeUnit.SECONDS), "Back button action did not complete");
        Assertions.assertTrue(stageRef[0].isShowing());
    }
}
