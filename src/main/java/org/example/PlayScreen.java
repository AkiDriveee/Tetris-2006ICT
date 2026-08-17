package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javafx.animation.AnimationTimer;
import javafx.scene.shape.Rectangle;

import org.example.pieces.*;
import java.util.Random;

import javafx.scene.input.KeyEvent;

public class PlayScreen {

    private static final int ROWS = 20;
    private static final int COLS = 10;
    private static final int CELL_SIZE = 25; // size of each square in pixels

    private static Color[][] board = new Color[ROWS][COLS]; // what's filled on the board (null = empty)
    private static Region[][] cellViews = new Region[ROWS][COLS]; // the actual squares shown on screen
    private static AnimationTimer timer; // controls the falling loop
    private static Tetromino currentPiece; // the piece falling right now
    private static Random random = new Random();

    public static void show(Stage stage) {
        BorderPane root = new BorderPane();

        // Build the empty board and reset old data
        GridPane grid = new GridPane();
        grid.setStyle("-fx-background-color: black");
        grid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Region cell = new Region();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle("-fx-background-color: #101010; -fx-border-color:#101010; -fx-border-width: 1;");
                grid.add(cell, col, row);
                cellViews[row][col] = cell;
                board[row][col] = null; // clear leftover data from the last game
            }
        }

        currentPiece = spawnRandomPiece();

        // Layer for the falling piece, sits on top of the board
        // We use pixels here (not grid cells) so it can fall smoothly
        Pane pieceLayer = new Pane();
        pieceLayer.setPickOnBounds(false);
        pieceLayer.setPrefSize(COLS * CELL_SIZE, ROWS * CELL_SIZE);
        pieceLayer.setMaxSize(COLS * CELL_SIZE, ROWS * CELL_SIZE);

        StackPane boardStack = new StackPane(grid, pieceLayer);
        boardStack.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        drawFallingPiece(currentPiece, pieceLayer);

        // Main game loop - runs every frame
        if (timer != null) {
            timer.stop(); // stop the old game if one's still running
        }

        timer = new AnimationTimer() {
            private boolean landed = false; // true once the game is over

            @Override
            public void handle(long now) {
                if (landed) return;

                if (canMoveDown(currentPiece)) {
                    currentPiece.addYOffset(1); // move down a tiny bit for a smooth fall

                    if (currentPiece.getYOffset() >= CELL_SIZE) {
                        currentPiece.resetYOffset();
                        currentPiece.moveDown(); // actually move to the next row
                    }

                    drawFallingPiece(currentPiece, pieceLayer);
                } else {
                    // piece can't fall anymore, lock it in place
                    drawPiece(currentPiece);
                    pieceLayer.getChildren().clear();

                    // ADDED: detect and erase completed rows before the next piece spawns
                    int rowsRemoved = eraseFullRows();

                    if (rowsRemoved > 0) {
                        System.out.println("Rows removed: " + rowsRemoved);
                    }

                    Tetromino nextPiece = spawnRandomPiece();

                    if (canSpawn(nextPiece)) {
                        currentPiece = nextPiece;
                    } else {
                        // no room for a new piece = game over
                        // TODO: show a real Game Over screen instead of this printout
                        System.out.println("GAME OVER");
                        landed = true;
                    }
                }
            }
        };

        timer.start();
        root.setCenter(boardStack);

        // Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> MainMenu.show(stage));
        VBox bottom = new VBox(backButton);
        bottom.setAlignment(Pos.TOP_CENTER);
        bottom.setPadding(new Insets(20));
        root.setBottom(bottom);

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("Tetris - Play");
        stage.setScene(scene);

        // Listens for arrow key presses
        // (using a filter here since buttons can steal arrow key presses otherwise)
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case DOWN -> {
                    // drop down fast
                    if (canMoveDown(currentPiece)) {
                        currentPiece.moveDown();
                        currentPiece.resetYOffset();
                    }
                }
                case LEFT -> {
                    if (canMoveLeft(currentPiece)) currentPiece.moveLeft();
                }
                case RIGHT -> {
                    if (canMoveRight(currentPiece)) currentPiece.moveRight();
                }
                case UP -> {
                    // rotate, but only if it's a legal spot to rotate into
                    if (canRotate(currentPiece)) {
                        currentPiece.setShape(currentPiece.getRotatedShape());
                    }
                }
                default -> {}
            }

            drawFallingPiece(currentPiece, pieceLayer);
        });

        stage.show();
    }

    // Locks a piece into the board once it's landed
    private static void drawPiece(Tetromino piece) {
        for (int r = 0; r < piece.getShape().length; r++) {
            for (int c = 0; c < piece.getShape()[r].length; c++) {
                if (piece.getShape()[r][c] == 1) {
                    int boardRow = piece.getRow() + r;
                    int boardCol = piece.getCol() + c;

                    String colorHex = piece.getColor().toString().replace("0x", "#"); // fix color format for CSS

                    cellViews[boardRow][boardCol].setStyle(
                            "-fx-background-color: " + colorHex + "; -fx-border-color: #101010; -fx-border-width: 1;"
                    );
                    board[boardRow][boardCol] = piece.getColor();
                }
            }
        }
    }

    // ADDED: detects and removes all completed rows.
    // Returns how many rows were removed.
    private static int eraseFullRows() {
        int rowsRemoved = 0;

        for (int row = ROWS - 1; row >= 0; row--) {
            if (isFullRow(row)) {
                removeRow(row);
                rowsRemoved++;

                // A row above has moved into this same position,
                // so check this row index again.
                row++;
            }
        }

        if (rowsRemoved > 0) {
            refreshBoardView();
        }

        return rowsRemoved;
    }

    // ADDED: a row is full only when every board cell contains a block.
    private static boolean isFullRow(int row) {
        for (Color cell : board[row]) {
            if (cell == null) {
                return false;
            }
        }

        return true;
    }

    // ADDED: removes one full row and moves all rows above it down by one.
    // The existing Color values move with the blocks, preserving their colours.
    private static void removeRow(int row) {
        for (int r = row; r > 0; r--) {
            for (int col = 0; col < COLS; col++) {
                board[r][col] = board[r - 1][col];
            }
        }

        // Clear the new top row.
        for (int col = 0; col < COLS; col++) {
            board[0][col] = null;
        }
    }

    // ADDED: updates the JavaFX grid after rows have been shifted.
    private static void refreshBoardView() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Color cellColor = board[row][col];

                if (cellColor == null) {
                    cellViews[row][col].setStyle(
                            "-fx-background-color: #101010; -fx-border-color: #101010; -fx-border-width: 1;"
                    );
                } else {
                    String colorHex = cellColor.toString().replace("0x", "#");

                    cellViews[row][col].setStyle(
                            "-fx-background-color: " + colorHex + "; -fx-border-color: #101010; -fx-border-width: 1;"
                    );
                }
            }
        }
    }

    // Draws the piece that's currently falling
    private static void drawFallingPiece(Tetromino piece, Pane pieceLayer) {
        pieceLayer.getChildren().clear(); // wipe old drawing first

        for (int r = 0; r < piece.getShape().length; r++) {
            for (int c = 0; c < piece.getShape()[r].length; c++) {
                if (piece.getShape()[r][c] == 1) {
                    double x = (piece.getCol() + c) * CELL_SIZE;
                    double y = (piece.getRow() + r) * CELL_SIZE + piece.getYOffset(); // yOffset = smooth falling nudge

                    Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                    rect.setX(x);
                    rect.setY(y);
                    rect.setFill(piece.getColor());
                    rect.setStroke(Color.web("#101010"));
                    rect.setStrokeWidth(1);

                    pieceLayer.getChildren().add(rect);
                }
            }
        }
    }

    // Picks a random tetromino piece to spawn
    private static Tetromino spawnRandomPiece() {
        int type = random.nextInt(7);

        return switch (type) {
            case 0 -> new IPiece(0, 3);
            case 1 -> new OPiece(0, 3);
            case 2 -> new TPiece(0, 3);
            case 3 -> new SPiece(0, 3);
            case 4 -> new ZPiece(0, 3);
            case 5 -> new JPiece(0, 3);
            default -> new LPiece(0, 3);
        };
    }

    // Checks if the piece can move down one row
    private static boolean canMoveDown(Tetromino piece) {
        for (int r = 0; r < piece.getShape().length; r++) {
            for (int c = 0; c < piece.getShape()[r].length; c++) {
                if (piece.getShape()[r][c] == 1) {
                    int boardRow = piece.getRow() + r + 1;
                    int boardCol = piece.getCol() + c;

                    if (boardRow >= ROWS) return false; // hit the floor
                    if (board[boardRow][boardCol] != null) return false; // hit another piece
                }
            }
        }

        return true;
    }

    // Checks if the piece can move one column left
    private static boolean canMoveLeft(Tetromino piece) {
        for (int r = 0; r < piece.getShape().length; r++) {
            for (int c = 0; c < piece.getShape()[r].length; c++) {
                if (piece.getShape()[r][c] == 1) {
                    int boardRow = piece.getRow() + r;
                    int boardCol = piece.getCol() + c - 1;

                    if (boardCol < 0) return false; // hit the left wall
                    if (board[boardRow][boardCol] != null) return false; // hit another piece
                }
            }
        }

        return true;
    }

    // Checks if the piece can move one column right
    private static boolean canMoveRight(Tetromino piece) {
        for (int r = 0; r < piece.getShape().length; r++) {
            for (int c = 0; c < piece.getShape()[r].length; c++) {
                if (piece.getShape()[r][c] == 1) {
                    int boardRow = piece.getRow() + r;
                    int boardCol = piece.getCol() + c + 1;

                    if (boardCol >= COLS) return false; // hit the right wall
                    if (board[boardRow][boardCol] != null) return false; // hit another piece
                }
            }
        }

        return true;
    }

    // Checks if it's safe to rotate before actually rotating
    private static boolean canRotate(Tetromino piece) {
        int[][] rotatedShape = piece.getRotatedShape();

        for (int r = 0; r < rotatedShape.length; r++) {
            for (int c = 0; c < rotatedShape[r].length; c++) {
                if (rotatedShape[r][c] == 1) {
                    int boardRow = piece.getRow() + r;
                    int boardCol = piece.getCol() + c;

                    if (boardCol < 0 || boardCol >= COLS) return false; // hits a wall
                    if (boardRow >= ROWS) return false; // hits the floor
                    if (board[boardRow][boardCol] != null) return false; // overlaps another piece
                }
            }
        }

        return true;
    }

    // Checks if a new piece has room to spawn - if not, it's game over
    private static boolean canSpawn(Tetromino piece) {
        for (int r = 0; r < piece.getShape().length; r++) {
            for (int c = 0; c < piece.getShape()[r].length; c++) {
                if (piece.getShape()[r][c] == 1) {
                    int boardRow = piece.getRow() + r;
                    int boardCol = piece.getCol() + c;

                    if (board[boardRow][boardCol] != null) return false; // no space to spawn
                }
            }
        }

        return true;
    }
}