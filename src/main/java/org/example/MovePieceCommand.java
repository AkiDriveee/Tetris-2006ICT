package org.example;

import org.example.pieces.Tetromino;

/*
 * Concrete Command for a legal tetromino movement/rotation.
 *
 * The command receives the action to perform and the redraw operation.
 * This keeps keyboard input responsible only for choosing a command,
 * while PlayerGame remains responsible for validating whether the
 * requested movement is legal.
 */
public class MovePieceCommand implements GameCommand {

    private final Runnable movementAction;
    private final Runnable redrawAction;
    private final boolean playMoveSound;

    public MovePieceCommand(
            Runnable movementAction,
            Runnable redrawAction,
            boolean playMoveSound
    ) {
        this.movementAction = movementAction;
        this.redrawAction = redrawAction;
        this.playMoveSound = playMoveSound;
    }

    @Override
    public void execute() {
        movementAction.run();

        if (playMoveSound) {
            AudioManager.playMoveTurnSound();
        }

        redrawAction.run();
    }
}
