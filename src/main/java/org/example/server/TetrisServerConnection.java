package org.example.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.paint.Color;
import org.example.pieces.Tetromino;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TetrisServerConnection {

    private static TetrisServerConnection instance;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3000;

    /*
     * Prevent a missing server from holding up the JavaFX game thread
     * for a long time while a connection attempt is made.
     */
    private static final int CONNECTION_TIMEOUT_MS = 500;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /*
     * Records whether the most recent request successfully reached
     * TetrisServer and received a valid move.
     */
    private boolean lastRequestSuccessful = false;

    private TetrisServerConnection() {
    }

    public static TetrisServerConnection getInstance() {

        if (instance == null) {
            instance = new TetrisServerConnection();
        }

        return instance;
    }

    private int[][] convertBoardToInts(Color[][] board) {

        int[][] result = new int[board.length][board[0].length];

        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[r].length; c++) {
                result[r][c] = (board[r][c] != null) ? 1 : 0;
            }
        }

        return result;
    }

    public int[] getServerMove(
            Color[][] board,
            Tetromino currentPiece,
            Tetromino nextPiece
    ) {

        /*
         * Assume failure until a complete request/response cycle succeeds.
         * PlayScreen can inspect this state and decide whether to use the
         * returned move or fall back to ordinary slow falling.
         */
        lastRequestSuccessful = false;

        int[][] cells = convertBoardToInts(board);
        int[][] currentShape = currentPiece.getShape();
        int[][] nextShape = nextPiece.getShape();

        PureGame game = new PureGame(
                board[0].length,
                board.length,
                cells,
                currentShape,
                nextShape
        );

        /*
         * TetrisServer accepts one JSON game-state request on localhost:3000
         * and returns one OpMove containing the target X and rotation.
         */
        try (Socket socket = new Socket()) {

            socket.connect(
                    new InetSocketAddress(SERVER_HOST, SERVER_PORT),
                    CONNECTION_TIMEOUT_MS
            );

            try (PrintWriter out =
                         new PrintWriter(
                                 new OutputStreamWriter(socket.getOutputStream()),
                                 true
                         );
                 BufferedReader in =
                         new BufferedReader(
                                 new InputStreamReader(socket.getInputStream())
                         )) {

                String jsonGameState =
                        objectMapper.writeValueAsString(game);

                out.println(jsonGameState);

                String response = in.readLine();

                if (response == null || response.isBlank()) {
                    throw new IOException(
                            "TetrisServer returned an empty response."
                    );
                }

                OpMove move =
                        objectMapper.readValue(
                                response,
                                OpMove.class
                        );

                lastRequestSuccessful = true;

                return new int[]{
                        move.opX(),
                        move.opRotate()
                };
            }

        } catch (IOException e) {

            System.out.println(
                    "Could not connect to TetrisServer: " +
                            e.getMessage()
            );

            /*
             * This fallback is deliberately harmless. PlayScreen checks
             * wasLastRequestSuccessful() before treating it as a genuine
             * External-player target.
             */
            return new int[]{
                    currentPiece.getCol(),
                    0
            };
        }
    }

    public boolean wasLastRequestSuccessful() {
        return lastRequestSuccessful;
    }
}
