package org.example.pieces;

import java.util.Random;

public class TetrominoFactory {

    private static final Random random =
            new Random();

    /*
     * Create a random tetromino using the original
     * default Tetris board width.
     *
     * Keeping this method preserves compatibility with
     * any existing code that still calls createRandomPiece()
     * without supplying a board width.
     */
    public Tetromino createRandomPiece() {

        return createRandomPiece(10);
    }

    /*
     * Create a random tetromino and calculate its starting
     * column from the configured board width.
     *
     * This allows pieces to spawn safely on dynamic field
     * sizes, including smaller boards such as width 5.
     */
    public Tetromino createRandomPiece(
            int boardWidth
    ) {

        int type =
                random.nextInt(7);

        /*
         * Tetromino shape matrices can occupy up to four
         * columns. Positioning from this column keeps the
         * initial piece near the centre while preventing
         * the spawn position from assuming a 10-column board.
         */
        int startCol =
                Math.max(
                        0,
                        (boardWidth - 4) / 2
                );

        return switch (type) {

            case 0 ->
                    new IPiece(0, startCol);

            case 1 ->
                    new OPiece(0, startCol);

            case 2 ->
                    new TPiece(0, startCol);

            case 3 ->
                    new SPiece(0, startCol);

            case 4 ->
                    new ZPiece(0, startCol);

            case 5 ->
                    new JPiece(0, startCol);

            default ->
                    new LPiece(0, startCol);
        };
    }
}