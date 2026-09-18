package org.example.server;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.pieces.Tetromino;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.Socket;

public class TetrisServerConnection {

    private static TetrisServerConnection instance;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3000;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private TetrisServerConnection()
    {

    }

    public static TetrisServerConnection getInstance()
    {
        if(instance == null)
        {
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

    public int[] getServerMove(Color[][] board, Tetromino currentPiece, Tetromino nextPiece) {

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

        // socket connection goes here next

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String jsonGameState = objectMapper.writeValueAsString(game);
            out.println(jsonGameState);

            String response = in.readLine();
            OpMove move = objectMapper.readValue(response, OpMove.class);

            return new int[] { move.opX(), move.opRotate() };

        } catch (IOException e) {
            System.out.println("Could not connect to TetrisServer: " + e.getMessage());
            return new int[] { currentPiece.getCol(), 0 };
        }
    }

}

