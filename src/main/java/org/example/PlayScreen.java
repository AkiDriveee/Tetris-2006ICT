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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;

import java.util.Optional;
import java.util.Random;

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
     * These arrays are created when PlayScreen opens so their
     * dimensions always match the configured field size.
     */
    private static Color[][] board;
    private static Region[][] cellViews;

    private static AnimationTimer timer;
    private static Tetromino currentPiece;
    private static Random random = new Random();
    private static TetrominoFactory tetrominoFactory = new TetrominoFactory();

    // Pause / game-over state
    private static boolean paused = false;
    private static boolean gameOver = false;

    //AI MODE
    private static TetrisAI tetrisAI = new TetrisAI();
    private static int aiTargetCol = 0;
    private static int aiTargetRotation = 0;
    private static int aiRotationsDone = 0;
    private static boolean aiMoving = false;
    private static long lastAiStepTime = 0;

    public static void show(Stage stage) {

        // Reset state whenever a new game starts.
        paused = false;
        gameOver = false;

        /*
         * Load the selected board dimensions from the shared
         * configuration each time a new game is started.
         */
        GameConfig config = GameSettings.getConfig();

        COLS = config.getFieldWidth();
        ROWS = config.getFieldHeight();

        /*
         * Recreate the board arrays so they exactly match
         * the configured width and height.
         */
        board = new Color[ROWS][COLS];
        cellViews = new Region[ROWS][COLS];

        BorderPane root = new BorderPane();
        // ---------------------------------------------------------
        // BUILD EMPTY 10 x 20 BOARD
        // ---------------------------------------------------------

        GridPane grid = new GridPane();

        grid.setStyle("-fx-background-color: black");
        grid.setMaxSize(
                Region.USE_PREF_SIZE,
                Region.USE_PREF_SIZE
        );

        for (int row = 0; row < ROWS; row++) {

            for (int col = 0; col < COLS; col++) {

                Region cell = new Region();

                cell.setPrefSize(
                        CELL_SIZE,
                        CELL_SIZE
                );

                cell.setStyle(
                        "-fx-background-color: #101010;" +
                                "-fx-border-color: #101010;" +
                                "-fx-border-width: 1;"
                );

                grid.add(cell, col, row);

                cellViews[row][col] = cell;

                // Clear data left from an earlier game.
                board[row][col] = null;
            }
        }

        // ---------------------------------------------------------
        // CREATE FIRST TETROMINO
        // ---------------------------------------------------------

        currentPiece = spawnRandomPiece();

        // Falling pieces are drawn on a separate pixel-based layer.
        // This allows smooth movement between grid rows.
        Pane pieceLayer = new Pane();

        pieceLayer.setPickOnBounds(false);

        pieceLayer.setPrefSize(
                COLS * CELL_SIZE,
                ROWS * CELL_SIZE
        );

        pieceLayer.setMaxSize(
                COLS * CELL_SIZE,
                ROWS * CELL_SIZE
        );

        StackPane boardStack =
                new StackPane(grid, pieceLayer);

        boardStack.setMaxSize(
                Region.USE_PREF_SIZE,
                Region.USE_PREF_SIZE
        );

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

        drawFallingPiece(
                currentPiece,
                pieceLayer
        );


        // ---------------------------------------------------------
        // MAIN GAME LOOP
        // ---------------------------------------------------------

        // Stop an old timer if PlayScreen is opened again.
        if (timer != null) {
            timer.stop();
        }

        timer = new AnimationTimer() {

            @Override
            public void handle(long now) {

                if (aiMoving) {

                    long delayNanos = 300_000_000; // 150 milliseconds between each AI step

                    if (now - lastAiStepTime < delayNanos) {
                        return; // not enough time has passed yet, wait
                    }

                    lastAiStepTime = now;

                    if (aiRotationsDone < aiTargetRotation) {

                        /*
                         * Apply the AI rotation only when the rotated tetromino
                         * remains inside the configured board and does not
                         * collide with an occupied cell.
                         */
                        if (canRotate(currentPiece)) {
                            currentPiece.setShape(
                                    currentPiece.getRotatedShape()
                            );

                            aiRotationsDone++;

                            // Play the sound only after a successful AI rotation.
                            AudioManager.playMoveTurnSound();

                            drawFallingPiece(
                                    currentPiece,
                                    pieceLayer
                            );

                            return;
                        }

                        /*
                         * The requested rotation cannot be performed safely.
                         * Skip the remaining rotations and continue positioning.
                         */
                        aiRotationsDone = aiTargetRotation;
                    }

                    if (currentPiece.getCol() < aiTargetCol) {

                        /*
                         * Use the same boundary/collision check as manual movement
                         * so the AI cannot move a tetromino outside the game board.
                         */
                        if (canMoveRight(currentPiece)) {
                            currentPiece.moveRight();

                            // Play the movement sound after a successful AI move.
                            AudioManager.playMoveTurnSound();

                            drawFallingPiece(currentPiece, pieceLayer);
                            return;
                        }

                    }

                    if (currentPiece.getCol() > aiTargetCol) {

                        /*
                         * Use the same boundary/collision check as manual movement
                         * so the AI cannot move a tetromino outside the game board.
                         */
                        if (canMoveLeft(currentPiece)) {
                            currentPiece.moveLeft();

                            // Play the movement sound after a successful AI move.
                            AudioManager.playMoveTurnSound();

                            drawFallingPiece(currentPiece, pieceLayer);
                            return;
                        }
                    }
                    // Finish AI positioning once no further safe horizontal move is required.
                    aiMoving = false;
                }


                // Completely freeze automatic movement while paused.
                if (paused || gameOver) {
                    return;
                }

                if (canMoveDown(currentPiece)) {

                    /*
                     * Move by one PIXEL rather than immediately moving
                     * by one entire board row.
                     *
                     * This provides the smooth normal downward movement
                     * required by the specification.
                     */

                    int fallSpeed = GameSettings.getConfig().isAiEnabled() ? 5 : 1;
                    currentPiece.addYOffset(fallSpeed);

                    if (currentPiece.getYOffset() >= CELL_SIZE) {

                        currentPiece.resetYOffset();

                        currentPiece.moveDown();
                    }

                    drawFallingPiece(
                            currentPiece,
                            pieceLayer
                    );

                } else {

                    // -------------------------------------------------
                    // PIECE HAS LANDED
                    // -------------------------------------------------

                    drawPiece(currentPiece);

                    pieceLayer
                            .getChildren()
                            .clear();

                    // Detect and erase ALL completed rows.
                    int rowsRemoved =
                            eraseFullRows();

                    if (rowsRemoved > 0) {

                        System.out.println(
                                "Rows removed: " +
                                        rowsRemoved
                        );
                    }

                    printBoard();

                    // Create the next piece.
                    Tetromino nextPiece =
                            spawnRandomPiece();

                    if (canSpawn(nextPiece)) {

                        currentPiece = nextPiece;

                        if (GameSettings.getConfig().isAiEnabled()) {
                            int[] move = tetrisAI.findBestMove(board, currentPiece);
                            aiTargetCol = move[0];
                            aiTargetRotation = move[1];
                            aiRotationsDone = 0;
                            aiMoving = true;

                            System.out.println("AI target col: " + aiTargetCol + ", rotation: " + aiTargetRotation);
                        }

                       /* if (GameSettings.getConfig().isAiEnabled()) {

                            int[] move = tetrisAI.findBestMove(board, currentPiece);

                            int targetCol = move[0];
                            int targetRotation = move[1];

                            for (int i = 0; i < targetRotation; i++) {
                                currentPiece.setShape(currentPiece.getRotatedShape());
                            }

                            while (currentPiece.getCol() < targetCol) {
                                currentPiece.moveRight();
                            }
                            while (currentPiece.getCol() > targetCol) {
                                currentPiece.moveLeft();
                            }

                            System.out.println("AI target col: " + targetCol + ", rotation: " + targetRotation);
                        }*/


                        drawFallingPiece(
                                currentPiece,
                                pieceLayer
                        );

                    } else {

                        // ---------------------------------------------
                        // GAME OVER
                        // ---------------------------------------------

                        System.out.println(
                                "GAME OVER"
                        );

                        gameOver = true;
                        paused = false;
                        // Play the game-finish sound once when the game ends.
                        AudioManager.playGameFinishSound();

                        pauseMessage.setVisible(false);

                        timer.stop();
                    }
                }
            }
        };

        timer.start();

        root.setCenter(boardStack);

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
         * Reserve vertical space for the Back button and other
         * layout spacing. If a configured board is too tall to fit,
         * visually scale the board while keeping the original
         * CELL_SIZE calculations used by the gameplay logic.
         */
        double availableBoardHeight =
                screenBounds.getHeight() - 170;

        double boardScale =
                Math.min(
                        1.0,
                        availableBoardHeight / boardHeight
                );

        boardStack.setScaleX(boardScale);
        boardStack.setScaleY(boardScale);

        /*
         * Calculate the visible dimensions of the board after
         * any required scaling has been applied.
         */
        double displayedBoardWidth =
                boardWidth * boardScale;

        double displayedBoardHeight =
                boardHeight * boardScale;

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
                                displayedBoardWidth + 180
                        )
                );

        double windowHeight =
                Math.min(
                        screenBounds.getHeight() - 40,
                        Math.max(
                                600,
                                displayedBoardHeight + 120
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
         * Apply the calculated size and centre the gameplay
         * window after the configured dimensions are applied.
         */
        stage.sizeToScene();
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
                                    canMoveDown(currentPiece)) {

                                currentPiece.moveDown();

                                currentPiece.resetYOffset();

                                drawFallingPiece(
                                        currentPiece,
                                        pieceLayer
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
                                    canMoveLeft(currentPiece)) {

                                currentPiece.moveLeft();
                                // Play movement sound when the piece moves successfully.
                                AudioManager.playMoveTurnSound();

                                drawFallingPiece(
                                        currentPiece,
                                        pieceLayer
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
                                    canMoveRight(currentPiece)) {

                                currentPiece.moveRight();
                                // Play movement sound when the piece moves successfully.
                                AudioManager.playMoveTurnSound();

                                drawFallingPiece(
                                        currentPiece,
                                        pieceLayer
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
                                    canRotate(currentPiece)) {

                                currentPiece.setShape(
                                        currentPiece
                                                .getRotatedShape()
                                );
                                // Play rotation sound after a successful turn.
                                AudioManager.playMoveTurnSound();

                                drawFallingPiece(
                                        currentPiece,
                                        pieceLayer
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
    // LOCK PIECE INTO BOARD
    // -------------------------------------------------------------

    private static void drawPiece(
            Tetromino piece
    ) {

        for (int r = 0;
             r < piece.getShape().length;
             r++) {

            for (int c = 0;
                 c < piece.getShape()[r].length;
                 c++) {

                if (piece.getShape()[r][c] == 1) {

                    int boardRow =
                            piece.getRow() + r;

                    int boardCol =
                            piece.getCol() + c;

                    String colorHex =
                            piece
                                    .getColor()
                                    .toString()
                                    .replace(
                                            "0x",
                                            "#"
                                    );

                    cellViews[boardRow][boardCol]
                            .setStyle(
                                    "-fx-background-color: " +
                                            colorHex +
                                            ";" +
                                            "-fx-border-color: #101010;" +
                                            "-fx-border-width: 1;"
                            );

                    /*
                     * Store the actual Color in the board.
                     * This is important because colours must
                     * remain correct when rows move down.
                     */
                    board[boardRow][boardCol] =
                            piece.getColor();
                }
            }
        }
    }

    // -------------------------------------------------------------
    // ERASE FULL ROWS
    // -------------------------------------------------------------

    /*
     * Detects and removes every completed row.
     *
     * Returns the total number of rows removed.
     */
    private static int eraseFullRows() {

        int rowsRemoved = 0;

        /*
         * Work upward from the bottom of the board.
         */
        for (int row = ROWS - 1;
             row >= 0;
             row--) {

            if (isFullRow(row)) {

                removeRow(row);

                rowsRemoved++;

                /*
                 * A row above has now moved into this
                 * same row position.
                 *
                 * Check this index again so consecutive
                 * full rows are also removed.
                 */
                row++;
            }
        }

        if (rowsRemoved > 0) {

            // Play the row-clear effect only when at least one row is removed.
            AudioManager.playEraseLineSound();

            refreshBoardView();
        }

        return rowsRemoved;
    }

    // -------------------------------------------------------------
    // CHECK FULL ROW
    // -------------------------------------------------------------

    private static boolean isFullRow(
            int row
    ) {

        for (Color cell : board[row]) {

            if (cell == null) {

                return false;
            }
        }

        return true;
    }

    // -------------------------------------------------------------
    // REMOVE ONE ROW
    // -------------------------------------------------------------

    /*
     * Removes a completed row and shifts everything
     * above it downward.
     *
     * The Color objects themselves are moved, which
     * preserves the original tetromino colours.
     */
    private static void removeRow(
            int row
    ) {

        for (int r = row;
             r > 0;
             r--) {

            for (int col = 0;
                 col < COLS;
                 col++) {

                board[r][col] =
                        board[r - 1][col];
            }
        }

        // Clear the newly-created top row.
        for (int col = 0;
             col < COLS;
             col++) {

            board[0][col] = null;
        }
    }

    // -------------------------------------------------------------
    // REFRESH BOARD AFTER ROW REMOVAL
    // -------------------------------------------------------------

    private static void refreshBoardView() {

        for (int row = 0;
             row < ROWS;
             row++) {

            for (int col = 0;
                 col < COLS;
                 col++) {

                Color cellColor =
                        board[row][col];

                if (cellColor == null) {

                    cellViews[row][col]
                            .setStyle(
                                    "-fx-background-color: #101010;" +
                                            "-fx-border-color: #101010;" +
                                            "-fx-border-width: 1;"
                            );

                } else {

                    String colorHex =
                            cellColor
                                    .toString()
                                    .replace(
                                            "0x",
                                            "#"
                                    );

                    cellViews[row][col]
                            .setStyle(
                                    "-fx-background-color: " +
                                            colorHex +
                                            ";" +
                                            "-fx-border-color: #101010;" +
                                            "-fx-border-width: 1;"
                            );
                }
            }
        }
    }

    // -------------------------------------------------------------
    // DRAW CURRENT FALLING PIECE
    // -------------------------------------------------------------

    private static void drawFallingPiece(
            Tetromino piece,
            Pane pieceLayer
    ) {

        pieceLayer
                .getChildren()
                .clear();

        for (int r = 0;
             r < piece.getShape().length;
             r++) {

            for (int c = 0;
                 c < piece.getShape()[r].length;
                 c++) {

                if (piece.getShape()[r][c] == 1) {
                    double x =
                            (piece.getCol() + c)
                                    * CELL_SIZE;
                    double y =
                            (piece.getRow() + r)
                                    * CELL_SIZE
                                    + piece.getYOffset();

                    Rectangle rect =
                            new Rectangle(
                                    CELL_SIZE,
                                    CELL_SIZE
                            );

                    rect.setX(x);
                    rect.setY(y);

                    rect.setFill(
                            piece.getColor()
                    );
                    rect.setStroke(
                            Color.web("#101010")
                    );

                    rect.setStrokeWidth(1);

                    pieceLayer
                            .getChildren()
                            .add(rect);
                }
            }
        }
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
// RANDOM TETROMINO
// -------------------------------------------------------------

    private static Tetromino spawnRandomPiece() {

        /*
         * Pass the configured board width to the factory so the
         * tetromino spawn position can adapt to dynamic field sizes.
         */
        return tetrominoFactory.createRandomPiece(COLS);
    }

    // -------------------------------------------------------------
// CAN MOVE DOWN?
// -------------------------------------------------------------

    private static boolean canMoveDown(
            Tetromino piece
    ) {

        for (int r = 0;
             r < piece.getShape().length;
             r++) {

            for (int c = 0;
                 c < piece.getShape()[r].length;
                 c++) {

                if (piece.getShape()[r][c] == 1) {

                    int boardRow =
                            piece.getRow()
                                    + r
                                    + 1;

                    int boardCol =
                            piece.getCol()
                                    + c;

                    /*
                     * Check all configured board boundaries before
                     * accessing the board array. This keeps movement
                     * safe when smaller dynamic field sizes are used.
                     */
                    if (boardRow < 0 ||
                            boardRow >= ROWS ||
                            boardCol < 0 ||
                            boardCol >= COLS) {

                        return false;
                    }

                    // Hit another piece.
                    if (board[boardRow][boardCol]
                            != null) {

                        return false;
                    }
                }
            }
        }

        return true;
    }

    // -------------------------------------------------------------
    // CAN MOVE LEFT
    // -------------------------------------------------------------

    private static boolean canMoveLeft(
            Tetromino piece
    ) {

        for (int r = 0;
             r < piece.getShape().length;
             r++) {

            for (int c = 0;
                 c < piece.getShape()[r].length;
                 c++) {

                if (piece.getShape()[r][c] == 1) {

                    int boardRow =
                            piece.getRow() + r;

                    int boardCol =
                            piece.getCol()
                                    + c
                                    - 1;

                    if (boardCol < 0) {
                        return false;
                    }

                    if (board[boardRow][boardCol]
                            != null) {

                        return false;
                    }
                }
            }
        }

        return true;
    }

    // -------------------------------------------------------------
    // MOVE RIGHT
    // -------------------------------------------------------------

    private static boolean canMoveRight(
            Tetromino piece
    ) {

        for (int r = 0;
             r < piece.getShape().length;
             r++) {

            for (int c = 0;
                 c < piece.getShape()[r].length;
                 c++) {

                if (piece.getShape()[r][c] == 1) {

                    int boardRow =
                            piece.getRow() + r;

                    int boardCol =
                            piece.getCol()
                                    + c
                                    + 1;

                    if (boardCol >= COLS) {
                        return false;
                    }

                    if (board[boardRow][boardCol]
                            != null) {

                        return false;
                    }
                }
            }
        }

        return true;
    }

    // -------------------------------------------------------------
    // CAN ROTATE?
    // -------------------------------------------------------------

    private static boolean canRotate(
            Tetromino piece
    ) {

        int[][] rotatedShape =
                piece.getRotatedShape();

        for (int r = 0;
             r < rotatedShape.length;
             r++) {

            for (int c = 0;
                 c < rotatedShape[r].length;
                 c++) {

                if (rotatedShape[r][c] == 1) {

                    int boardRow =
                            piece.getRow() + r;

                    int boardCol =
                            piece.getCol() + c;

                    if (boardCol < 0 ||
                            boardCol >= COLS) {

                        return false;
                    }

                    if (boardRow >= ROWS) {

                        return false;
                    }

                    if (board[boardRow][boardCol]
                            != null) {

                        return false;
                    }
                }
            }
        }

        return true;
    }

    // -------------------------------------------------------------
// CAN NEW PIECE SPAWN?
// -------------------------------------------------------------

    private static boolean canSpawn(
            Tetromino piece
    ) {

        for (int r = 0;
             r < piece.getShape().length;
             r++) {

            for (int c = 0;
                 c < piece.getShape()[r].length;
                 c++) {

                if (piece.getShape()[r][c] == 1) {

                    int boardRow =
                            piece.getRow() + r;

                    int boardCol =
                            piece.getCol() + c;

                    /*
                     * Check the configured board boundaries before
                     * accessing the board array. This prevents pieces
                     * from spawning outside smaller dynamic fields.
                     */
                    if (boardRow < 0 ||
                            boardRow >= ROWS ||
                            boardCol < 0 ||
                            boardCol >= COLS) {

                        return false;
                    }

                    if (board[boardRow][boardCol]
                            != null) {

                        return false;
                    }
                }
            }
        }

        return true;
    }

}