package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreLogicTest {

    @Test
    void initialValuesShouldBeCorrect() {
        ScoreLogic scoreLogic = new ScoreLogic(1);

        assertEquals(0, scoreLogic.getScore());
        assertEquals(1, scoreLogic.getLevel());
        assertEquals(0, scoreLogic.getLinesErased());
    }

    @Test
    void clearingOneLineShouldAdd100Points() {
        ScoreLogic scoreLogic = new ScoreLogic(1);

        scoreLogic.addLinesCleared(1);

        assertEquals(100, scoreLogic.getScore());
        assertEquals(1, scoreLogic.getLinesErased());
    }

    @Test
    void clearingTwoLinesShouldAdd300Points() {
        ScoreLogic scoreLogic = new ScoreLogic(1);

        scoreLogic.addLinesCleared(2);

        assertEquals(300, scoreLogic.getScore());
        assertEquals(2, scoreLogic.getLinesErased());
    }

    @Test
    void clearingThreeLinesShouldAdd600Points() {
        ScoreLogic scoreLogic = new ScoreLogic(1);

        scoreLogic.addLinesCleared(3);

        assertEquals(600, scoreLogic.getScore());
        assertEquals(3, scoreLogic.getLinesErased());
    }

    @Test
    void clearingFourLinesShouldAdd1000Points() {
        ScoreLogic scoreLogic = new ScoreLogic(1);

        scoreLogic.addLinesCleared(4);

        assertEquals(1000, scoreLogic.getScore());
        assertEquals(4, scoreLogic.getLinesErased());
    }

    @Test
    void levelShouldIncreaseAfterTenLines() {
        ScoreLogic scoreLogic = new ScoreLogic(1);

        scoreLogic.addLinesCleared(4);
        scoreLogic.addLinesCleared(4);
        scoreLogic.addLinesCleared(2);

        assertEquals(10, scoreLogic.getLinesErased());
        assertEquals(2, scoreLogic.getLevel());
    }

    @Test
    void scoreShouldAccumulateAcrossMultipleLineClears() {
        ScoreLogic scoreLogic = new ScoreLogic(1);

        scoreLogic.addLinesCleared(1);
        scoreLogic.addLinesCleared(2);
        scoreLogic.addLinesCleared(4);

        assertEquals(1400, scoreLogic.getScore());
        assertEquals(7, scoreLogic.getLinesErased());
    }

    /*
     * Parameterized Test:
     * The same scoring behaviour is tested with several different
     * line-clear inputs and expected scores.
     */
    @ParameterizedTest
    @CsvSource({
            "1, 100",
            "2, 300",
            "3, 600",
            "4, 1000"
    })
    void lineClearShouldProduceExpectedScore(
            int linesCleared,
            int expectedScore
    ) {
        ScoreLogic scoreLogic = new ScoreLogic(1);

        scoreLogic.addLinesCleared(linesCleared);

        assertEquals(expectedScore, scoreLogic.getScore());
        assertEquals(linesCleared, scoreLogic.getLinesErased());
    }
}