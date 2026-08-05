package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class ConfigurationScreen {

    private ConfigurationScreen() {
        // Prevents creation of ConfigurationScreen objects.
    }

    public static void show(Stage stage) {

        Label title = new Label("CONFIGURATION");
        title.setStyle("""
                -fx-font-size: 34px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                """);

        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(25);
        settingsGrid.setVgap(24);
        settingsGrid.setAlignment(Pos.CENTER);
        settingsGrid.setPadding(new Insets(20));

        Slider widthSlider = createIntegerSlider(5, 15, 10);
        Slider heightSlider = createIntegerSlider(15, 30, 20);
        Slider levelSlider = createIntegerSlider(1, 10, 1);

        Label widthValue = createValueLabel((int) widthSlider.getValue());
        Label heightValue = createValueLabel((int) heightSlider.getValue());
        Label levelValue = createValueLabel((int) levelSlider.getValue());

        widthSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                widthValue.setText(String.valueOf(newValue.intValue()))
        );

        heightSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                heightValue.setText(String.valueOf(newValue.intValue()))
        );

        levelSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                levelValue.setText(String.valueOf(newValue.intValue()))
        );

        CheckBox musicCheckBox = new CheckBox();
        CheckBox soundCheckBox = new CheckBox();
        CheckBox aiCheckBox = new CheckBox();
        CheckBox extendedModeCheckBox = new CheckBox();

        musicCheckBox.setSelected(true);
        soundCheckBox.setSelected(true);

        Label musicValue = createStatusLabel(musicCheckBox.isSelected());
        Label soundValue = createStatusLabel(soundCheckBox.isSelected());
        Label aiValue = createStatusLabel(aiCheckBox.isSelected());
        Label extendedValue = createStatusLabel(extendedModeCheckBox.isSelected());

        musicCheckBox.selectedProperty().addListener((observable, oldValue, selected) ->
                musicValue.setText(selected ? "On" : "Off")
        );

        soundCheckBox.selectedProperty().addListener((observable, oldValue, selected) ->
                soundValue.setText(selected ? "On" : "Off")
        );

        aiCheckBox.selectedProperty().addListener((observable, oldValue, selected) ->
                aiValue.setText(selected ? "On" : "Off")
        );

        extendedModeCheckBox.selectedProperty().addListener((observable, oldValue, selected) ->
                extendedValue.setText(selected ? "On" : "Off")
        );

        addSliderRow(
                settingsGrid,
                0,
                "Field Width (No. of cells):",
                widthSlider,
                widthValue
        );

        addSliderRow(
                settingsGrid,
                1,
                "Field Height (No. of cells):",
                heightSlider,
                heightValue
        );

        addSliderRow(
                settingsGrid,
                2,
                "Game Level:",
                levelSlider,
                levelValue
        );

        addCheckBoxRow(
                settingsGrid,
                3,
                "Music:",
                musicCheckBox,
                musicValue
        );

        addCheckBoxRow(
                settingsGrid,
                4,
                "Sound Effects:",
                soundCheckBox,
                soundValue
        );

        addCheckBoxRow(
                settingsGrid,
                5,
                "AI Play:",
                aiCheckBox,
                aiValue
        );

        addCheckBoxRow(
                settingsGrid,
                6,
                "Extended Mode:",
                extendedModeCheckBox,
                extendedValue
        );

        Button backButton = new Button("Back");
        backButton.setPrefWidth(220);
        backButton.setStyle("""
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                -fx-background-color: #8338ec;
                -fx-background-radius: 10;
                -fx-padding: 10 20;
                """);

        backButton.setOnAction(event -> MainMenu.show(stage));

        VBox root = new VBox(
                22,
                title,
                settingsGrid,
                new HBox(backButton)
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("""
                -fx-background-color: linear-gradient(
                    to bottom,
                    #07122b,
                    #15104a,
                    #241059
                );
                """);

        HBox buttonBox = (HBox) root.getChildren().get(2);
        buttonBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 900, 700);

        stage.setTitle("Tetris - Configuration");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private static Slider createIntegerSlider(
            double minimum,
            double maximum,
            double initialValue
    ) {
        Slider slider = new Slider(minimum, maximum, initialValue);

        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setBlockIncrement(1);
        slider.setSnapToTicks(true);
        slider.setPrefWidth(380);

        return slider;
    }

    private static Label createValueLabel(int value) {
        Label label = new Label(String.valueOf(value));

        label.setStyle("""
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-text-fill: #4cc9f0;
                """);

        return label;
    }

    private static Label createStatusLabel(boolean selected) {
        Label label = new Label(selected ? "On" : "Off");

        label.setStyle("""
                -fx-font-size: 17px;
                -fx-font-weight: bold;
                -fx-text-fill: #4cc9f0;
                """);

        return label;
    }

    private static Label createSettingLabel(String text) {
        Label label = new Label(text);

        label.setStyle("""
                -fx-font-size: 17px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                """);

        return label;
    }

    private static void addSliderRow(
            GridPane grid,
            int row,
            String settingName,
            Slider slider,
            Label valueLabel
    ) {
        grid.add(createSettingLabel(settingName), 0, row);
        grid.add(slider, 1, row);
        grid.add(valueLabel, 2, row);
    }

    private static void addCheckBoxRow(
            GridPane grid,
            int row,
            String settingName,
            CheckBox checkBox,
            Label statusLabel
    ) {
        grid.add(createSettingLabel(settingName), 0, row);
        grid.add(checkBox, 1, row);
        grid.add(statusLabel, 2, row);
    }
}