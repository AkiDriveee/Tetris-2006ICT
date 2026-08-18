package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

import org.example.pieces.*;

import java.util.Optional;
import java.util.Random;

public class PlayScreen {

    private static final int ROWS = 20;
    private static final int COLS = 10;
    private static final int CELL_SIZE = 29;

    private static Color[][] board = new Color[ROWS][COLS];
    private static Region[][] cellViews = new Region[ROWS][COLS];

    private static AnimationTimer timer;
    private static Tetromino currentPiece;
    private static Random random = new Random();

    // Pause / game-over state
    private static boolean paused = false;
    private static boolean gameOver = false;

    public static void show(Stage stage) {

        // Reset state whenever a new game starts.
        paused = false;
        gameOver = false;

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
                    currentPiece.addYOffset(1);

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

                    // Create the next piece.
                    Tetromino nextPiece =
                            spawnRandomPiece();

                    if (canSpawn(nextPiece)) {

                        currentPiece = nextPiece;

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

            ButtonType noButton =
                    new ButtonType("No");

            ButtonType yesButton =
                    new ButtonType("Yes");

            confirmation
                    .getButtonTypes()
                    .setAll(
                            noButton,
                            yesButton
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
        // SCENE
        // ---------------------------------------------------------

        Scene scene =
                new Scene(
                        root,
                        1000,
                        700
                );

        stage.setTitle(
                "Tetris - Play"
        );

        stage.setScene(scene);

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

                    /*
                     * yOffset gives the intermediate
                     * pixel positions required for smooth
                     * automatic downward movement.
                     */
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
    // RANDOM TETROMINO
    // -------------------------------------------------------------

    private static Tetromino spawnRandomPiece() {

        int type =
                random.nextInt(7);

        return switch (type) {

            case 0 ->
                    new IPiece(0, 3);

            case 1 ->
                    new OPiece(0, 3);

            case 2 ->
                    new TPiece(0, 3);

            case 3 ->
                    new SPiece(0, 3);

            case 4 ->
                    new ZPiece(0, 3);

            case 5 ->
                    new JPiece(0, 3);

            default ->
                    new LPiece(0, 3);
        };
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

                    // Hit floor.
                    if (boardRow >= ROWS) {
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
    // CAN MOVE LEFT?
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
    // CAN MOVE RIGHT?
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