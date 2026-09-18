package org.example;

import org.example.pieces.*;

import java.util.Random;

public class TetrominoFactory {

    private static final Random random =
            new Random();

    /*
     * Create a random tetromino using the original
     * default Tetris board width.
     *
     * Keeping this method preserves compatibility with
     * existing single-player code.
     */
    public Tetromino createRandomPiece() {

        return createRandomPiece(10);
    }

    /*
     * Create a random tetromino for the supplied board width.
     *
     * The random type is generated separately so Extended Mode
     * can later reuse the same type for both players.
     */
    public Tetromino createRandomPiece(
            int boardWidth
    ) {

        TetrominoType type =
                createRandomType();

        return createPiece(
                type,
                boardWidth
        );
    }

    /*
     * Generates one of the seven standard tetromino types.
     *
     * Extended Mode can call this once and then create an
     * independent piece of that same type for each player.
     */
    public TetrominoType createRandomType() {

        TetrominoType[] types =
                TetrominoType.values();

        return types[
                random.nextInt(types.length)
                ];
    }

    /*
     * Creates a new tetromino instance of a specific type.
     *
     * Each call returns a separate object. Therefore two players
     * can receive the same tetromino type and sequence without
     * sharing movement, rotation, row, column, or offset state.
     */
    public Tetromino createPiece(
            TetrominoType type,
            int boardWidth
    ) {

        /*
         * Tetromino shape matrices can occupy up to four
         * columns. This keeps the initial piece near the centre
         * and also supports dynamically configured board widths.
         */
        int startCol =
                Math.max(
                        0,
                        (boardWidth - 4) / 2
                );

        return switch (type) {

            case I ->
                    new IPiece(0, startCol);

            case O ->
                    new OPiece(0, startCol);

            case T ->
                    new TPiece(0, startCol);

            case S ->
                    new SPiece(0, startCol);

            case Z ->
                    new ZPiece(0, startCol);

            case J ->
                    new JPiece(0, startCol);

            case L ->
                    new LPiece(0, startCol);
        };
    }
}