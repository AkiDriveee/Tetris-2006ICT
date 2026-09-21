package org.example;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerGameObserverTest {

    /*
     * Spy Test Double:
     * Records whether PlayerGame notified an observer,
     * how many lines were cleared, and which PlayerGame
     * published the event.
     */
    private static class SpyGameEventObserver
            implements GameEventObserver {

        private boolean notified;
        private int rowsRemoved;
        private PlayerGame source;

        @Override
        public void onLinesCleared(
                PlayerGame playerGame,
                int rowsRemoved
        ) {
            this.notified = true;
            this.rowsRemoved = rowsRemoved;
            this.source = playerGame;
        }
    }

    @Test
    void observerShouldBeNotifiedWhenFullRowIsCleared() {

        PlayerGame playerGame =
                new PlayerGame(
                        PlayerType.HUMAN,
                        10,
                        20
                );

        SpyGameEventObserver spy =
                new SpyGameEventObserver();

        playerGame.addObserver(spy);

        /*
         * Fill the bottom row directly through the board
         * returned by PlayerGame.
         */
        Color[][] board = playerGame.getBoard();

        for (int col = 0; col < playerGame.getCols(); col++) {
            board[playerGame.getRows() - 1][col] = Color.BLUE;
        }

        int rowsRemoved =
                playerGame.eraseFullRows();

        assertEquals(1, rowsRemoved);

        // Verify information recorded by the Spy.
        assertTrue(spy.notified);
        assertEquals(1, spy.rowsRemoved);
        assertSame(playerGame, spy.source);
    }

    @Test
    void observerShouldNotBeNotifiedWhenNoRowIsCleared() {

        PlayerGame playerGame =
                new PlayerGame(
                        PlayerType.HUMAN,
                        10,
                        20
                );

        SpyGameEventObserver spy =
                new SpyGameEventObserver();

        playerGame.addObserver(spy);

        int rowsRemoved =
                playerGame.eraseFullRows();

        assertEquals(0, rowsRemoved);
        assertEquals(false, spy.notified);
        assertEquals(0, spy.rowsRemoved);
    }
}