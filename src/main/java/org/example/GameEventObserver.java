package org.example;

/*
 * Observer contract for gameplay events published by PlayerGame.
 */
public interface GameEventObserver {

    void onLinesCleared(PlayerGame playerGame, int rowsRemoved);
}
