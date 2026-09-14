package org.example;
import javafx.scene.paint.Color;
import org.example.pieces.Tetromino;

public class TetrisAI {

    public int evaluateBoard(Color[][] board )
    {
        int heightScore = getHeight(board);
        int holesScore = getHoles(board);
        int linesCleared = getClearedLines(board);
        int bumpinessScore = getBumpiness(board);

        return (-4 * heightScore) + (3 * linesCleared) - (5 * holesScore) - (2 * bumpinessScore);
    }


    private int getHeight(Color[][] board)
    {
        int height = 0;
        for (int x = 0; x <board[0].length; x++){
            for(int y = 0; y <board.length; y++)
            {
                if(board[y][x] != null)
                {
                    height = Math.max(height, board.length - y);
                    break;
                }
            }
        }
        return height;
    }

    private int getHoles(Color[][] board)
    {
        int holes = 0;
        for (int x = 0; x < board[0].length; x++){
            boolean blockFound = false;
            for (Color[] colors : board) {
                if (colors[x] != null) {
                    blockFound = true;
                } else if (blockFound && colors[x] == null) {
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
// Calculate the bumpiness of the surface
        int bumpiness = 0;
        for (int x = 0; x < board[0].length - 1; x++) {
            int colHeight1 = getColumnHeight(board, x);
            int colHeight2 = getColumnHeight(board, x + 1);
            bumpiness += Math.abs(colHeight1 - colHeight2);
        }
        return bumpiness;
    }

    private int getColumnHeight(Color[][] board, int col) {
        for (int y = 0; y < board.length; y++) {
            if (board[y][col] != null) {
                return board.length - y;
            }
        }
        return 0;
    }


    private boolean canPlace(int[][] shape, int row, int col, Color[][] board)
    {
        for(int r = 0; r < shape.length; r++)
        {
            for(int c = 0; c < shape[r]. length; c++)
            {
                if (shape[r][c] == 1)
                {
                    int boardRow = row + r;
                    int boardCol = col + c;

                    if (boardRow < 0 || boardRow >= board.length) {
                        return false;
                    }
                    if (boardCol < 0 || boardCol >= board[0].length) {
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

    private int findDropRow(int[][] shape, int col, Color[][] board) {
        int row = 0;

        while (canPlace(shape, row + 1, col, board)) {
            row++;
        }

        return row;
    }


    private Color[][] copyBoard(Color[][] board) {
        Color[][] newBoard = new Color[board.length][board[0].length];

        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[r].length; c++) {
                newBoard[r][c] = board[r][c];
            }
        }

        return newBoard;
    }

    private int[][] rotateShape(int[][] shape) {
        int n = shape.length;
        int[][] rotated = new int[n][n];

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                rotated[c][n - 1 - r] = shape[r][c];
            }
        }

        return rotated;
    }

    private void placePiece(Color[][] board, int[][] shape, int row, int col, Color color) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    board[row + r][col + c] = color;
                }
            }
        }
    }

    public int[] findBestMove(Color[][] board, Tetromino piece) {
        int bestScore = Integer.MIN_VALUE;
        int bestCol = 0;
        int bestRotation = 0;

        int[][] shape = piece.getShape();

        for (int rotation = 0; rotation < 4; rotation++) {

            for (int col = 0; col < board[0].length; col++) {

                if (!canPlace(shape, 0, col, board)) {
                    continue;
                }

                int dropRow = findDropRow(shape, col, board);

                Color[][] simulatedBoard = copyBoard(board);
                placePiece(simulatedBoard, shape, dropRow, col, piece.getColor());

                int score = evaluateBoard(simulatedBoard);

                if (score > bestScore) {
                    bestScore = score;
                    bestCol = col;
                    bestRotation = rotation;
                }
            }

            shape = rotateShape(shape);
        }

        return new int[] { bestCol, bestRotation };
    }


}





