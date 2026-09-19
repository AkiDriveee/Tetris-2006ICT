package org.example;

public class ScoreLogic {

    private int score = 0;
    private int level;
    private int linesErased = 0;
    private int initialLevel;

    public ScoreLogic(int initialLevel) {
        this.initialLevel = initialLevel;
        this.level = initialLevel;
    }

    public void addLinesCleared(int rowsRemoved) {
        switch (rowsRemoved) {
            case 1 -> score += 100;
            case 2 -> score += 300;
            case 3 -> score += 600;
            case 4 -> score += 1000;
        }

        linesErased += rowsRemoved;
        level = initialLevel + (linesErased / 10);
    }

    public int getScore() { return score; }
    public int getLevel() { return level; }
    public int getLinesErased() { return linesErased; }
}