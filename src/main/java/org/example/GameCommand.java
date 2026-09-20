package org.example;

/*
 * Command Pattern:
 * Encapsulates a gameplay request as an object.
 */
@FunctionalInterface
public interface GameCommand {

    void execute();
}
