package org.example;

/*
 * Concrete Observer that records completed-line events.
 *
 * The logging operation is performed on a background thread so that
 * non-critical event logging does not run on the main JavaFX game thread.
 */
public class GameEventLogger implements GameEventObserver {

    private final String playerName;

    public GameEventLogger(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void onLinesCleared(PlayerGame playerGame, int rowsRemoved) {

        Thread loggingThread = new Thread(() -> {

            System.out.println(
                    "[Game Event] " + playerName +
                            " cleared " + rowsRemoved +
                            (rowsRemoved == 1 ? " line." : " lines.") +
                            " [Thread: " + Thread.currentThread().getName() + "]"
            );

        });

        loggingThread.setName(
                "GameEventLogger-" +
                        playerName.replace(" ", "")
        );

        /*
         * A daemon thread will not prevent the Tetris application
         * from closing if logging is still finishing.
         */
        loggingThread.setDaemon(true);

        loggingThread.start();
    }
}