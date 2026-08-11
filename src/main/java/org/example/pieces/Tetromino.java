package org.example.pieces;

import javafx.scene.paint.Color;

public abstract class Tetromino{

    protected int[][]shape;
    protected Color color;
    protected int row;
    protected int col;

    protected double yOffset = 0;

    public Tetromino(int startRow, int startCol)
    {
        this.row = startRow;
        this.col = startCol;
    }

    public void moveDown(){
        row++;
    }

    public void moveLeft() {
        col--;
    }

    public void moveRight() {
        col++;
    }

    public int[][] getShape() {
        return shape;
    }

    public Color getColor() {
        return color;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public double getYOffset() {
        return yOffset;
    }

    public void addYOffset(double amount) {
        yOffset += amount;
    }

    public void resetYOffset() {
        yOffset = 0;
    }

    public void setYOffset(double value) {
        yOffset = value;
    }

    public int[][] getRotatedShape() {
        int n = shape.length;
        int[][] rotated = new int[n][n];

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                rotated[c][n - 1 - r] = shape[r][c];
            }
        }

        return rotated;
    }

    public void setShape(int[][] newShape) {
        shape = newShape;
    }
}