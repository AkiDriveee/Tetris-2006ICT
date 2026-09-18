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
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;


public final class ConfigurationScreen {

    private ConfigurationScreen() {
        // Prevents creation of ConfigurationScreen objects.
    }

    public static void show(Stage stage) {

        // Get the shared configuration values
        GameConfig config = GameSettings.getConfig();

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

        // Load current configuration values instead of hard-coded defaults
        Slider widthSlider = createIntegerSlider(
                5,
                15,
                config.getFieldWidth()
        );

        Slider heightSlider = createIntegerSlider(
                15,
                30,
                config.getFieldHeight()
        );

        Slider levelSlider = createIntegerSlider(
                1,
                10,
                config.getGameLevel()
        );

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
        CheckBox extendedModeCheckBox = new CheckBox();

        // ---------------------------------------------------------
// PLAYER TYPE SELECTION
// ---------------------------------------------------------

        /*
         * Each player can be controlled by a Human, AI, or
         * External controller. ToggleGroup ensures that only
         * one controller type can be selected for each player.
         */
        ToggleGroup playerOneGroup = new ToggleGroup();

        RadioButton playerOneHuman =
                new RadioButton("Human");

        RadioButton playerOneAI =
                new RadioButton("AI");

        RadioButton playerOneExternal =
                new RadioButton("External");

        playerOneHuman.setToggleGroup(playerOneGroup);
        playerOneAI.setToggleGroup(playerOneGroup);
        playerOneExternal.setToggleGroup(playerOneGroup);


        ToggleGroup playerTwoGroup = new ToggleGroup();

        RadioButton playerTwoHuman =
                new RadioButton("Human");

        RadioButton playerTwoAI =
                new RadioButton("AI");

        RadioButton playerTwoExternal =
                new RadioButton("External");

        playerTwoHuman.setToggleGroup(playerTwoGroup);
        playerTwoAI.setToggleGroup(playerTwoGroup);
        playerTwoExternal.setToggleGroup(playerTwoGroup);

        /*
         * Keep the radio-button text visible against the
         * dark Configuration screen background.
         */
        String playerTypeStyle =
                "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;";

        playerOneHuman.setStyle(playerTypeStyle);
        playerOneAI.setStyle(playerTypeStyle);
        playerOneExternal.setStyle(playerTypeStyle);

        playerTwoHuman.setStyle(playerTypeStyle);
        playerTwoAI.setStyle(playerTypeStyle);
        playerTwoExternal.setStyle(playerTypeStyle);

        // Load the current saved settings
        musicCheckBox.setSelected(config.isMusicEnabled());
        soundCheckBox.setSelected(config.isSoundEnabled());
        extendedModeCheckBox.setSelected(config.isExtendedMode());
        /*
         * Restore the saved controller type for each player.
         */
        switch (config.getPlayerOneType()) {

            case HUMAN ->
                    playerOneHuman.setSelected(true);

            case AI ->
                    playerOneAI.setSelected(true);

            case EXTERNAL ->
                    playerOneExternal.setSelected(true);
        }

        switch (config.getPlayerTwoType()) {

            case HUMAN ->
                    playerTwoHuman.setSelected(true);

            case AI ->
                    playerTwoAI.setSelected(true);

            case EXTERNAL ->
                    playerTwoExternal.setSelected(true);
        }
        AudioManager.updateMusicState();

        Label musicValue = createStatusLabel(musicCheckBox.isSelected());
        Label soundValue = createStatusLabel(soundCheckBox.isSelected());
        Label extendedValue = createStatusLabel(extendedModeCheckBox.isSelected());

        musicCheckBox.selectedProperty().addListener((observable, oldValue, selected) -> {

            musicValue.setText(selected ? "On" : "Off");

            config.setMusicEnabled(selected);

            GameSettings.save();

            AudioManager.updateMusicState();
        });

        soundCheckBox.selectedProperty().addListener((observable, oldValue, selected) ->
                soundValue.setText(selected ? "On" : "Off")
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
                "Extended Mode:",
                extendedModeCheckBox,
                extendedValue
        );

        // ---------------------------------------------------------
// PLAYER TYPE ROWS
// ---------------------------------------------------------

        HBox playerOneOptions =
                new HBox(
                        20,
                        playerOneHuman,
                        playerOneAI,
                        playerOneExternal
                );

        playerOneOptions.setAlignment(
                Pos.CENTER_LEFT
        );

        HBox playerTwoOptions =
                new HBox(
                        20,
                        playerTwoHuman,
                        playerTwoAI,
                        playerTwoExternal
                );

        playerTwoOptions.setAlignment(
                Pos.CENTER_LEFT
        );

        /*
         * Player Two is only available in Extended Mode.
         * Binding the disabled state keeps the UI synchronized
         * immediately when Extended Mode is switched on or off.
         */
        playerTwoOptions
                .disableProperty()
                .bind(
                        extendedModeCheckBox
                                .selectedProperty()
                                .not()
                );

        settingsGrid.add(
                createSettingLabel("Player One Type:"),
                0,
                6
        );

        settingsGrid.add(
                playerOneOptions,
                1,
                6
        );

        settingsGrid.add(
                createSettingLabel("Player Two Type:"),
                0,
                7
        );

        settingsGrid.add(
                playerTwoOptions,
                1,
                7
        );

        Button backButton = new Button("Back");
        backButton.setPrefWidth(220);
        backButton.setStyle("""
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                -fx-background-color: #c02ec2;
                -fx-background-radius: 10;
                -fx-padding: 10 20;
                """);

        /*
         * Save the selected configuration values
         * before returning to the Main Menu.
         */
        backButton.setOnAction(event -> {

            config.setFieldWidth((int) widthSlider.getValue());
            config.setFieldHeight((int) heightSlider.getValue());
            config.setGameLevel((int) levelSlider.getValue());

            config.setMusicEnabled(musicCheckBox.isSelected());
            config.setSoundEnabled(soundCheckBox.isSelected());
            config.setExtendedMode(extendedModeCheckBox.isSelected());
            /*
             * Save the selected controller type for both players
             * so the selections persist between application runs.
             */
            if (playerOneHuman.isSelected()) {

                config.setPlayerOneType(
                        PlayerType.HUMAN
                );

            } else if (playerOneAI.isSelected()) {

                config.setPlayerOneType(
                        PlayerType.AI
                );

            } else {

                config.setPlayerOneType(
                        PlayerType.EXTERNAL
                );
            }
            /*
             * Keep the existing AI gameplay implementation synchronized
             * with the new Player One Type selection.
             *
             * This allows the current PlayScreen AI code to continue using
             * isAiEnabled() without changing the teammate's AI implementation.
             */
            config.setAiEnabled(
                    config.getPlayerOneType() == PlayerType.AI
            );

            if (playerTwoHuman.isSelected()) {

                config.setPlayerTwoType(
                        PlayerType.HUMAN
                );

            } else if (playerTwoAI.isSelected()) {

                config.setPlayerTwoType(
                        PlayerType.AI
                );

            } else {

                config.setPlayerTwoType(
                        PlayerType.EXTERNAL
                );
            }
            GameSettings.save();

            MainMenu.show(stage);
        });

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

        Scene scene = new Scene(root, 1000, 700);

        stage.setTitle("Tetris - Configuration");
        stage.setScene(scene);
    }

    private static Slider createIntegerSlider(
            double minimum,
            double maximum,
            double initialValue
    ) {

        Slider slider = new Slider(
                minimum,
                maximum,
                initialValue
        );

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

        Label label = new Label(
                String.valueOf(value)
        );

        label.setStyle("""
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-text-fill: #4cc9f0;
                """);

        return label;
    }

    private static Label createStatusLabel(boolean selected) {

        Label label = new Label(
                selected ? "On" : "Off"
        );

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

        grid.add(
                createSettingLabel(settingName),
                0,
                row
        );

        grid.add(
                slider,
                1,
                row
        );

        grid.add(
                valueLabel,
                2,
                row
        );
    }

    private static void addCheckBoxRow(
            GridPane grid,
            int row,
            String settingName,
            CheckBox checkBox,
            Label statusLabel
    ) {

        grid.add(
                createSettingLabel(settingName),
                0,
                row
        );

        grid.add(
                checkBox,
                1,
                row
        );

        grid.add(
                statusLabel,
                2,
                row
        );
    }
}