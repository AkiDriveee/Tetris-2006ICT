package org.example;

/*
 * Identifies the seven standard tetromino types.
 *
 * Extended Mode can generate one TetrominoType and use it
 * to create separate piece instances for both players.
 * This keeps both players on the same piece sequence while
 * allowing each piece to move and rotate independently.
 */
public enum TetrominoType {
    I,
    O,
    T,
    S,
    Z,
    J,
    L
}