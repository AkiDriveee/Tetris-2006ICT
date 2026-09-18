package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import org.example.*;
import org.example.pieces.Tetromino;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayScreen {

    // ---------------------------------------------------------
// BOARD DIMENSIONS
// ---------------------------------------------------------

    /*
     * Board width and height are loaded from the shared
     * configuration whenever a new game starts.
     *
     * They are not final because the player can change the
     * field size from the Configuration screen.
     */
    private static int ROWS;
    private static int COLS;

    private static final int CELL_SIZE = 29;

    /*
     * Temporary board reference retained for the existing AI
     * calculation and terminal debug output.
     *
     * PlayerGame owns the actual board state.
     */
    private static Color[][] board;

    /*
     * Represents Player One's independent game field.
     *
     * PlayerGame will gradually take ownership of the board
     * state and gameplay operations currently handled directly
     * by PlayScreen. Keeping the original fields temporarily
     * allows the migration to be tested in small safe steps.
     */
    private static PlayerGame playerOne;

    /*
     * Player Two exists only when Extended Mode is enabled.
     * Gameplay is intentionally connected in a later step; this
     * first stage establishes and tests the independent second field.
     */
    private static PlayerGame playerTwo;

    private static AnimationTimer timer;
    private static Tetromino currentPiece;

    /*
     * Player Two receives a separate Tetromino object in Extended Mode.
     * The type is shared with Player One, but movement/rotation state is not.
     */
    private static Tetromino currentPieceTwo;
    private static TetrominoFactory tetrominoFactory = new TetrominoFactory();

    /*
     * Both players consume the same ordered tetromino types independently.
     * Separate indexes allow either player to progress faster without
     * changing the sequence seen by the other player.
     */
    private static final List<TetrominoType> sharedSequence =
            new ArrayList<>();

    private static int playerOneSequenceIndex;
    private static int playerTwoSequenceIndex;

    // Pause / game-over state
    private static boolean paused = false;
    private static boolean gameOver = false;

    // ---------------------------------------------------------
    // INDEPENDENT AI STATE
    // ---------------------------------------------------------
    private static final TetrisAI tetrisAI = new TetrisAI();

    private static int playerOneAiTargetCol;
    private static int playerOneAiTargetRotation;
    private static int playerOneAiRotationsDone;
    private static boolean playerOneAiMoving;
    private static long playerOneLastAiStepTime;

    private static int playerTwoAiTargetCol;
    private static int playerTwoAiTargetRotation;
    private static int playerTwoAiRotationsDone;
    private static boolean playerTwoAiMoving;
    private static long playerTwoLastAiStepTime;

    private static final long AI_STEP_DELAY_NANOS =
            300_000_000L;

    public static void show(Stage stage) {

        // Reset state whenever a new game starts.
        paused = false;
        gameOver = false;

        // Start a fresh shared sequence whenever a new game begins.
        sharedSequence.clear();
        playerOneSequenceIndex = 0;
        playerTwoSequenceIndex = 0;

        playerOneAiMoving = false;
        playerTwoAiMoving = false;
        playerOneLastAiStepTime = 0;
        playerTwoLastAiStepTime = 0;

        /*
         * Load the selected board dimensions from the shared
         * configuration each time a new game is started.
         */
        GameConfig config = GameSettings.getConfig();

        COLS = config.getFieldWidth();
        ROWS = config.getFieldHeight();

        /*
         * Create Player One using the saved field dimensions
         * and controller type from the Configuration screen.
         *
         * The existing PlayScreen board remains active during
         * this migration so current gameplay is not disrupted.
         */
        playerOne =
                new PlayerGame(
                        config.getPlayerOneType(),
                        COLS,
                        ROWS
                );

        /*
         * Create an independent Player Two game state only for
         * Extended Mode. Single-player mode therefore keeps the
         * existing Player One behaviour unchanged.
         */
        boolean extendedMode =
                config.isExtendedMode();

        playerTwo =
                extendedMode
                        ? new PlayerGame(
                        config.getPlayerTwoType(),
                        COLS,
                        ROWS
                )
                        : null;

        /*
         * Keep a temporary reference to Player One's board for the
         * existing AI calculation and terminal debug output.
         */
        board = playerOne.getBoard();

        BorderPane root = new BorderPane();

        // ---------------------------------------------------------
        // CREATE PLAYER ONE GAME FIELD
        // ---------------------------------------------------------

        /*
         * PlayerGame now creates and owns Player One's visual
         * board cells and falling-piece layer.
         *
         * PlayScreen still controls the game loop and input while
         * the gameplay logic is migrated gradually to PlayerGame.
         */
        StackPane boardStack =
                playerOne.createGameField(
                        CELL_SIZE
                );

        /*
         * Extended Mode displays two independent fields side-by-side.
         * Player Two is visual-only in this step; movement and the shared
         * tetromino sequence are connected after the layout is verified.
         */
        StackPane boardStackTwo =
                extendedMode
                        ? playerTwo.createGameField(CELL_SIZE)
                        : null;

        Label playerOneLabel =
                createPlayerLabel("Player One");

        VBox playerOneArea =
                new VBox(
                        8,
                        playerOneLabel,
                        boardStack
                );

        playerOneArea.setAlignment(Pos.CENTER);

        HBox boardsArea =
                new HBox(24);

        boardsArea.setAlignment(Pos.CENTER);
        boardsArea.getChildren().add(playerOneArea);

        if (extendedMode) {

            Label playerTwoLabel =
                    createPlayerLabel("Player Two");

            VBox playerTwoArea =
                    new VBox(
                            8,
                            playerTwoLabel,
                            boardStackTwo
                    );

            playerTwoArea.setAlignment(Pos.CENTER);
            boardsArea.getChildren().add(playerTwoArea);
        }

        // ---------------------------------------------------------
        // CREATE FIRST TETROMINO
        // ---------------------------------------------------------

        /*
         * Generate the opening tetromino type once. Extended Mode then
         * creates two independent objects from that same type so both
         * players start with the same sequence without sharing state.
         */
        currentPiece =
                createPieceFromSharedSequence(
                        playerOneSequenceIndex
                );

        currentPieceTwo =
                extendedMode
                        ? createPieceFromSharedSequence(
                        playerTwoSequenceIndex
                )
                        : null;

        // ---------------------------------------------------------
        // PAUSE MESSAGE
        // ---------------------------------------------------------

        Label pauseMessage = new Label(
                "Game is paused.\nPress P to continue."
        );

        pauseMessage.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: rgba(0, 0, 0, 0.78);" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 18 12 18;"
        );

        pauseMessage.setTextAlignment(
                TextAlignment.CENTER
        );

        pauseMessage.setAlignment(Pos.CENTER);

        pauseMessage.setVisible(false);

        // Do not let the label interfere with mouse input.
        pauseMessage.setMouseTransparent(true);

        StackPane.setAlignment(
                pauseMessage,
                Pos.TOP_CENTER
        );

        StackPane.setMargin(
                pauseMessage,
                new Insets(35, 0, 0, 0)
        );

        boardStack.getChildren().add(pauseMessage);

        playerOne.drawFallingPiece(
                currentPiece,
                CELL_SIZE
        );

        /*
         * Player Two is visual-only at this checkpoint. Showing the opening
         * piece verifies that both fields received the same TetrominoType
         * before independent Player Two gameplay is connected.
         */
        if (extendedMode && currentPieceTwo != null) {

            playerTwo.drawFallingPiece(
                    currentPieceTwo,
                    CELL_SIZE
            );
        }


        // ---------------------------------------------------------
        // MAIN GAME LOOP
        // ---------------------------------------------------------

        // Stop an old timer if PlayScreen is opened again.
        if (timer != null) {
            timer.stop();
        }

        /*
         * Update Player One and Player Two independently on every frame.
         * No player's AI processing is allowed to return from handle(),
         * because doing so would freeze the other player's field.
         */
        timer = new AnimationTimer() {

            @Override
            public void handle(long now) {

                if (paused || gameOver) {
                    return;
                }

                updatePlayerOne(now);

                if (extendedMode &&
                        playerTwo != null &&
                        currentPieceTwo != null &&
                        !playerTwo.isGameOver()) {

                    updatePlayerTwo(now);
                }
            }
        };

        timer.start();

        /*
         * Place the board inside a fixed layout wrapper. Scaling a JavaFX node
         * changes its visual bounds but not the amount of layout space that
         * BorderPane reserves for it. The wrapper is resized later to the
         * board's displayed dimensions so large fields cannot push the Back
         * button below the visible window.
         */
        StackPane boardContainer =
                new StackPane(boardsArea);

        boardContainer.setAlignment(
                Pos.CENTER
        );

        root.setCenter(boardContainer);

        // ---------------------------------------------------------
        // BACK BUTTON
        // ---------------------------------------------------------

        Button backButton =
                new Button("Back");

        backButton.setPrefWidth(200);
        backButton.setPrefHeight(45);

        // Same blue styling as the Play button.
        backButton.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-weight: 900;" +
                        "-fx-background-color: #3a86ff;" +
                        "-fx-background-radius: 6px;"
        );

        // ---------------------------------------------------------
        // BACK / STOP GAME FUNCTION
        // ---------------------------------------------------------

        backButton.setOnAction(e -> {

            // If the game is already over, go straight back.
            if (gameOver) {

                if (timer != null) {
                    timer.stop();
                }

                MainMenu.show(stage);
                return;
            }

            /*
             * Remember whether the user had already paused
             * the game manually with P BEFORE clicking Back.
             */
            boolean wasAlreadyPaused = paused;

            /*
             * Temporarily pause the game while the
             * Stop Game confirmation is open.
             */
            paused = true;
            pauseMessage.setVisible(true);

            Alert confirmation =
                    new Alert(Alert.AlertType.CONFIRMATION);

            confirmation.initOwner(stage);

            confirmation.setTitle("Stop Game");
            confirmation.setHeaderText(null);

            confirmation.setContentText(
                    "Are you sure you want to stop the current game?"
            );

            ButtonType yesButton =
                    new ButtonType("Yes");

            ButtonType noButton =
                    new ButtonType("no");

            confirmation
                    .getButtonTypes()
                    .setAll(
                            yesButton,
                            noButton
                    );

            Optional<ButtonType> result =
                    confirmation.showAndWait();

            // -------------------------------------------------
            // YES = STOP GAME
            // -------------------------------------------------

            if (result.isPresent()
                    && result.get() == yesButton) {

                if (timer != null) {
                    timer.stop();
                }

                paused = false;
                pauseMessage.setVisible(false);

                MainMenu.show(stage);

            } else {

                // -------------------------------------------------
                // NO = RETURN TO GAME
                // -------------------------------------------------

                if (wasAlreadyPaused) {

                    /*
                     * The player had pressed P before clicking Back.
                     *
                     * Therefore the game must remain paused.
                     * The pause message stays visible.
                     */
                    paused = true;
                    pauseMessage.setVisible(true);

                } else {

                    /*
                     * The game was running before Back was clicked.
                     *
                     * Back only paused it temporarily for the alert.
                     * Therefore selecting No resumes automatically.
                     */
                    paused = false;
                    pauseMessage.setVisible(false);
                }
            }
        });

        VBox bottom =
                new VBox(backButton);

        bottom.setAlignment(Pos.CENTER);

        bottom.setPadding(
                new Insets(35, 0, 15, 0)
        );

        root.setBottom(bottom);

        // ---------------------------------------------------------
// DYNAMIC GAME WINDOW
// ---------------------------------------------------------

        /*
         * Get the usable screen dimensions so the gameplay window
         * does not extend beyond the user's visible screen area.
         */
        Rectangle2D screenBounds =
                Screen.getPrimary().getVisualBounds();

        double boardWidth =
                COLS * CELL_SIZE;

        double boardHeight =
                ROWS * CELL_SIZE;

        /*
         * Extended Mode must fit two fields horizontally as well as
         * vertically. Use one common scale so both players' fields remain
         * the same visible size and the Back button remains on screen.
         */
        double boardGap =
                extendedMode ? 24 : 0;

        double unscaledBoardsWidth =
                extendedMode
                        ? (boardWidth * 2) + boardGap
                        : boardWidth;

        double availableBoardHeight =
                screenBounds.getHeight() - 210;

        double availableBoardWidth =
                screenBounds.getWidth() - 120;

        double heightScale =
                availableBoardHeight / boardHeight;

        double widthScale =
                availableBoardWidth / unscaledBoardsWidth;

        double boardScale =
                Math.min(
                        1.0,
                        Math.min(
                                heightScale,
                                widthScale
                        )
                );

        boardStack.setScaleX(boardScale);
        boardStack.setScaleY(boardScale);

        if (boardStackTwo != null) {
            boardStackTwo.setScaleX(boardScale);
            boardStackTwo.setScaleY(boardScale);
        }

        double displayedBoardWidth =
                boardWidth * boardScale;

        double displayedBoardHeight =
                boardHeight * boardScale;

        /*
         * Scaling changes visual bounds but not JavaFX layout bounds.
         * Give each labelled player area the visible dimensions so the
         * two-board HBox does not reserve the original unscaled sizes.
         */
        playerOneArea.setMinWidth(displayedBoardWidth);
        playerOneArea.setPrefWidth(displayedBoardWidth);
        playerOneArea.setMaxWidth(displayedBoardWidth);

        if (extendedMode) {
            VBox playerTwoArea =
                    (VBox) boardsArea.getChildren().get(1);

            playerTwoArea.setMinWidth(displayedBoardWidth);
            playerTwoArea.setPrefWidth(displayedBoardWidth);
            playerTwoArea.setMaxWidth(displayedBoardWidth);
        }

        double displayedBoardsWidth =
                extendedMode
                        ? (displayedBoardWidth * 2) + boardGap
                        : displayedBoardWidth;

        /*
         * Include the player label above the board while reserving only
         * the visible scaled board height in the centre layout.
         */
        double displayedAreaHeight =
                displayedBoardHeight + 35;

        boardContainer.setMinSize(
                displayedBoardsWidth,
                displayedAreaHeight
        );

        boardContainer.setPrefSize(
                displayedBoardsWidth,
                displayedAreaHeight
        );

        boardContainer.setMaxSize(
                displayedBoardsWidth,
                displayedAreaHeight
        );

        /*
         * Automatically size the gameplay window according to
         * the configured field dimensions while keeping it within
         * the usable screen area.
         */
        double windowWidth =
                Math.min(
                        screenBounds.getWidth() - 80,
                        Math.max(
                                500,
                                displayedBoardsWidth + 120
                        )
                );

        double windowHeight =
                Math.min(
                        screenBounds.getHeight() - 40,
                        Math.max(
                                600,
                                displayedAreaHeight + 120
                        )
                );

        Scene scene =
                new Scene(
                        root,
                        windowWidth,
                        windowHeight
                );

        stage.setTitle(
                "Tetris - Play"
        );

        stage.setScene(scene);

        /*
         * Keep the explicitly calculated scene dimensions and centre the
         * gameplay window. Calling sizeToScene() here would allow the
         * unscaled board layout bounds to enlarge the stage again.
         */
        stage.centerOnScreen();
        // ---------------------------------------------------------
        // KEYBOARD CONTROLS
        // ---------------------------------------------------------

        /*
         * Event filter is used because JavaFX buttons can otherwise
         * take keyboard focus and interfere with arrow-key controls.
         */
        scene.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> {

                    switch (event.getCode()) {

                        // ---------------------------------------------
                        // P = PAUSE / RESUME
                        // ---------------------------------------------

                        case P -> {

                            // P does nothing after GAME OVER.
                            if (!gameOver) {

                                paused = !paused;

                                pauseMessage.setVisible(
                                        paused
                                );

                                if (paused) {

                                    System.out.println(
                                            "Game paused"
                                    );

                                } else {

                                    System.out.println(
                                            "Game resumed"
                                    );
                                }
                            }

                            event.consume();
                        }

                        // ---------------------------------------------
                        // M = TOGGLE BACKGROUND MUSIC
                        // ---------------------------------------------

                        case M -> {

                            /*
                             * Toggle the shared music setting so the change is
                             * applied immediately during gameplay and remains
                             * synchronized with the Configuration screen.
                             */
                            GameConfig musicConfig = GameSettings.getConfig();

                            musicConfig.setMusicEnabled(
                                    !musicConfig.isMusicEnabled()
                            );

                            // Apply the updated music setting immediately.
                            AudioManager.updateMusicState();

                            // Save the preference so it persists after restart.
                            GameSettings.save();

                            System.out.println(
                                    "Music: " +
                                            (musicConfig.isMusicEnabled() ? "On" : "Off")
                            );

                            event.consume();
                        }

// ---------------------------------------------
// S = TOGGLE SOUND EFFECTS
// ---------------------------------------------

                        case S -> {

                            /*
                             * Toggle the shared sound-effects setting.
                             * Sound effects added to gameplay can read this
                             * value before playing any effect.
                             */
                            GameConfig soundConfig = GameSettings.getConfig();

                            soundConfig.setSoundEnabled(
                                    !soundConfig.isSoundEnabled()
                            );

                            // Persist the updated sound preference.
                            GameSettings.save();

                            System.out.println(
                                    "Sound: " +
                                            (soundConfig.isSoundEnabled() ? "On" : "Off")
                            );

                            event.consume();
                        }
                        // ---------------------------------------------
                        // DOWN = MANUAL FAST DROP
                        // ---------------------------------------------

                        case DOWN -> {

                            if (!paused &&
                                    !gameOver &&
                                    playerOne.getPlayerType() == PlayerType.HUMAN &&
                                    playerOne.canMoveDown(currentPiece)) {

                                currentPiece.moveDown();

                                currentPiece.resetYOffset();

                                playerOne.drawFallingPiece(
                                        currentPiece,
                                        CELL_SIZE
                                );
                            }

                            event.consume();
                        }

                        // ---------------------------------------------
                        // LEFT
                        // ---------------------------------------------

                        case LEFT -> {

                            if (!paused &&
                                    !gameOver &&
                                    playerOne.getPlayerType() == PlayerType.HUMAN &&
                                    playerOne.canMoveLeft(currentPiece)) {

                                currentPiece.moveLeft();
                                // Play movement sound when the piece moves successfully.
                                AudioManager.playMoveTurnSound();

                                playerOne.drawFallingPiece(
                                        currentPiece,
                                        CELL_SIZE
                                );
                            }

                            event.consume();
                        }

                        // ---------------------------------------------
                        // RIGHT
                        // ---------------------------------------------

                        case RIGHT -> {

                            if (!paused &&
                                    !gameOver &&
                                    playerOne.getPlayerType() == PlayerType.HUMAN &&
                                    playerOne.canMoveRight(currentPiece)) {

                                currentPiece.moveRight();
                                // Play movement sound when the piece moves successfully.
                                AudioManager.playMoveTurnSound();

                                playerOne.drawFallingPiece(
                                        currentPiece,
                                        CELL_SIZE
                                );
                            }

                            event.consume();
                        }

                        // ---------------------------------------------
                        // UP = ROTATE
                        // ---------------------------------------------

                        case UP -> {

                            if (!paused &&
                                    !gameOver &&
                                    playerOne.getPlayerType() == PlayerType.HUMAN &&
                                    playerOne.canRotate(currentPiece)) {

                                currentPiece.setShape(
                                        currentPiece
                                                .getRotatedShape()
                                );
                                // Play rotation sound after a successful turn.
                                AudioManager.playMoveTurnSound();

                                playerOne.drawFallingPiece(
                                        currentPiece,
                                        CELL_SIZE
                                );
                            }

                            event.consume();
                        }

                        // ---------------------------------------------
                        // PLAYER TWO HUMAN CONTROLS
                        // L = LEFT, R = RIGHT, D = DOWN, W = ROTATE
                        // ---------------------------------------------

                        case L -> {

                            if (!paused &&
                                    !gameOver &&
                                    extendedMode &&
                                    playerTwo != null &&
                                    !playerTwo.isGameOver() &&
                                    playerTwo.getPlayerType() == PlayerType.HUMAN &&
                                    playerTwo.canMoveLeft(currentPieceTwo)) {

                                currentPieceTwo.moveLeft();
                                AudioManager.playMoveTurnSound();

                                playerTwo.drawFallingPiece(
                                        currentPieceTwo,
                                        CELL_SIZE
                                );
                            }

                            event.consume();
                        }

                        case R -> {

                            if (!paused &&
                                    !gameOver &&
                                    extendedMode &&
                                    playerTwo != null &&
                                    !playerTwo.isGameOver() &&
                                    playerTwo.getPlayerType() == PlayerType.HUMAN &&
                                    playerTwo.canMoveRight(currentPieceTwo)) {

                                currentPieceTwo.moveRight();
                                AudioManager.playMoveTurnSound();

                                playerTwo.drawFallingPiece(
                                        currentPieceTwo,
                                        CELL_SIZE
                                );
                            }

                            event.consume();
                        }

                        case D -> {

                            /*
                             * D moves Player Two down faster while S remains
                             * reserved for the in-game sound-effects toggle.
                             */
                            if (!paused &&
                                    !gameOver &&
                                    extendedMode &&
                                    playerTwo != null &&
                                    !playerTwo.isGameOver() &&
                                    playerTwo.getPlayerType() == PlayerType.HUMAN &&
                                    playerTwo.canMoveDown(currentPieceTwo)) {

                                currentPieceTwo.moveDown();
                                currentPieceTwo.resetYOffset();

                                playerTwo.drawFallingPiece(
                                        currentPieceTwo,
                                        CELL_SIZE
                                );
                            }

                            event.consume();
                        }

                        case W -> {

                            if (!paused &&
                                    !gameOver &&
                                    extendedMode &&
                                    playerTwo != null &&
                                    !playerTwo.isGameOver() &&
                                    playerTwo.getPlayerType() == PlayerType.HUMAN &&
                                    playerTwo.canRotate(currentPieceTwo)) {

                                currentPieceTwo.setShape(
                                        currentPieceTwo.getRotatedShape()
                                );

                                AudioManager.playMoveTurnSound();

                                playerTwo.drawFallingPiece(
                                        currentPieceTwo,
                                        CELL_SIZE
                                );
                            }

                            event.consume();
                        }

                        default -> {
                        }
                    }
                }
        );

        stage.show();
    }


    // -------------------------------------------------------------
    // INDEPENDENT PLAYER UPDATES
    // -------------------------------------------------------------

    private static void updatePlayerOne(long now) {

        if (playerOne.isGameOver() || currentPiece == null) {
            return;
        }

        if (playerOne.getPlayerType() == PlayerType.AI) {

            if (!playerOneAiMoving) {
                preparePlayerOneAiMove();
            }

            if (now - playerOneLastAiStepTime >= AI_STEP_DELAY_NANOS) {
                playerOneLastAiStepTime = now;
                performPlayerOneAiStep();
            }
        }

        /*
         * AI pieces use the existing faster fall speed. Human pieces
         * retain the normal smooth fall speed.
         */
        int fallSpeed =
                playerOne.getPlayerType() == PlayerType.AI
                        ? 5
                        : 1;

        if (playerOne.canMoveDown(currentPiece)) {

            currentPiece.addYOffset(fallSpeed);

            if (currentPiece.getYOffset() >= CELL_SIZE) {
                currentPiece.resetYOffset();
                currentPiece.moveDown();
            }

            playerOne.drawFallingPiece(
                    currentPiece,
                    CELL_SIZE
            );

        } else {
            landPlayerOnePiece();
        }
    }


    private static void updatePlayerTwo(long now) {

        if (playerTwo.isGameOver() || currentPieceTwo == null) {
            return;
        }

        if (playerTwo.getPlayerType() == PlayerType.AI) {

            if (!playerTwoAiMoving) {
                preparePlayerTwoAiMove();
            }

            if (now - playerTwoLastAiStepTime >= AI_STEP_DELAY_NANOS) {
                playerTwoLastAiStepTime = now;
                performPlayerTwoAiStep();
            }
        }

        int fallSpeed =
                playerTwo.getPlayerType() == PlayerType.AI
                        ? 5
                        : 1;

        if (playerTwo.canMoveDown(currentPieceTwo)) {

            currentPieceTwo.addYOffset(fallSpeed);

            if (currentPieceTwo.getYOffset() >= CELL_SIZE) {
                currentPieceTwo.resetYOffset();
                currentPieceTwo.moveDown();
            }

            playerTwo.drawFallingPiece(
                    currentPieceTwo,
                    CELL_SIZE
            );

        } else {
            landPlayerTwoPiece();
        }
    }


    // -------------------------------------------------------------
    // PLAYER ONE AI
    // -------------------------------------------------------------

    private static void preparePlayerOneAiMove() {

        int[] move =
                tetrisAI.findBestMove(
                        playerOne.getBoard(),
                        currentPiece
                );

        playerOneAiTargetCol = move[0];
        playerOneAiTargetRotation = move[1];
        playerOneAiRotationsDone = 0;
        playerOneAiMoving = true;
    }


    private static void performPlayerOneAiStep() {

        if (playerOneAiRotationsDone < playerOneAiTargetRotation) {

            if (playerOne.canRotate(currentPiece)) {

                currentPiece.setShape(
                        currentPiece.getRotatedShape()
                );

                playerOneAiRotationsDone++;
                AudioManager.playMoveTurnSound();

                playerOne.drawFallingPiece(
                        currentPiece,
                        CELL_SIZE
                );

                return;
            }

            playerOneAiRotationsDone =
                    playerOneAiTargetRotation;
        }

        if (currentPiece.getCol() < playerOneAiTargetCol &&
                playerOne.canMoveRight(currentPiece)) {

            currentPiece.moveRight();
            AudioManager.playMoveTurnSound();

            playerOne.drawFallingPiece(
                    currentPiece,
                    CELL_SIZE
            );

            return;
        }

        if (currentPiece.getCol() > playerOneAiTargetCol &&
                playerOne.canMoveLeft(currentPiece)) {

            currentPiece.moveLeft();
            AudioManager.playMoveTurnSound();

            playerOne.drawFallingPiece(
                    currentPiece,
                    CELL_SIZE
            );

            return;
        }

        playerOneAiMoving = false;
    }


    // -------------------------------------------------------------
    // PLAYER TWO AI
    // -------------------------------------------------------------

    private static void preparePlayerTwoAiMove() {

        int[] move =
                tetrisAI.findBestMove(
                        playerTwo.getBoard(),
                        currentPieceTwo
                );

        playerTwoAiTargetCol = move[0];
        playerTwoAiTargetRotation = move[1];
        playerTwoAiRotationsDone = 0;
        playerTwoAiMoving = true;
    }


    private static void performPlayerTwoAiStep() {

        if (playerTwoAiRotationsDone < playerTwoAiTargetRotation) {

            if (playerTwo.canRotate(currentPieceTwo)) {

                currentPieceTwo.setShape(
                        currentPieceTwo.getRotatedShape()
                );

                playerTwoAiRotationsDone++;
                AudioManager.playMoveTurnSound();

                playerTwo.drawFallingPiece(
                        currentPieceTwo,
                        CELL_SIZE
                );

                return;
            }

            playerTwoAiRotationsDone =
                    playerTwoAiTargetRotation;
        }

        if (currentPieceTwo.getCol() < playerTwoAiTargetCol &&
                playerTwo.canMoveRight(currentPieceTwo)) {

            currentPieceTwo.moveRight();
            AudioManager.playMoveTurnSound();

            playerTwo.drawFallingPiece(
                    currentPieceTwo,
                    CELL_SIZE
            );

            return;
        }

        if (currentPieceTwo.getCol() > playerTwoAiTargetCol &&
                playerTwo.canMoveLeft(currentPieceTwo)) {

            currentPieceTwo.moveLeft();
            AudioManager.playMoveTurnSound();

            playerTwo.drawFallingPiece(
                    currentPieceTwo,
                    CELL_SIZE
            );

            return;
        }

        playerTwoAiMoving = false;
    }


    // -------------------------------------------------------------
    // LANDING / NEXT PIECE
    // -------------------------------------------------------------

    private static void landPlayerOnePiece() {

        playerOne.lockPiece(currentPiece);
        playerOne.clearFallingPiece();

        int rowsRemoved =
                playerOne.eraseFullRows();

        if (rowsRemoved > 0) {
            AudioManager.playEraseLineSound();
        }

        printBoard();

        playerOneSequenceIndex++;

        Tetromino nextPiece =
                createPieceFromSharedSequence(
                        playerOneSequenceIndex
                );

        if (playerOne.canSpawn(nextPiece)) {

            currentPiece = nextPiece;
            playerOneAiMoving = false;

            playerOne.drawFallingPiece(
                    currentPiece,
                    CELL_SIZE
            );

        } else {

            playerOne.setGameOver(true);
            gameOver = true;
            paused = false;

            AudioManager.playGameFinishSound();

            if (timer != null) {
                timer.stop();
            }

            System.out.println("PLAYER ONE GAME OVER");
        }
    }


    private static void landPlayerTwoPiece() {

        playerTwo.lockPiece(currentPieceTwo);
        playerTwo.clearFallingPiece();

        int rowsRemoved =
                playerTwo.eraseFullRows();

        if (rowsRemoved > 0) {
            AudioManager.playEraseLineSound();
        }

        playerTwoSequenceIndex++;

        Tetromino nextPiece =
                createPieceFromSharedSequence(
                        playerTwoSequenceIndex
                );

        if (playerTwo.canSpawn(nextPiece)) {

            currentPieceTwo = nextPiece;
            playerTwoAiMoving = false;

            playerTwo.drawFallingPiece(
                    currentPieceTwo,
                    CELL_SIZE
            );

        } else {

            playerTwo.setGameOver(true);
            playerTwo.clearFallingPiece();

            System.out.println("PLAYER TWO GAME OVER");
        }
    }


    /*
     * Creates a simple heading for each field in Extended Mode.
     * Keeping the styling here avoids duplicating label setup.
     */
    private static Label createPlayerLabel(
            String text
    ) {

        Label label =
                new Label(text);

        label.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;"
        );

        return label;
    }


    // -------------------------------------------------------------
    // PRINT BOARD TO TERMINAL
    // -------------------------------------------------------------

    private static void printBoard() {

        System.out.println("Current board:");

        for (int row = 0; row < ROWS; row++) {

            for (int col = 0; col < COLS; col++) {

                if (board[row][col] == null) {
                    System.out.print("0 ");
                } else {
                    System.out.print("1 ");
                }
            }

            System.out.println();
        }

        System.out.println();
    }

    // -------------------------------------------------------------
    // SHARED TETROMINO SEQUENCE
    // -------------------------------------------------------------

    /*
     * Creates an independent tetromino object for the requested
     * position in the common sequence.
     *
     * A random type is generated only when that sequence position
     * does not exist yet. Therefore both players receive the same
     * type at index 0, 1, 2, and so on.
     */
    private static Tetromino createPieceFromSharedSequence(
            int sequenceIndex
    ) {

        while (sharedSequence.size() <= sequenceIndex) {

            sharedSequence.add(
                    tetrominoFactory.createRandomType()
            );
        }

        TetrominoType type =
                sharedSequence.get(sequenceIndex);

        return tetrominoFactory.createPiece(
                type,
                COLS
        );
    }



}