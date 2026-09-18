package org.example;

/*
 * Defines the available controller types for each player.
 * Using an enum keeps player selection type-safe and avoids
 * multiple boolean flags for Human, AI, and External modes.
 */
public enum PlayerType {

    HUMAN,
    AI,
    EXTERNAL
}