package org.example;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import org.example.*;
import org.example.pieces.Tetromino;
import org.example.pieces.TetrominoFactory;
import org.example.server.TetrisServerConnection;

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

    /*
     * External players are driven by the target returned from TetrisServer.
     * Use a shorter controller step so the piece can rotate/move to the
     * requested position before descending, matching the tutor demo more
     * closely without changing the existing AI speed.
     */
    private static final long EXTERNAL_STEP_DELAY_NANOS =
            100_000_000L;

    /*
     * Prevent the missing-server warning from appearing repeatedly while
     * the same PlayScreen is open.
     */
    private static boolean externalServerWarningShown = false;

    // ---------------------------------------------------------
    // PLAYER ONE SCORE STATE
    // ---------------------------------------------------------
    private static ScoreLogic scoreLogic;
    private static Label scoreLabel;
    private static Label levelLabel;
    private static Label linesLabel;

    // Play-screen information labels for both players.
    private static Label musicStatusLabel;
    private static Label soundStatusLabel;
    private static Label playerOneNextLabel;
    private static Pane playerOneNextPreview;
    private static Label playerTwoScoreLabel;
    private static Label playerTwoLevelLabel;
    private static Label playerTwoLinesLabel;
    private static Label playerTwoNextLabel;
    private static Pane playerTwoNextPreview;
    private static ScoreLogic playerTwoScoreLogic;

    // Stage reference used for automatic per-player high-score dialogs.
    private static Stage gameStage;

    // Prevent a player's high-score dialog from being shown more than once.
    private static boolean playerOneHighScoreHandled;
    private static boolean playerTwoHighScoreHandled;

    public static void show(Stage stage) {

        // Reset state whenever a new game starts.
        paused = false;
        gameOver = false;
        externalServerWarningShown = false;
        gameStage = stage;
        playerOneHighScoreHandled = false;
        playerTwoHighScoreHandled = false;

        // Start Player One's score from the configured game level.
        scoreLogic = new ScoreLogic(
                GameSettings.getConfig().getGameLevel()
        );

        // Player Two needs independent score/level/line state in Extended Mode.
        playerTwoScoreLogic = new ScoreLogic(
                GameSettings.getConfig().getGameLevel()
        );

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

        // Observer Pattern: subscribe to Player One gameplay events.
        playerOne.addObserver(new GameEventLogger("Player 1"));

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

        if (playerTwo != null) {
            // Player Two publishes its events independently.
            playerTwo.addObserver(new GameEventLogger("Player 2"));
        }

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

        VBox playerOneInfo = createPlayerInfoPanel(
                "Game Info (Player 1)",
                playerOne.getPlayerType(),
                config.getGameLevel(),
                false
        );

        /*
         * Group the information panel and board inside one outlined card.
         * This keeps each player's complete game area visually together.
         */
        HBox playerOneArea = createPlayerGameArea(playerOneInfo, boardStack);

        HBox boardsArea = new HBox(24);
        boardsArea.setAlignment(Pos.CENTER);
        boardsArea.getChildren().add(playerOneArea);

        HBox playerTwoArea = null;

        if (extendedMode) {

            VBox playerTwoInfo = createPlayerInfoPanel(
                    "Game Info (Player 2)",
                    playerTwo.getPlayerType(),
                    config.getGameLevel(),
                    true
            );

            playerTwoArea = createPlayerGameArea(playerTwoInfo, boardStackTwo);
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

        updateNextTetrominoLabels();

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
        // MUSIC / SOUND STATUS
        // ---------------------------------------------------------
        musicStatusLabel = new Label();
        soundStatusLabel = new Label();
        updateAudioStatusLabels();

        String statusStyle =
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #222222;";

        musicStatusLabel.setStyle(statusStyle);
        soundStatusLabel.setStyle(statusStyle);

        HBox audioStatus = new HBox(18, musicStatusLabel, soundStatusLabel);
        audioStatus.setAlignment(Pos.CENTER);

        Label playHeading = new Label("Play");
        playHeading.setStyle(
                "-fx-font-size: 26px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #222222;"
        );

        VBox topArea = new VBox(4, playHeading, audioStatus);
        topArea.setAlignment(Pos.CENTER);
        topArea.setPadding(new Insets(10, 0, 8, 0));
        root.setTop(topArea);

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

                checkAndSaveHighScore(stage);
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

        double infoPanelWidth = 220;

        double unscaledBoardsWidth =
                extendedMode
                        ? ((boardWidth + infoPanelWidth) * 2) + boardGap
                        : boardWidth + infoPanelWidth;

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
        playerOneArea.setMinWidth(displayedBoardWidth + infoPanelWidth);
        playerOneArea.setPrefWidth(displayedBoardWidth + infoPanelWidth);
        playerOneArea.setMaxWidth(displayedBoardWidth + infoPanelWidth);

        if (extendedMode && playerTwoArea != null) {
            playerTwoArea.setMinWidth(displayedBoardWidth + infoPanelWidth);
            playerTwoArea.setPrefWidth(displayedBoardWidth + infoPanelWidth);
            playerTwoArea.setMaxWidth(displayedBoardWidth + infoPanelWidth);
        }

        double displayedBoardsWidth =
                extendedMode
                        ? ((displayedBoardWidth + infoPanelWidth) * 2) + boardGap
                        : displayedBoardWidth + infoPanelWidth;

        /*
         * Player One / Player Two are already identified inside each
         * Game Info panel, so no separate heading height is required.
         */
        double displayedAreaHeight =
                displayedBoardHeight;

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

        /*
         * Reserve vertical room for the Play/Music/Sound header and the
         * bottom Back button. The previous +120 allowance was too small,
         * so the Back button could be pushed below the visible window.
         */
        double windowHeight =
                Math.min(
                        screenBounds.getHeight() - 40,
                        Math.max(
                                600,
                                displayedAreaHeight + 210
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
                            updateAudioStatusLabels();

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
                            updateAudioStatusLabels();

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

                                GameCommand command =
                                        new MovePieceCommand(
                                                () -> {
                                                    currentPiece.moveDown();
                                                    currentPiece.resetYOffset();
                                                },
                                                () -> playerOne.drawFallingPiece(
                                                        currentPiece,
                                                        CELL_SIZE
                                                ),
                                                false
                                        );

                                command.execute();
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

                                GameCommand command =
                                        new MovePieceCommand(
                                                currentPiece::moveLeft,
                                                () -> playerOne.drawFallingPiece(
                                                        currentPiece,
                                                        CELL_SIZE
                                                ),
                                                true
                                        );

                                command.execute();
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

                                GameCommand command =
                                        new MovePieceCommand(
                                                currentPiece::moveRight,
                                                () -> playerOne.drawFallingPiece(
                                                        currentPiece,
                                                        CELL_SIZE
                                                ),
                                                true
                                        );

                                command.execute();
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

                                GameCommand command =
                                        new MovePieceCommand(
                                                () -> currentPiece.setShape(
                                                        currentPiece.getRotatedShape()
                                                ),
                                                () -> playerOne.drawFallingPiece(
                                                        currentPiece,
                                                        CELL_SIZE
                                                ),
                                                true
                                        );

                                command.execute();
                            }

                            event.consume();
                        }

                        // ---------------------------------------------
                        // PLAYER TWO HUMAN CONTROLS
                        // Z = LEFT, X = RIGHT, C = DOWN, V = ROTATE
                        // ---------------------------------------------

                        case Z -> {

                            if (!paused &&
                                    !gameOver &&
                                    extendedMode &&
                                    playerTwo != null &&
                                    !playerTwo.isGameOver() &&
                                    playerTwo.getPlayerType() == PlayerType.HUMAN &&
                                    playerTwo.canMoveLeft(currentPieceTwo)) {

                                GameCommand command =
                                        new MovePieceCommand(
                                                currentPieceTwo::moveLeft,
                                                () -> playerTwo.drawFallingPiece(
                                                        currentPieceTwo,
                                                        CELL_SIZE
                                                ),
                                                true
                                        );

                                command.execute();
                            }

                            event.consume();
                        }

                        case X -> {

                            if (!paused &&
                                    !gameOver &&
                                    extendedMode &&
                                    playerTwo != null &&
                                    !playerTwo.isGameOver() &&
                                    playerTwo.getPlayerType() == PlayerType.HUMAN &&
                                    playerTwo.canMoveRight(currentPieceTwo)) {

                                GameCommand command =
                                        new MovePieceCommand(
                                                currentPieceTwo::moveRight,
                                                () -> playerTwo.drawFallingPiece(
                                                        currentPieceTwo,
                                                        CELL_SIZE
                                                ),
                                                true
                                        );

                                command.execute();
                            }

                            event.consume();
                        }

                        case C -> {

                            /*
                             * C moves Player Two down faster while S remains
                             * reserved for the in-game sound-effects toggle.
                             */
                            if (!paused &&
                                    !gameOver &&
                                    extendedMode &&
                                    playerTwo != null &&
                                    !playerTwo.isGameOver() &&
                                    playerTwo.getPlayerType() == PlayerType.HUMAN &&
                                    playerTwo.canMoveDown(currentPieceTwo)) {

                                GameCommand command =
                                        new MovePieceCommand(
                                                () -> {
                                                    currentPieceTwo.moveDown();
                                                    currentPieceTwo.resetYOffset();
                                                },
                                                () -> playerTwo.drawFallingPiece(
                                                        currentPieceTwo,
                                                        CELL_SIZE
                                                ),
                                                false
                                        );

                                command.execute();
                            }

                            event.consume();
                        }

                        case V -> {

                            if (!paused &&
                                    !gameOver &&
                                    extendedMode &&
                                    playerTwo != null &&
                                    !playerTwo.isGameOver() &&
                                    playerTwo.getPlayerType() == PlayerType.HUMAN &&
                                    playerTwo.canRotate(currentPieceTwo)) {

                                GameCommand command =
                                        new MovePieceCommand(
                                                () -> currentPieceTwo.setShape(
                                                        currentPieceTwo.getRotatedShape()
                                                ),
                                                () -> playerTwo.drawFallingPiece(
                                                        currentPieceTwo,
                                                        CELL_SIZE
                                                ),
                                                true
                                        );

                                command.execute();
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

        PlayerType playerType = playerOne.getPlayerType();

        /*
         * External mode follows the server's requested rotation and X position.
         * Once aligned, each controller step moves the piece down one row.
         * This avoids the previous problem where the piece was falling quickly
         * at the same time as it was still trying to reach the server target.
         */
        if (playerType == PlayerType.EXTERNAL) {

            if (!playerOneAiMoving) {
                preparePlayerOneAutomatedMove();
            }

            if (playerOneAiMoving) {

                if (now - playerOneLastAiStepTime >= EXTERNAL_STEP_DELAY_NANOS) {
                    playerOneLastAiStepTime = now;
                    performPlayerOneExternalStep();
                }

                return;
            }

            /*
             * If TetrisServer is unavailable, keep the game alive and let
             * the tetromino fall at normal human speed instead of freezing.
             */
        }

        /*
         * Keep the existing local AI behaviour unchanged.
         */
        if (playerType == PlayerType.AI) {

            if (!playerOneAiMoving) {
                preparePlayerOneAutomatedMove();
            }

            if (playerOneAiMoving &&
                    now - playerOneLastAiStepTime >= AI_STEP_DELAY_NANOS) {

                playerOneLastAiStepTime = now;
                performPlayerOneAiStep();
            }
        }

        int fallSpeed =
                playerType == PlayerType.AI
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

        PlayerType playerType = playerTwo.getPlayerType();

        /*
         * Player Two External uses the same server-driven controller as
         * Player One, but operates on its own board and tetromino state.
         */
        if (playerType == PlayerType.EXTERNAL) {

            if (!playerTwoAiMoving) {
                preparePlayerTwoAutomatedMove();
            }

            if (playerTwoAiMoving) {

                if (now - playerTwoLastAiStepTime >= EXTERNAL_STEP_DELAY_NANOS) {
                    playerTwoLastAiStepTime = now;
                    performPlayerTwoExternalStep();
                }

                return;
            }

            /*
             * Missing server: Player Two also continues falling normally.
             */
        }

        if (playerType == PlayerType.AI) {

            if (!playerTwoAiMoving) {
                preparePlayerTwoAutomatedMove();
            }

            if (playerTwoAiMoving &&
                    now - playerTwoLastAiStepTime >= AI_STEP_DELAY_NANOS) {

                playerTwoLastAiStepTime = now;
                performPlayerTwoAiStep();
            }
        }

        int fallSpeed =
                playerType == PlayerType.AI
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

    private static void preparePlayerOneAutomatedMove() {

        int[] move;

        if (playerOne.getPlayerType() == PlayerType.EXTERNAL) {

            /*
             * Supply the external server with Player One's independent board,
             * current piece, and next piece from the shared sequence.
             */
            Tetromino nextPiece =
                    createPieceFromSharedSequence(
                            playerOneSequenceIndex + 1
                    );

            TetrisServerConnection serverConnection =
                    TetrisServerConnection.getInstance();

            move =
                    serverConnection.getServerMove(
                            playerOne.getBoard(),
                            currentPiece,
                            nextPiece
                    );

            if (!serverConnection.wasLastRequestSuccessful()) {
                playerOneAiMoving = false;
                showExternalServerWarning();
                return;
            }

        } else {

            move =
                    tetrisAI.findBestMove(
                            playerOne.getBoard(),
                            currentPiece
                    );
        }

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


    /*
     * Applies one server-controlled action for Player One.
     * Rotation is completed first, then horizontal positioning,
     * then the piece descends one row. A new server request is made
     * only after the piece lands and the next piece is spawned.
     */
    private static void performPlayerOneExternalStep() {

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

        if (playerOne.canMoveDown(currentPiece)) {

            currentPiece.resetYOffset();
            currentPiece.moveDown();

            playerOne.drawFallingPiece(
                    currentPiece,
                    CELL_SIZE
            );

        } else {
            landPlayerOnePiece();
        }
    }


    // -------------------------------------------------------------
    // PLAYER TWO AI
    // -------------------------------------------------------------

    private static void preparePlayerTwoAutomatedMove() {

        int[] move;

        if (playerTwo.getPlayerType() == PlayerType.EXTERNAL) {

            /*
             * Player Two uses its own board and sequence position while still
             * consuming the same ordered tetromino types as Player One.
             */
            Tetromino nextPiece =
                    createPieceFromSharedSequence(
                            playerTwoSequenceIndex + 1
                    );

            TetrisServerConnection serverConnection =
                    TetrisServerConnection.getInstance();

            move =
                    serverConnection.getServerMove(
                            playerTwo.getBoard(),
                            currentPieceTwo,
                            nextPiece
                    );

            if (!serverConnection.wasLastRequestSuccessful()) {
                playerTwoAiMoving = false;
                showExternalServerWarning();
                return;
            }

        } else {

            move =
                    tetrisAI.findBestMove(
                            playerTwo.getBoard(),
                            currentPieceTwo
                    );
        }

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


    /*
     * Applies one server-controlled action for Player Two using its
     * independent board, target, and falling tetromino.
     */
    private static void performPlayerTwoExternalStep() {

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

        if (playerTwo.canMoveDown(currentPieceTwo)) {

            currentPieceTwo.resetYOffset();
            currentPieceTwo.moveDown();

            playerTwo.drawFallingPiece(
                    currentPieceTwo,
                    CELL_SIZE
            );

        } else {
            landPlayerTwoPiece();
        }
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
            scoreLogic.addLinesCleared(rowsRemoved);
            scoreLabel.setText("Score: " + scoreLogic.getScore());
            levelLabel.setText("Current Level: " + scoreLogic.getLevel());
            linesLabel.setText("Line Erased: " + scoreLogic.getLinesErased());
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
            updateNextTetrominoLabels();

            playerOne.drawFallingPiece(
                    currentPiece,
                    CELL_SIZE
            );

        } else {

            // Only Player One is finished here. Do NOT stop the shared timer
            // while Player Two is still alive in Extended Mode.
            playerOne.setGameOver(true);
            playerOne.clearFallingPiece();
            playerOneAiMoving = false;
            currentPiece = null;

            AudioManager.playGameFinishSound();
            System.out.println("PLAYER ONE GAME OVER");

            final PlayerType finishedPlayerType = playerOne.getPlayerType();

            /*
             * Dialog.showAndWait() cannot be called directly from inside
             * AnimationTimer.handle(). Defer the high-score dialog until the
             * current animation pulse has finished.
             */
            Platform.runLater(() ->
                    checkAndSavePlayerHighScore(
                            1,
                            scoreLogic,
                            finishedPlayerType
                    )
            );

            finishWholeGameIfRequired();
        }
    }


    private static void landPlayerTwoPiece() {

        playerTwo.lockPiece(currentPieceTwo);
        playerTwo.clearFallingPiece();

        int rowsRemoved =
                playerTwo.eraseFullRows();

        if (rowsRemoved > 0) {
            AudioManager.playEraseLineSound();
            playerTwoScoreLogic.addLinesCleared(rowsRemoved);
            playerTwoScoreLabel.setText("Score: " + playerTwoScoreLogic.getScore());
            playerTwoLevelLabel.setText("Current Level: " + playerTwoScoreLogic.getLevel());
            playerTwoLinesLabel.setText("Line Erased: " + playerTwoScoreLogic.getLinesErased());
        }

        playerTwoSequenceIndex++;

        Tetromino nextPiece =
                createPieceFromSharedSequence(
                        playerTwoSequenceIndex
                );

        if (playerTwo.canSpawn(nextPiece)) {

            currentPieceTwo = nextPiece;
            playerTwoAiMoving = false;
            updateNextTetrominoLabels();

            playerTwo.drawFallingPiece(
                    currentPieceTwo,
                    CELL_SIZE
            );

        } else {

            // Only Player Two is finished here. Player One keeps updating
            // until it also reaches game over.
            playerTwo.setGameOver(true);
            playerTwo.clearFallingPiece();
            playerTwoAiMoving = false;
            currentPieceTwo = null;

            AudioManager.playGameFinishSound();
            System.out.println("PLAYER TWO GAME OVER");

            final PlayerType finishedPlayerType = playerTwo.getPlayerType();

            /*
             * Defer the modal high-score dialog for the same reason as
             * Player One: showAndWait() is illegal during AnimationTimer
             * processing.
             */
            Platform.runLater(() ->
                    checkAndSavePlayerHighScore(
                            2,
                            playerTwoScoreLogic,
                            finishedPlayerType
                    )
            );

            finishWholeGameIfRequired();
        }
    }


    /*
     * The AnimationTimer is shared by both fields. It must only be stopped
     * when the whole match is finished, never when just one player finishes.
     */
    private static void finishWholeGameIfRequired() {

        GameConfig config = GameSettings.getConfig();

        boolean wholeGameFinished;

        if (!config.isExtendedMode()) {
            wholeGameFinished = playerOne != null && playerOne.isGameOver();
        } else {
            wholeGameFinished =
                    playerOne != null &&
                            playerTwo != null &&
                            playerOne.isGameOver() &&
                            playerTwo.isGameOver();
        }

        if (wholeGameFinished) {
            gameOver = true;
            paused = false;

            if (timer != null) {
                timer.stop();
            }
        }
    }


    // -------------------------------------------------------------
    // PLAY-SCREEN INFORMATION PANELS
    // -------------------------------------------------------------

    private static VBox createPlayerInfoPanel(
            String heading,
            PlayerType playerType,
            int initialLevel,
            boolean playerTwoPanel
    ) {
        Label headingLabel = new Label(heading);
        Label typeLabel = new Label("Player Type: " + formatPlayerType(playerType));
        Label initialLevelLabel = new Label("Initial Level: " + initialLevel);
        Label currentLevelLabel = new Label("Current Level: " + initialLevel);
        Label erasedLabel = new Label("Line Erased: 0");
        Label currentScoreLabel = new Label("Score: 0");
        Label nextHeading = new Label("Next Tetromino:");
        Label nextLabel = new Label("-");

        // The letter label is retained internally as a fallback/reference.
        nextLabel.setVisible(false);
        nextLabel.setManaged(false);

        Pane nextPreview = new Pane();
        nextPreview.setMinSize(150, 95);
        nextPreview.setPrefSize(150, 95);
        nextPreview.setMaxSize(150, 95);
        nextPreview.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #c7ccd6;" +
                        "-fx-border-width: 1.5px;"
        );

        String normalStyle =
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: #222222;";

        headingLabel.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #222222;"
        );

        typeLabel.setStyle(normalStyle);
        initialLevelLabel.setStyle(normalStyle);
        currentLevelLabel.setStyle(normalStyle);
        erasedLabel.setStyle(normalStyle);

        nextHeading.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #222222;"
        );

        // Make the score the strongest item in the information panel.
        currentScoreLabel.setStyle(
                "-fx-font-size: 25px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-text-fill: #222222;"
        );

        if (playerTwoPanel) {
            playerTwoScoreLabel = currentScoreLabel;
            playerTwoLevelLabel = currentLevelLabel;
            playerTwoLinesLabel = erasedLabel;
            playerTwoNextLabel = nextLabel;
            playerTwoNextPreview = nextPreview;
        } else {
            scoreLabel = currentScoreLabel;
            levelLabel = currentLevelLabel;
            linesLabel = erasedLabel;
            playerOneNextLabel = nextLabel;
            playerOneNextPreview = nextPreview;
        }

        VBox panel = new VBox(
                16,
                headingLabel,
                typeLabel,
                initialLevelLabel,
                currentLevelLabel,
                erasedLabel,
                currentScoreLabel,
                nextHeading,
                nextPreview,
                nextLabel
        );

        panel.setAlignment(Pos.TOP_LEFT);
        panel.setPadding(new Insets(18));
        panel.setMinWidth(220);
        panel.setPrefWidth(220);

        // The outer player card now owns the blue border.
        panel.setStyle(
                "-fx-background-color: #f5f7fb;"
        );

        return panel;
    }


    /*
     * Creates one complete player card so the blue outline surrounds
     * both Game Info and the black gameplay board.
     */
    private static HBox createPlayerGameArea(
            VBox infoPanel,
            StackPane boardStack
    ) {
        HBox playerArea = new HBox(0, infoPanel, boardStack);
        playerArea.setAlignment(Pos.CENTER);

        playerArea.setStyle(
                "-fx-border-color: #3a86ff;" +
                        "-fx-border-width: 3px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;"
        );

        return playerArea;
    }


    private static String formatPlayerType(PlayerType playerType) {
        return switch (playerType) {
            case HUMAN -> "Human";
            case AI -> "AI";
            case EXTERNAL -> "External";
        };
    }


    private static void updateAudioStatusLabels() {
        GameConfig config = GameSettings.getConfig();

        if (musicStatusLabel != null) {
            musicStatusLabel.setText(
                    "Music: " + (config.isMusicEnabled() ? "ON" : "OFF")
            );
        }

        if (soundStatusLabel != null) {
            soundStatusLabel.setText(
                    "Sound: " + (config.isSoundEnabled() ? "ON" : "OFF")
            );
        }
    }


    private static void updateNextTetrominoLabels() {
        if (playerOneNextLabel != null) {
            TetrominoType nextType =
                    getSharedSequenceType(playerOneSequenceIndex + 1);

            playerOneNextLabel.setText(formatTetrominoType(nextType));
            drawNextTetrominoPreview(playerOneNextPreview, nextType);
        }

        if (playerTwo != null && playerTwoNextLabel != null) {
            TetrominoType nextType =
                    getSharedSequenceType(playerTwoSequenceIndex + 1);

            playerTwoNextLabel.setText(formatTetrominoType(nextType));
            drawNextTetrominoPreview(playerTwoNextPreview, nextType);
        }
    }


    /*
     * Draw the actual upcoming tetromino in a small preview box.
     * The same factory, shape and colour used by gameplay are reused here.
     */
    private static void drawNextTetrominoPreview(
            Pane previewPane,
            TetrominoType type
    ) {
        if (previewPane == null || type == null) {
            return;
        }

        previewPane.getChildren().clear();

        Tetromino previewPiece =
                tetrominoFactory.createPiece(type, COLS);

        int[][] shape = previewPiece.getShape();
        int previewCellSize = 24;

        int minRow = shape.length;
        int maxRow = -1;
        int minCol = Integer.MAX_VALUE;
        int maxCol = -1;

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != 0) {
                    minRow = Math.min(minRow, row);
                    maxRow = Math.max(maxRow, row);
                    minCol = Math.min(minCol, col);
                    maxCol = Math.max(maxCol, col);
                }
            }
        }

        if (maxRow < 0 || maxCol < 0) {
            return;
        }

        int shapeWidth =
                (maxCol - minCol + 1) * previewCellSize;

        int shapeHeight =
                (maxRow - minRow + 1) * previewCellSize;

        double startX =
                (previewPane.getPrefWidth() - shapeWidth) / 2.0;

        double startY =
                (previewPane.getPrefHeight() - shapeHeight) / 2.0;

        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {

                if (shape[row][col] == 0) {
                    continue;
                }

                Region cell = new Region();
                cell.setPrefSize(previewCellSize, previewCellSize);
                cell.setMinSize(previewCellSize, previewCellSize);
                cell.setMaxSize(previewCellSize, previewCellSize);

                cell.setBackground(
                        new Background(
                                new BackgroundFill(
                                        previewPiece.getColor(),
                                        CornerRadii.EMPTY,
                                        Insets.EMPTY
                                )
                        )
                );

                cell.setBorder(
                        new Border(
                                new BorderStroke(
                                        Color.rgb(255, 255, 255, 0.35),
                                        BorderStrokeStyle.SOLID,
                                        CornerRadii.EMPTY,
                                        new BorderWidths(0.5)
                                )
                        )
                );

                cell.setLayoutX(
                        startX + (col - minCol) * previewCellSize
                );

                cell.setLayoutY(
                        startY + (row - minRow) * previewCellSize
                );

                previewPane.getChildren().add(cell);
            }
        }
    }


    private static TetrominoType getSharedSequenceType(int sequenceIndex) {
        while (sharedSequence.size() <= sequenceIndex) {
            sharedSequence.add(tetrominoFactory.createRandomType());
        }

        return sharedSequence.get(sequenceIndex);
    }


    private static String formatTetrominoType(TetrominoType type) {
        return type == null ? "-" : type.name();
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



    // -------------------------------------------------------------
    // EXTERNAL SERVER WARNING
    // -------------------------------------------------------------

    private static void showExternalServerWarning() {

        if (externalServerWarningShown) {
            return;
        }

        externalServerWarningShown = true;

        Alert warning =
                new Alert(Alert.AlertType.INFORMATION);

        warning.setTitle("External Player");
        warning.setHeaderText(null);
        warning.setContentText(
                "You need to start TetrisServer to use external player mode."
        );

        warning.show();
    }


    // -------------------------------------------------------------
    // HIGH SCORE ENTRY
    // -------------------------------------------------------------

    /*
     * Called immediately when an individual player reaches game over.
     * In Extended Mode this does not end the other player's game.
     */
    private static void checkAndSavePlayerHighScore(
            int playerNumber,
            ScoreLogic playerScoreLogic,
            PlayerType playerType
    ) {

        if (playerScoreLogic == null) {
            return;
        }

        if (playerNumber == 1) {
            if (playerOneHighScoreHandled) {
                return;
            }
            playerOneHighScoreHandled = true;
        } else {
            if (playerTwoHighScoreHandled) {
                return;
            }
            playerTwoHighScoreHandled = true;
        }

        List<ScoreEntry> scores = HighScoreManager.load();

        // Always sort before checking the tenth-place score.
        scores.sort((a, b) -> Integer.compare(b.score(), a.score()));

        int finalScore = playerScoreLogic.getScore();

        boolean qualifies =
                scores.size() < 10 ||
                        finalScore > scores.get(scores.size() - 1).score();

        if (!qualifies) {
            return;
        }

        TextInputDialog nameDialog = new TextInputDialog();

        if (gameStage != null) {
            nameDialog.initOwner(gameStage);
        }

        nameDialog.setTitle("High Score");
        nameDialog.setHeaderText(null);
        nameDialog.setContentText(
                "Player " + playerNumber +
                        "'s score is in the top scores, please enter Player " +
                        playerNumber + "'s name:"
        );

        Optional<String> result = nameDialog.showAndWait();

        String playerName =
                result.isPresent() && !result.get().isBlank()
                        ? result.get().trim()
                        : "Player " + playerNumber;

        GameConfig config = GameSettings.getConfig();

        String modeDescription =
                config.isExtendedMode()
                        ? "Extended"
                        : "Single";

        String configDescription =
                COLS + "x" + ROWS +
                        "(" + config.getGameLevel() + ") " +
                        playerType + " " +
                        modeDescription;

        scores.add(
                new ScoreEntry(
                        playerName,
                        finalScore,
                        configDescription
                )
        );

        scores.sort(
                (a, b) -> Integer.compare(
                        b.score(),
                        a.score()
                )
        );

        if (scores.size() > 10) {
            scores =
                    new ArrayList<>(
                            scores.subList(0, 10)
                    );
        }

        HighScoreManager.save(scores);
    }


    /*
     * Retained for the Back-button path after a completed game.
     * Scores are already handled automatically at each player's game over,
     * so this method only handles any exceptional unhandled score and then
     * returns to the Main Menu.
     */
    private static void checkAndSaveHighScore(Stage stage) {

        if (playerOne != null &&
                playerOne.isGameOver() &&
                !playerOneHighScoreHandled) {

            checkAndSavePlayerHighScore(
                    1,
                    scoreLogic,
                    playerOne.getPlayerType()
            );
        }

        if (playerTwo != null &&
                playerTwo.isGameOver() &&
                !playerTwoHighScoreHandled) {

            checkAndSavePlayerHighScore(
                    2,
                    playerTwoScoreLogic,
                    playerTwo.getPlayerType()
            );
        }

        MainMenu.show(stage);
    }

}
