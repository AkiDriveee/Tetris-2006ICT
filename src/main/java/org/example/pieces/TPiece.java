package org.example.pieces;

import javafx.scene.paint.Color;

public class TPiece extends Tetromino {

    public TPiece(int startRow, int startCol) {
        super(startRow, startCol);

        this.shape = new int[][]{
                {0, 0, 0, 0},
                {1, 1, 1, 0},
                {0, 1, 0, 0},
                {0, 0, 0, 0}
        };

        this.color = Color.PURPLE;
    }
}