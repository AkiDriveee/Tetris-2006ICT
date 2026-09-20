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
     * Both connection and response reads need a timeout. Without a read
     * timeout, in.readLine() can block the JavaFX AnimationTimer indefinitely
     * if TetrisServer accepts the socket but stops responding.
     */
    private static final int CONNECTION_TIMEOUT_MS = 500;
    private static final int READ_TIMEOUT_MS = 500;

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

        try (Socket socket = new Socket()) {

            socket.connect(
                    new InetSocketAddress(SERVER_HOST, SERVER_PORT),
                    CONNECTION_TIMEOUT_MS
            );

            /*
             * Critical: connect(timeout) protects only the connection phase.
             * setSoTimeout protects the later blocking read from hanging the
             * JavaFX game loop forever.
             */
            socket.setSoTimeout(READ_TIMEOUT_MS);

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
             * Harmless fallback. PlayScreen checks
             * wasLastRequestSuccessful() and lets the piece continue with
             * ordinary falling instead of treating this as a server target.
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
