package org.example;

import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.PlayerType;
import org.example.Tetromino;

/*
 * Represents the independent game state for one Tetris player.
 *
 * Each player has their own board data, visual board cells,
 * dimensions, controller type, and game-over state. Keeping
 * this state separate allows Extended Mode to run two game
 * fields at the same time without sharing board data.
 */
public class PlayerGame {

    private final int rows;
    private final int cols;

    private final Color[][] board;
    private final Region[][] cellViews;

    /*
     * Separate visual layer used to draw the currently
     * falling tetromino above the locked board cells.
     */
    private Pane pieceLayer;

    private PlayerType playerType;
    private boolean gameOver;

    /*
     * Create an independent game state using the configured
     * field dimensions and selected controller type.
     */
    public PlayerGame(
            PlayerType playerType,
            int fieldWidth,
            int fieldHeight
    ) {

        this.playerType = playerType;

        this.cols = fieldWidth;
        this.rows = fieldHeight;

        this.board =
                new Color[rows][cols];

        this.cellViews =
                new Region[rows][cols];

        this.gameOver = false;
    }

    /*
     * Builds the visual grid for this player's game field.
     *
     * Each visual cell is stored in cellViews so it can later
     * be updated when tetrominoes are locked into the board
     * or completed rows are removed.
     */
    public GridPane createBoardGrid(
            int cellSize
    ) {

        GridPane grid = new GridPane();

        grid.setStyle(
                "-fx-background-color: black;"
        );

        grid.setMaxSize(
                Region.USE_PREF_SIZE,
                Region.USE_PREF_SIZE
        );

        for (int row = 0; row < rows; row++) {

            for (int col = 0; col < cols; col++) {

                Region cell = new Region();

                cell.setPrefSize(
                        cellSize,
                        cellSize
                );

                cell.setStyle(
                        "-fx-background-color: #101010;" +
                                "-fx-border-color: #101010;" +
                                "-fx-border-width: 1;"
                );

                grid.add(
                        cell,
                        col,
                        row
                );

                cellViews[row][col] = cell;

                /*
                 * Ensure a newly created visual board starts
                 * with no occupied gameplay cells.
                 */
                board[row][col] = null;
            }
        }

        return grid;
    }

    /*
     * Creates this player's complete visual game field.
     *
     * The GridPane displays locked tetrominoes while the
     * transparent Pane above it displays the currently
     * falling tetromino.
     */
    public StackPane createGameField(
            int cellSize
    ) {

        GridPane grid =
                createBoardGrid(cellSize);

        pieceLayer =
                new Pane();

        pieceLayer.setPickOnBounds(false);

        /*
         * Keep the overlay at exactly the same dimensions as the board.
         * A StackPane normally centres children whose layout sizes differ,
         * which can make the falling-piece coordinate origin drift away
         * from the GridPane origin. Fixed min/pref/max sizes plus TOP_LEFT
         * alignment guarantee that (0, 0) is identical for both layers.
         */
        double fieldWidth =
                cols * cellSize;

        double fieldHeight =
                rows * cellSize;

        pieceLayer.setMinSize(
                fieldWidth,
                fieldHeight
        );

        pieceLayer.setPrefSize(
                fieldWidth,
                fieldHeight
        );

        pieceLayer.setMaxSize(
                fieldWidth,
                fieldHeight
        );

        StackPane boardStack =
                new StackPane(
                        grid,
                        pieceLayer
                );

        StackPane.setAlignment(
                grid,
                Pos.TOP_LEFT
        );

        StackPane.setAlignment(
                pieceLayer,
                Pos.TOP_LEFT
        );

        boardStack.setMinSize(
                fieldWidth,
                fieldHeight
        );

        boardStack.setPrefSize(
                fieldWidth,
                fieldHeight
        );

        boardStack.setMaxSize(
                fieldWidth,
                fieldHeight
        );

        return boardStack;
    }

    /*
     * Draws the currently falling tetromino on this
     * player's independent falling-piece layer.
     */
    public void drawFallingPiece(
            Tetromino piece,
            int cellSize
    ) {

        if (pieceLayer == null) {
            return;
        }

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
                                    * cellSize;

                    double y =
                            (piece.getRow() + r)
                                    * cellSize
                                    + piece.getYOffset();

                    Rectangle rectangle =
                            new Rectangle(
                                    cellSize,
                                    cellSize
                            );

                    rectangle.setX(x);
                    rectangle.setY(y);

                    rectangle.setFill(
                            piece.getColor()
                    );

                    rectangle.setStroke(
                            Color.web("#101010")
                    );

                    rectangle.setStrokeWidth(1);

                    pieceLayer
                            .getChildren()
                            .add(rectangle);
                }
            }
        }
    }
    /*
     * Removes the active falling-piece graphics after
     * the tetromino has been locked into the board.
     */
    public void clearFallingPiece() {

        if (pieceLayer != null) {

            pieceLayer
                    .getChildren()
                    .clear();
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Color[][] getBoard() {
        return board;
    }

    public Region[][] getCellViews() {
        return cellViews;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(
            PlayerType playerType
    ) {
        this.playerType = playerType;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(
            boolean gameOver
    ) {
        this.gameOver = gameOver;
    }

    // ---------------------------------------------------------
// PIECE MOVEMENT VALIDATION
// ---------------------------------------------------------

    /*
     * Checks whether a tetromino can move one row downward
     * without leaving this player's board or colliding with
     * an already occupied cell.
     */
    public boolean canMoveDown(
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
                            piece.getRow() + r + 1;

                    int boardCol =
                            piece.getCol() + c;

                    if (boardRow < 0 ||
                            boardRow >= rows ||
                            boardCol < 0 ||
                            boardCol >= cols) {

                        return false;
                    }

                    if (board[boardRow][boardCol] != null) {

                        return false;
                    }
                }
            }
        }

        return true;
    }

    /*
     * Checks whether a tetromino can move one column
     * to the left on this player's board.
     */
    public boolean canMoveLeft(
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
                            piece.getCol() + c - 1;

                    if (boardRow < 0 ||
                            boardRow >= rows ||
                            boardCol < 0 ||
                            boardCol >= cols) {

                        return false;
                    }

                    if (board[boardRow][boardCol] != null) {

                        return false;
                    }
                }
            }
        }

        return true;
    }

    /*
     * Checks whether a tetromino can move one column
     * to the right on this player's board.
     */
    public boolean canMoveRight(
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
                            piece.getCol() + c + 1;

                    if (boardRow < 0 ||
                            boardRow >= rows ||
                            boardCol < 0 ||
                            boardCol >= cols) {

                        return false;
                    }

                    if (board[boardRow][boardCol] != null) {

                        return false;
                    }
                }
            }
        }

        return true;
    }

    /*
     * Checks whether the rotated form of a tetromino
     * fits safely within this player's game field.
     */
    public boolean canRotate(
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

                    if (boardRow < 0 ||
                            boardRow >= rows ||
                            boardCol < 0 ||
                            boardCol >= cols) {

                        return false;
                    }

                    if (board[boardRow][boardCol] != null) {

                        return false;
                    }
                }
            }
        }

        return true;
    }

    /*
     * Checks whether a newly generated tetromino can
     * safely enter this player's game field.
     */
    public boolean canSpawn(
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

                    if (boardRow < 0 ||
                            boardRow >= rows ||
                            boardCol < 0 ||
                            boardCol >= cols) {

                        return false;
                    }

                    if (board[boardRow][boardCol] != null) {

                        return false;
                    }
                }
            }
        }

        return true;
    }

    // ---------------------------------------------------------
// LOCK PIECE INTO BOARD
// ---------------------------------------------------------

    /*
     * Stores a landed tetromino in this player's board.
     * The corresponding visual cells are updated at the
     * same time so the locked piece remains visible.
     */
    public void lockPiece(
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
                     * Protect the board arrays from invalid
                     * positions before storing the piece.
                     */
                    if (boardRow < 0 ||
                            boardRow >= rows ||
                            boardCol < 0 ||
                            boardCol >= cols) {

                        continue;
                    }

                    board[boardRow][boardCol] =
                            piece.getColor();

                    updateCellStyle(
                            boardRow,
                            boardCol,
                            piece.getColor()
                    );
                }
            }
        }
    }

    // ---------------------------------------------------------
// COMPLETED ROW HANDLING
// ---------------------------------------------------------

    /*
     * Removes all completed rows from this player's board
     * and returns the number of rows that were erased.
     */
    public int eraseFullRows() {

        int rowsRemoved = 0;

        for (int row = rows - 1;
             row >= 0;
             row--) {

            if (isFullRow(row)) {

                removeRow(row);

                rowsRemoved++;

                /*
                 * Recheck the same position because the row
                 * above has now shifted into this index.
                 */
                row++;
            }
        }

        if (rowsRemoved > 0) {

            refreshBoardView();
        }

        return rowsRemoved;
    }

    /*
     * Returns true when every cell in the specified
     * row contains part of a locked tetromino.
     */
    private boolean isFullRow(
            int row
    ) {

        for (Color cell : board[row]) {

            if (cell == null) {

                return false;
            }
        }

        return true;
    }

    /*
     * Removes one completed row and shifts all rows
     * above it downward by one position.
     */
    private void removeRow(
            int row
    ) {

        for (int r = row;
             r > 0;
             r--) {

            for (int col = 0;
                 col < cols;
                 col++) {

                board[r][col] =
                        board[r - 1][col];
            }
        }

        /*
         * The top row becomes empty after all other
         * rows have shifted downward.
         */
        for (int col = 0;
             col < cols;
             col++) {

            board[0][col] = null;
        }
    }

    // ---------------------------------------------------------
// BOARD VIEW UPDATE
// ---------------------------------------------------------

    /*
     * Refreshes every visual cell so it matches the
     * current contents of this player's board array.
     */
    private void refreshBoardView() {

        for (int row = 0;
             row < rows;
             row++) {

            for (int col = 0;
                 col < cols;
                 col++) {

                updateCellStyle(
                        row,
                        col,
                        board[row][col]
                );
            }
        }
    }

    /*
     * Applies either the tetromino colour or the empty-cell
     * appearance to one visual board cell.
     */
    private void updateCellStyle(
            int row,
            int col,
            Color color
    ) {

        Region cell =
                cellViews[row][col];

        /*
         * A PlayerGame may exist before its visual grid has
         * been created, so avoid updating a missing cell.
         */
        if (cell == null) {
            return;
        }

        if (color == null) {

            cell.setStyle(
                    "-fx-background-color: #101010;" +
                            "-fx-border-color: #101010;" +
                            "-fx-border-width: 1;"
            );

            return;
        }

        String colorHex =
                color
                        .toString()
                        .replace(
                                "0x",
                                "#"
                        );

        cell.setStyle(
                "-fx-background-color: " +
                        colorHex +
                        ";" +
                        "-fx-border-color: #101010;" +
                        "-fx-border-width: 1;"
        );
    }
}

