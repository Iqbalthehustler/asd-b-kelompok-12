import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class BoardPanel extends JPanel {
    private static final int BOARD_SIZE = 10;
    private static final int SQUARE_SIZE = 60;
    private Map<Integer, Point> squarePositions;
    private Map<Integer, List<Integer>> graph;
    private Player[] players;
    private String statusMessage;
    private AnimationPanel animationPanel;

    public BoardPanel() {
        this.squarePositions = new HashMap<>();
        this.graph = new HashMap<>();
        this.statusMessage = "Game dimulai!";
        this.animationPanel = new AnimationPanel();

        setLayout(new OverlayLayout(this));
        add(animationPanel);

        initializeSquarePositions();
        initializeGraph();
        setPreferredSize(new Dimension(BOARD_SIZE * SQUARE_SIZE + 50, BOARD_SIZE * SQUARE_SIZE + 80));
        setBackground(new Color(240, 240, 240));
        setBorder(BorderFactory.createTitledBorder("Papan Ular Tangga 10x10"));
    }

    private void initializeSquarePositions() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int position = calculatePosition(row, col);
                int x = col * SQUARE_SIZE + 25;
                int y = row * SQUARE_SIZE + 25;
                squarePositions.put(position, new Point(x, y));
            }
        }
    }

    private int calculatePosition(int row, int col) {
        if (row % 2 == 0) {
            return (BOARD_SIZE - row) * BOARD_SIZE - col;
        } else {
            return (BOARD_SIZE - row - 1) * BOARD_SIZE + col + 1;
        }
    }

    private void initializeGraph() {
        for (int i = 1; i <= 100; i++) {
            graph.put(i, new ArrayList<>());
        }

        for (int i = 1; i <= 100; i++) {
            for (int j = 1; j <= 6; j++) {
                int nextPos = i + j;
                if (nextPos <= 100) {
                    graph.get(i).add(nextPos);
                }
            }
        }

        for (int i = 1; i <= 100; i++) {
            for (int j = 1; j <= 6; j++) {
                int prevPos = i - j;
                if (prevPos >= 1) {
                    graph.get(i).add(prevPos);
                }
            }
        }
    }

    public void addLadder(int from, int to) {
        if (from < to && from >= 1 && to <= 100) {
            graph.get(from).add(to);
        }
    }

    public void addSnake(int from, int to) {
        if (from > to && from >= 1 && to <= 100) {
            graph.get(from).add(to);
        }
    }

    public List<Integer> getPossibleMoves(int position) {
        return graph.getOrDefault(position, new ArrayList<>());
    }

    public boolean isValidMove(int from, int to) {
        return graph.get(from).contains(to);
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public void setStatusMessage(String message) {
        this.statusMessage = message;
        repaint();
    }

    public void animatePlayerMovement(Player player, int startPos, int endPos, Runnable onComplete) {
        if (animationPanel.isAnimating()) {
            onComplete.run();
            return;
        }
        animationPanel.animateMovement(player, startPos, endPos, onComplete);
    }

    public boolean isAnimating() {
        return animationPanel.isAnimating();
    }

    // HANYA SATU METHOD INI - hapus yang duplikat
    public AnimationPanel getAnimationPanel() {
        return animationPanel;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBoard(g2d);
        drawPlayers(g2d);
        drawStatus(g2d);
    }

    private void drawBoard(Graphics2D g2d) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int position = calculatePosition(row, col);
                Point point = squarePositions.get(position);

                Color squareColor;
                if (position == 1) {
                    squareColor = new Color(144, 238, 144);
                } else if (position == 100) {
                    squareColor = new Color(255, 182, 193);
                } else {
                    squareColor = (row + col) % 2 == 0 ?
                            new Color(200, 230, 255) : new Color(150, 200, 255);
                }

                g2d.setColor(squareColor);
                g2d.fillRect(point.x, point.y, SQUARE_SIZE, SQUARE_SIZE);

                g2d.setColor(Color.BLACK);
                g2d.drawRect(point.x, point.y, SQUARE_SIZE, SQUARE_SIZE);

                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                String number;
                if (position == 1) {
                    number = "START";
                } else if (position == 100) {
                    number = "FINISH";
                } else {
                    number = String.valueOf(position);
                }

                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(number);
                int textX = point.x + (SQUARE_SIZE - textWidth) / 2;
                int textY = point.y + 20;

                g2d.drawString(number, textX, textY);
            }
        }
    }

    private void drawPlayers(Graphics2D g2d) {
        if (players == null) return;

        for (Player player : players) {
            if (player != null && player.getPosition() > 0) {
                Point position = squarePositions.get(player.getPosition());
                if (position != null) {
                    drawPlayerToken(g2d, player, position);
                }
            }
        }
    }

    private void drawPlayerToken(Graphics2D g2d, Player player, Point position) {
        int playerNum = player.getPlayerNumber();
        int offsetX = (playerNum % 2) * 20 + 5;
        int offsetY = (playerNum / 2) * 20 + 25;

        g2d.setColor(player.getColor().darker());
        g2d.fillOval(position.x + offsetX + 2, position.y + offsetY + 2, 20, 20);

        g2d.setColor(player.getColor());
        g2d.fillOval(position.x + offsetX, position.y + offsetY, 20, 20);

        g2d.setColor(Color.BLACK);
        g2d.drawOval(position.x + offsetX, position.y + offsetY, 20, 20);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString(String.valueOf(playerNum + 1),
                position.x + offsetX + 7, position.y + offsetY + 13);
    }

    private void drawStatus(Graphics2D g2d) {
        int statusY = BOARD_SIZE * SQUARE_SIZE + 50;

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(statusMessage, 20, statusY);

        if (animationPanel.isAnimating()) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("• Animasi berjalan", 250, statusY);
        }
    }

    public Point getSquarePosition(int position) {
        return squarePositions.get(position);
    }

    public void printBoardLayout() {
        System.out.println("=== BOARD LAYOUT ===");
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int pos = calculatePosition(row, col);
                System.out.print(String.format("%3d ", pos));
            }
            System.out.println();
        }
        System.out.println("START: 1 (bawah kiri)");
        System.out.println("FINISH: 100 (atas kanan)");
    }

    public void printGraph() {
        System.out.println("=== GRAPH REPRESENTATION ===");
        for (int i = 1; i <= 100; i++) {
            List<Integer> moves = graph.get(i);
            System.out.println("Position " + i + " -> " + moves);
        }
    }
}