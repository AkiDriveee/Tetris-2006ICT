package org.example.server;

public class PureGame {

    private int width;
    private int height;
    private int[][] cells;
    private int[][] currentShape;
    private int[][] nextShape;

    public PureGame(int width, int height, int[][] cells, int[][] currentShape, int[][] nextShape) {
        this.width = width;
        this.height = height;
        this.cells = cells;
        this.currentShape = currentShape;
        this.nextShape = nextShape;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int[][] getCells() { return cells; }
    public int[][] getCurrentShape() { return currentShape; }
    public int[][] getNextShape() { return nextShape; }
}