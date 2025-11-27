import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class BoardPanel extends JPanel {
    private static final int BOARD_SIZE = 10;
    private static final int SQUARE_SIZE = 60;
    private Map<Integer, Point> squarePositions;
    private Map<Integer, List<Integer>> graph;
    private Player[] players;
    private String statusMessage;
    private AnimationPanel animationPanel;
    private Set<Integer> specialNodes; // Node khusus (5 titik hijau)
    private Map<Integer, List<Integer>> specialGraph; // Graph khusus untuk node hijau
    private ControlPanel controlPanel; // Reference ke control panel
    private Set<Integer> primeNumbers; // Set untuk angka prima

    public BoardPanel() {
        this.squarePositions = new HashMap<>();
        this.graph = new HashMap<>();
        this.specialNodes = new HashSet<>();
        this.specialGraph = new HashMap<>();
        this.primeNumbers = new HashSet<>();
        this.statusMessage = "Game dimulai!";
        this.animationPanel = new AnimationPanel();

        setLayout(new OverlayLayout(this));
        add(animationPanel);

        initializeSquarePositions();
        initializeGraph();
        initializePrimeNumbers(); // Initialize angka prima
        generateFullyConnectedSpecialNodes();
        setPreferredSize(new Dimension(BOARD_SIZE * SQUARE_SIZE + 50, BOARD_SIZE * SQUARE_SIZE + 80));
        setBackground(new Color(240, 240, 240));
        setBorder(BorderFactory.createTitledBorder("Papan Ular Tangga 10x10"));
    }

    // Method untuk initialize angka prima
    private void initializePrimeNumbers() {
        // Angka prima antara 1-100
        int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47,
                53, 59, 61, 67, 71, 73, 79, 83, 89, 97};
        for (int prime : primes) {
            primeNumbers.add(prime);
        }
    }

    // Method untuk cek apakah angka prima
    public boolean isPrimeNumber(int number) {
        return primeNumbers.contains(number);
    }

    // Method untuk set control panel reference
    public void setControlPanel(ControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    // Method untuk initialize posisi kotak
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

    // Method untuk initialize graph
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

    // Method untuk generate 5 node khusus yang FULLY CONNECTED
    private void generateFullyConnectedSpecialNodes() {
        Random random = new Random();
        specialNodes.clear();
        specialGraph.clear();

        // Generate 5 posisi unik antara 10-90 (hindari area start/finish yang terlalu dekat)
        while (specialNodes.size() < 5) {
            int node = random.nextInt(81) + 10; // 10-90
            // Pastikan node tidak terlalu berdekatan (minimal jarak 5)
            boolean tooClose = false;
            for (int existingNode : specialNodes) {
                if (Math.abs(existingNode - node) < 8) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) {
                specialNodes.add(node);
            }
        }

        // Konversi ke list untuk memudahkan
        List<Integer> nodeList = new ArrayList<>(specialNodes);
        Collections.sort(nodeList); // Urutkan untuk konsistensi

        // Buat graph FULLY CONNECTED: setiap node terhubung ke SEMUA node lain
        for (int i = 0; i < nodeList.size(); i++) {
            int currentNode = nodeList.get(i);
            specialGraph.put(currentNode, new ArrayList<>());

            // Hubungkan ke SEMUA node lain
            for (int j = 0; j < nodeList.size(); j++) {
                if (i != j) { // Jangan hubungkan ke diri sendiri
                    int otherNode = nodeList.get(j);
                    specialGraph.get(currentNode).add(otherNode);
                }
            }
        }

        // Print debug info
        System.out.println("=== FULLY CONNECTED SPECIAL NODES ===");
        for (int node : nodeList) {
            System.out.println("Node " + node + " -> " + specialGraph.get(node));
        }
        System.out.println("Total koneksi: " + countTotalConnections());
    }

    // Method untuk menghitung total koneksi
    private int countTotalConnections() {
        int total = 0;
        for (List<Integer> connections : specialGraph.values()) {
            total += connections.size();
        }
        return total / 2; // Karena setiap koneksi dihitung dua kali
    }

    // Method untuk mendapatkan node khusus terdekat
    public Integer getNearestSpecialNode(int position) {
        int minDistance = Integer.MAX_VALUE;
        Integer nearestNode = null;

        for (int node : specialNodes) {
            int distance = Math.abs(node - position);
            if (distance < minDistance) {
                minDistance = distance;
                nearestNode = node;
            }
        }
        return nearestNode;
    }

    // Method untuk mendapatkan node khusus terjauh
    public Integer getFarthestSpecialNode(int position) {
        int maxDistance = -1;
        Integer farthestNode = null;

        for (int node : specialNodes) {
            int distance = Math.abs(node - position);
            if (distance > maxDistance) {
                maxDistance = distance;
                farthestNode = node;
            }
        }
        return farthestNode;
    }

    // Method untuk mendapatkan node khusus random (bukan posisi saat ini)
    public Integer getRandomSpecialNode(int currentPosition) {
        List<Integer> availableNodes = new ArrayList<>(specialNodes);
        availableNodes.remove(Integer.valueOf(currentPosition)); // Hapus posisi saat ini

        if (availableNodes.isEmpty()) {
            return null;
        }

        Random random = new Random();
        return availableNodes.get(random.nextInt(availableNodes.size()));
    }

    // Method untuk teleport ke node khusus
    public void teleportToSpecialNode(Player player, int targetNode) {
        if (specialNodes.contains(targetNode)) {
            int oldPosition = player.getPosition();
            player.setPosition(targetNode);

            if (controlPanel != null) {
                controlPanel.addGameLog("🚀 " + player.getName() + " TELEPORT ke node khusus! (" + oldPosition + " → " + targetNode + ")");
            }

            // Animasikan teleport
            animatePlayerMovement(player, oldPosition, targetNode, () -> {
                checkSpecialNodeEffects(player, targetNode);
            });
        }
    }

    // Method untuk efek khusus ketika mendarat di node khusus
    private void checkSpecialNodeEffects(Player player, int nodePosition) {
        List<Integer> connectedNodes = specialGraph.get(nodePosition);

        if (connectedNodes != null && !connectedNodes.isEmpty() && controlPanel != null) {
            String message = "📍 " + player.getName() + " di Node Khusus " + nodePosition +
                    " - Terhubung ke SEMUA node: " + connectedNodes;
            controlPanel.addGameLog(message);

            // Berikan bonus atau efek khusus
            giveSpecialNodeBonus(player, nodePosition);
        }
    }

    // Method untuk memberikan bonus di node khusus
    private void giveSpecialNodeBonus(Player player, int nodePosition) {
        Random random = new Random();
        int bonusType = random.nextInt(5); // 5 efek berbeda

        if (controlPanel == null) return;

        switch (bonusType) {
            case 0:
                // Teleport ke node khusus random lainnya
                Integer randomNode = getRandomSpecialNode(nodePosition);
                if (randomNode != null) {
                    controlPanel.addGameLog("🎯 " + player.getName() + " memilih TELEPORT ke node " + randomNode);
                    teleportToSpecialNode(player, randomNode);
                }
                break;

            case 1:
                // Bonus extra turn
                controlPanel.addGameLog("🔄 BONUS: " + player.getName() + " dapat EXTRA TURN!");
                break;

            case 2:
                // Bonus protection (tidak bisa dimundurkan 1 turn)
                controlPanel.addGameLog("🛡️ BONUS: " + player.getName() + " dapat PROTECTION 1 turn!");
                break;

            case 3:
                // Bonus roll lagi
                controlPanel.addGameLog("🎲 BONUS: " + player.getName() + " dapat ROLL LAGI!");
                break;

            case 4:
                // Bonus lompat ke node terjauh
                Integer farthestNode = getFarthestSpecialNode(nodePosition);
                if (farthestNode != null) {
                    controlPanel.addGameLog("⚡ BONUS: " + player.getName() + " LOMPAT ke node terjauh " + farthestNode);
                    teleportToSpecialNode(player, farthestNode);
                }
                break;
        }
    }

    // Method untuk reset special nodes (dipanggil saat game baru)
    public void resetSpecialNodes() {
        generateFullyConnectedSpecialNodes();
    }

    // Method untuk animate player movement
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

    public AnimationPanel getAnimationPanel() {
        return animationPanel;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBoard(g2d);
        drawSpecialNodes(g2d); // Gambar node khusus
        drawSpecialConnections(g2d); // Gambar koneksi khusus
        drawPlayers(g2d);
        drawStatus(g2d);
    }

    // Method untuk menggambar papan
    private void drawBoard(Graphics2D g2d) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int position = calculatePosition(row, col);
                Point point = squarePositions.get(position);

                Color squareColor;
                if (position == 1) {
                    squareColor = new Color(144, 238, 144); // Hijau muda untuk START
                } else if (position == 100) {
                    squareColor = new Color(255, 182, 193); // Pink untuk FINISH
                } else if (isPrimeNumber(position)) {
                    squareColor = new Color(255, 200, 200); // Merah muda untuk angka prima
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

                // Tanda khusus untuk angka prima
                if (isPrimeNumber(position) && position != 1) {
                    g2d.setColor(Color.RED);
                    g2d.setFont(new Font("Arial", Font.BOLD, 16));
                    g2d.drawString("P", point.x + 5, point.y + 40);
                }
            }
        }
    }

    // Method untuk menggambar node khusus (kotak hijau)
    private void drawSpecialNodes(Graphics2D g2d) {
        for (int node : specialNodes) {
            Point point = squarePositions.get(node);
            if (point != null) {
                // Gambar kotak hijau dengan border
                g2d.setColor(new Color(0, 200, 0, 180)); // Hijau lebih terang
                g2d.fillRect(point.x + 3, point.y + 3, SQUARE_SIZE - 6, SQUARE_SIZE - 6);

                g2d.setColor(new Color(0, 100, 0)); // Hijau tua untuk border
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(point.x + 3, point.y + 3, SQUARE_SIZE - 6, SQUARE_SIZE - 6);

                // Reset stroke
                g2d.setStroke(new BasicStroke(1));

                // Tanda khusus (bintang)
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                String star = "★";
                FontMetrics fm = g2d.getFontMetrics();
                int starWidth = fm.stringWidth(star);
                g2d.drawString(star, point.x + (SQUARE_SIZE - starWidth)/2, point.y + SQUARE_SIZE/2 + 6);

                // Tulis nomor node kecil di bawah bintang
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                String nodeText = String.valueOf(node);
                int textWidth = g2d.getFontMetrics().stringWidth(nodeText);
                g2d.drawString(nodeText, point.x + (SQUARE_SIZE - textWidth)/2, point.y + SQUARE_SIZE - 8);
            }
        }
    }

    // Method untuk menggambar koneksi antara node khusus (FULLY CONNECTED)
    private void drawSpecialConnections(Graphics2D g2d) {
        g2d.setColor(new Color(0, 150, 0, 120)); // Hijau transparan
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        List<Integer> nodeList = new ArrayList<>(specialNodes);
        Collections.sort(nodeList);

        // Gambar garis untuk setiap pasangan node (FULLY CONNECTED)
        for (int i = 0; i < nodeList.size(); i++) {
            int fromNode = nodeList.get(i);
            Point fromPoint = squarePositions.get(fromNode);
            if (fromPoint != null) {
                for (int j = i + 1; j < nodeList.size(); j++) {
                    int toNode = nodeList.get(j);
                    Point toPoint = squarePositions.get(toNode);
                    if (toPoint != null) {
                        int fromX = fromPoint.x + SQUARE_SIZE/2;
                        int fromY = fromPoint.y + SQUARE_SIZE/2;
                        int toX = toPoint.x + SQUARE_SIZE/2;
                        int toY = toPoint.y + SQUARE_SIZE/2;

                        g2d.drawLine(fromX, fromY, toX, toY);
                    }
                }
            }
        }

        // Reset stroke
        g2d.setStroke(new BasicStroke(1));
    }

    // Method untuk menggambar pemain
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

    // Method untuk menggambar status
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

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public void setStatusMessage(String message) {
        this.statusMessage = message;
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

    public void printSpecialNodes() {
        System.out.println("=== FULLY CONNECTED SPECIAL NODES ===");
        List<Integer> nodeList = new ArrayList<>(specialNodes);
        Collections.sort(nodeList);

        for (int node : nodeList) {
            List<Integer> connections = new ArrayList<>(specialGraph.get(node));
            Collections.sort(connections);
            System.out.println("Node " + node + " -> " + connections);
        }
        System.out.println("Total nodes: " + specialNodes.size());
        System.out.println("Total koneksi: " + countTotalConnections() + " (fully connected)");
        System.out.println("Node positions: " + nodeList);
    }

    // Getter untuk special nodes
    public Set<Integer> getSpecialNodes() {
        return specialNodes;
    }

    public Map<Integer, List<Integer>> getSpecialGraph() {
        return specialGraph;
    }

    // Method untuk mendapatkan info special nodes sebagai string
    public String getSpecialNodesInfo() {
        List<Integer> nodeList = new ArrayList<>(specialNodes);
        Collections.sort(nodeList);
        return "Node Khusus: " + nodeList + " (FULLY CONNECTED)";
    }

    // Method untuk menambahkan tangga
    public void addLadder(int from, int to) {
        if (from < to && from >= 1 && to <= 100) {
            graph.get(from).add(to);
        }
    }

    // Method untuk menambahkan ular
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
}