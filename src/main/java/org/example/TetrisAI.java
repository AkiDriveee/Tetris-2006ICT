package org.example;

import javafx.scene.paint.Color;
import org.example.pieces.Tetromino;

public class TetrisAI {

    public int evaluateBoard(Color[][] board) {

        int aggregateHeight = getAggregateHeight(board);
        int holesScore = getHoles(board);
        int bumpinessScore = getBumpiness(board);

        return (-2 * aggregateHeight)
                - (8 * holesScore)
                - (2 * bumpinessScore);
    }

    private int getAggregateHeight(Color[][] board) {

        int total = 0;

        for (int col = 0; col < board[0].length; col++) {
            total += getColumnHeight(board, col);
        }

        return total;
    }


    private int getHeight(Color[][] board) {

        int height = 0;

        for (int x = 0; x < board[0].length; x++) {

            for (int y = 0; y < board.length; y++) {

                if (board[y][x] != null) {

                    height = Math.max(
                            height,
                            board.length - y
                    );

                    break;
                }
            }
        }

        return height;
    }


    private int getHoles(Color[][] board) {

        int holes = 0;

        for (int x = 0; x < board[0].length; x++) {

            boolean blockFound = false;

            for (Color[] colors : board) {

                if (colors[x] != null) {

                    blockFound = true;

                } else if (blockFound) {

                    holes++;
                }
            }
        }

        return holes;
    }


    private int getClearedLines(Color[][] board) {

        int clearedLines = 0;

        for (Color[] colors : board) {

            boolean isLineFull = true;

            for (int x = 0; x < board[0].length; x++) {

                if (colors[x] == null) {

                    isLineFull = false;
                    break;
                }
            }

            if (isLineFull) {
                clearedLines++;
            }
        }

        return clearedLines;
    }


    private int getBumpiness(Color[][] board) {

        int bumpiness = 0;

        for (int x = 0;
             x < board[0].length - 1;
             x++) {

            int colHeight1 =
                    getColumnHeight(board, x);

            int colHeight2 =
                    getColumnHeight(board, x + 1);

            bumpiness +=
                    Math.abs(colHeight1 - colHeight2);
        }

        return bumpiness;
    }


    private int getColumnHeight(
            Color[][] board,
            int col
    ) {

        for (int y = 0; y < board.length; y++) {

            if (board[y][col] != null) {

                return board.length - y;
            }
        }

        return 0;
    }


    private boolean canPlace(
            int[][] shape,
            int row,
            int col,
            Color[][] board
    ) {

        for (int r = 0; r < shape.length; r++) {

            for (int c = 0;
                 c < shape[r].length;
                 c++) {

                if (shape[r][c] == 1) {

                    int boardRow = row + r;
                    int boardCol = col + c;

                    if (boardRow < 0 ||
                            boardRow >= board.length) {

                        return false;
                    }

                    if (boardCol < 0 ||
                            boardCol >= board[0].length) {

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


    private int findDropRow(
            int[][] shape,
            int startRow,
            int col,
            Color[][] board
    ) {

        if (!canPlace(
                shape,
                startRow,
                col,
                board
        )) {

            return -1;
        }

        int row = startRow;

        while (canPlace(
                shape,
                row + 1,
                col,
                board
        )) {

            row++;
        }

        return row;
    }


    private int clearCompletedLines(Color[][] board) {

        int cleared = 0;

        for (int row = board.length - 1; row >= 0; row--) {

            boolean full = true;

            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == null) {
                    full = false;
                    break;
                }
            }

            if (!full) {
                continue;
            }

            cleared++;

            for (int moveRow = row; moveRow > 0; moveRow--) {
                System.arraycopy(
                        board[moveRow - 1],
                        0,
                        board[moveRow],
                        0,
                        board[moveRow].length
                );
            }

            for (int col = 0; col < board[0].length; col++) {
                board[0][col] = null;
            }

            // Re-check this row because the row above has just moved into it.
            row++;
        }

        return cleared;
    }


    private Color[][] copyBoard(Color[][] board) {

        Color[][] newBoard =
                new Color[board.length][board[0].length];

        for (int r = 0; r < board.length; r++) {

            for (int c = 0;
                 c < board[r].length;
                 c++) {

                newBoard[r][c] =
                        board[r][c];
            }
        }

        return newBoard;
    }


    private int[][] rotateShape(int[][] shape) {

        int n = shape.length;

        int[][] rotated =
                new int[n][n];

        for (int r = 0; r < n; r++) {

            for (int c = 0; c < n; c++) {

                rotated[c][n - 1 - r] =
                        shape[r][c];
            }
        }

        return rotated;
    }


    private void placePiece(
            Color[][] board,
            int[][] shape,
            int row,
            int col,
            Color color
    ) {

        for (int r = 0; r < shape.length; r++) {

            for (int c = 0;
                 c < shape[r].length;
                 c++) {

                if (shape[r][c] == 1) {

                    board[row + r][col + c] =
                            color;
                }
            }
        }
    }


    /*
     * Checks whether the requested rotation can actually be
     * performed at the piece's current spawn position.
     */
    private boolean canReachRotation(
            int[][] startingShape,
            int rotationCount,
            int row,
            int col,
            Color[][] board
    ) {

        int[][] shape = startingShape;

        if (!canPlace(shape, row, col, board)) {
            return false;
        }

        for (int i = 0; i < rotationCount; i++) {

            shape = rotateShape(shape);

            if (!canPlace(
                    shape,
                    row,
                    col,
                    board
            )) {

                return false;
            }
        }

        return true;
    }


    /*
     * Checks whether the rotated piece can physically move from
     * its current column to the requested target column.
     *
     * This prevents the AI from choosing a destination that looks
     * good mathematically but cannot actually be reached.
     */
    private boolean canReachColumn(
            int[][] shape,
            int row,
            int startCol,
            int targetCol,
            Color[][] board
    ) {

        if (!canPlace(
                shape,
                row,
                startCol,
                board
        )) {

            return false;
        }

        int currentCol = startCol;

        if (targetCol < startCol) {

            while (currentCol > targetCol) {

                currentCol--;

                if (!canPlace(
                        shape,
                        row,
                        currentCol,
                        board
                )) {

                    return false;
                }
            }

        } else {

            while (currentCol < targetCol) {

                currentCol++;

                if (!canPlace(
                        shape,
                        row,
                        currentCol,
                        board
                )) {

                    return false;
                }
            }
        }

        return true;
    }


    public int[] findBestMove(
            Color[][] board,
            Tetromino piece
    ) {

        int bestScore = Integer.MIN_VALUE;

        /*
         * Fall back to the piece's current column if no better
         * reachable position can be found.
         */
        int bestCol = piece.getCol();
        int bestRotation = 0;

        int startRow = piece.getRow();
        int startCol = piece.getCol();

        int[][] originalShape =
                piece.getShape();

        int[][] shape =
                originalShape;

        for (int rotation = 0;
             rotation < 4;
             rotation++) {

            /*
             * First confirm that this rotation is physically
             * reachable at the real spawn position.
             */
            if (canReachRotation(
                    originalShape,
                    rotation,
                    startRow,
                    startCol,
                    board
            )) {

                for (int col = 0;
                     col < board[0].length;
                     col++) {

                    /*
                     * The piece must be able to travel from its
                     * real spawn column to this candidate column.
                     */
                    if (!canReachColumn(
                            shape,
                            startRow,
                            startCol,
                            col,
                            board
                    )) {

                        continue;
                    }

                    int dropRow =
                            findDropRow(
                                    shape,
                                    startRow,
                                    col,
                                    board
                            );

                    if (dropRow < 0) {
                        continue;
                    }

                    Color[][] simulatedBoard =
                            copyBoard(board);

                    placePiece(
                            simulatedBoard,
                            shape,
                            dropRow,
                            col,
                            piece.getColor()
                    );

                    /*
                     * Keep your original board evaluation for this
                     * test. We are changing reachability, not the
                     * AI personality/weights at the same time.
                     */
                    /*
                     * A real Tetris move removes completed rows before the next
                     * piece is evaluated. Simulate that same result here.
                     */
                    int completedLines =
                            clearCompletedLines(simulatedBoard);

                    int score =
                            evaluateBoard(simulatedBoard)
                                    + (20 * completedLines);

                    if (score > bestScore) {

                        bestScore = score;
                        bestCol = col;
                        bestRotation = rotation;
                    }
                }
            }

            shape = rotateShape(shape);
        }

        return new int[]{
                bestCol,
                bestRotation
        };
    }
}
