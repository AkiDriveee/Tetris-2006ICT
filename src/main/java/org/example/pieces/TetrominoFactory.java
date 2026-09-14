package org.example.pieces;

import java.util.Random;

public class TetrominoFactory {

    private static Random random = new Random();

    public Tetromino createRandomPiece()
    {

        int type =
                random.nextInt(7);

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
}
