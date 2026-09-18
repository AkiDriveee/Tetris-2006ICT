package org.example.pieces;

import javafx.scene.paint.Color;

public class ZPiece extends Tetromino {

    public ZPiece(int startRow, int startCol) {
        super(startRow, startCol);

        this.shape = new int[][]{
                {1, 1, 0, 0},
                {0, 1, 1, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };

        this.color = Color.RED;
    }
}