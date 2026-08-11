package org.example.pieces;
import javafx.scene.paint.Color;

public class OPiece extends Tetromino{
    public OPiece(int startRow, int startCol)
    {
        super(startRow, startCol);

        this.shape = new int[][]{
                {1, 1},
                {1, 1}
        };

        this.color = Color.YELLOW;
    }
}

