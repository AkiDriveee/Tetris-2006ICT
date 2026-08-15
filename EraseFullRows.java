
package org.oosd.EraseFullRows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class EraseFullRows extends JPanel {

    private static final int ROWS = 20;
    private static final int COLS = 10;
    private static final int BLOCK_SIZE = 30;

    private Color[][] board = new Color[ROWS][COLS];

    private JButton eraseButton;
    private JLabel resultLabel;

    public EraseFullRows() {

        setPreferredSize(new Dimension(
                COLS * BLOCK_SIZE,
                ROWS * BLOCK_SIZE
        ));

        setBackground(Color.DARK_GRAY);

        createTestBoard();
    }

    private void createTestBoard() {

        // Two complete rows at the bottom
        for (int col = 0; col < COLS; col++) {

            board[ROWS - 1][col] = Color.RED;
            board[ROWS - 2][col] = Color.BLUE;
        }

        // Some blocks above the full rows
        board[ROWS - 3][2] = Color.GREEN;
        board[ROWS - 3][3] = Color.GREEN;

        board[ROWS - 4][5] = Color.YELLOW;

        board[ROWS - 5][1] = Color.MAGENTA;
        board[ROWS - 5][7] = Color.ORANGE;
    }

    private boolean isFullRow(int row) {

        for (int col = 0; col < COLS; col++) {

            if (board[row][col] == null) {
                return false;
            }
        }

        return true;
    }

    private void eraseFullRows() {

        int rowsRemoved = 0;

        for (int row = ROWS - 1; row >= 0; row--) {

            if (isFullRow(row)) {

                removeRow(row);

                rowsRemoved++;

                // Check this row again because another row
                // has moved into the same position.
                row++;
            }
        }

        resultLabel.setText(
                "Rows removed: " + rowsRemoved
        );

        repaint();
    }

    private void removeRow(int row) {

        // Move everything above the deleted row down.
        for (int r = row; r > 0; r--) {

            for (int col = 0; col < COLS; col++) {

                board[r][col] = board[r - 1][col];
            }
        }

        // Clear the top row.
        for (int col = 0; col < COLS; col++) {

            board[0][col] = null;
        }
    }

    private void drawBlock(Graphics g, int row, int col, Color color) {

        int x = col * BLOCK_SIZE;
        int y = row * BLOCK_SIZE;

        g.setColor(color);

        g.fillRect(
                x,
                y,
                BLOCK_SIZE,
                BLOCK_SIZE
        );

        g.setColor(Color.BLACK);

        g.drawRect(
                x,
                y,
                BLOCK_SIZE - 1,
                BLOCK_SIZE - 1
        );
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        for (int row = 0; row < ROWS; row++) {

            for (int col = 0; col < COLS; col++) {

                if (board[row][col] != null) {

                    drawBlock(
                            g,
                            row,
                            col,
                            board[row][col]
                    );
                }
            }
        }

        // Draw the board lines
        g.setColor(Color.GRAY);

        for (int row = 0; row <= ROWS; row++) {

            g.drawLine(
                    0,
                    row * BLOCK_SIZE,
                    COLS * BLOCK_SIZE,
                    row * BLOCK_SIZE
            );
        }

        for (int col = 0; col <= COLS; col++) {

            g.drawLine(
                    col * BLOCK_SIZE,
                    0,
                    col * BLOCK_SIZE,
                    ROWS * BLOCK_SIZE
            );
        }
    }

    private void createWindow() {

        JFrame frame = new JFrame("Erase Full Rows");

        EraseFullRows game = this;

        eraseButton = new JButton("Erase Full Rows");

        resultLabel = new JLabel(
                "Press the button to erase the full rows"
        );

        eraseButton.addActionListener(
                (ActionEvent e) -> game.eraseFullRows()
        );

        JPanel bottomPanel = new JPanel();

        bottomPanel.add(eraseButton);
        bottomPanel.add(resultLabel);

        frame.setLayout(new BorderLayout());

        frame.add(
                game,
                BorderLayout.CENTER
        );

        frame.add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        frame.pack();

        frame.setLocationRelativeTo(null);

        frame.setResizable(false);

        frame.setVisible(true);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            EraseFullRows game = new EraseFullRows();

            game.createWindow();
        });
    }
}

