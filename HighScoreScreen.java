import javax.swing.*;
import java.awt.*;

public class HighScoreScreen extends JFrame {

    public HighScoreScreen() {

        
        setTitle("Tetris - High Scores");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("HIGH SCORES", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(Color.white);
        panel.add(title, BorderLayout.NORTH);

        String[] scores = {
                "Aksa              9800",
                "Sukhdeep          9200",
                "Taj               8700",
                "Havana            8300",
                "Emma              7900",
                "Liam              7500",
                "John              7100",
                "Mia               6800",
                "Olivia            6400",
                "James             6000"
        };

        JList<String> scoreList = new JList<>(scores);
        scoreList.setFont(new Font("Monospaced", Font.BOLD, 26));
        scoreList.setBackground(Color.BLACK);
        scoreList.setForeground(Color.WHITE);
        scoreList.setSelectionBackground(Color.BLACK);
        scoreList.setSelectionForeground(Color.white);
        scoreList.setFixedCellHeight(45);

        DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        scoreList.setCellRenderer(renderer);

        JScrollPane scrollPane = new JScrollPane(scoreList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.BLACK);

        panel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("BACK");
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.setPreferredSize(new Dimension(180, 50));
        backButton.setBackground(Color.white);
        backButton.setForeground(Color.BLACK);
        

        backButton.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.add(backButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HighScoreScreen());
    }
}