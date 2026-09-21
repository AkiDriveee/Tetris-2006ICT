package org.example;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PlayerGameMockitoTest {

    @Test
    void mockObserverShouldReceiveLineClearNotification() {

        PlayerGame playerGame =
                new PlayerGame(
                        PlayerType.HUMAN,
                        10,
                        20
                );

        /*
         * Mockito creates a mock implementation of
         * the GameEventObserver interface.
         */
        GameEventObserver mockObserver =
                mock(GameEventObserver.class);

        playerGame.addObserver(mockObserver);

        // Fill the bottom row so PlayerGame clears one line.
        Color[][] board = playerGame.getBoard();

        for (int col = 0; col < playerGame.getCols(); col++) {
            board[playerGame.getRows() - 1][col] = Color.BLUE;
        }

        playerGame.eraseFullRows();

        /*
         * Verify that PlayerGame sent the expected event
         * to the Mockito mock exactly once.
         */
        verify(mockObserver)
                .onLinesCleared(playerGame, 1);
    }

    @Test
    void mockObserverShouldNotReceiveNotificationWithoutLineClear() {

        PlayerGame playerGame =
                new PlayerGame(
                        PlayerType.HUMAN,
                        10,
                        20
                );

        GameEventObserver mockObserver =
                mock(GameEventObserver.class);

        playerGame.addObserver(mockObserver);

        // Board is empty, so no completed line exists.
        playerGame.eraseFullRows();

        /*
         * Verify that no line-clear event was sent
         * to the Mockito mock.
         */
        verify(mockObserver, never())
                .onLinesCleared(playerGame, 1);
    }
}