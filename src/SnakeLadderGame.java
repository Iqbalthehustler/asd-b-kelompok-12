import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class SnakeLadderGame extends JFrame {
    private BoardPanel boardPanel;
    private ControlPanel controlPanel;
    private Dice dice;
    private Player[] players;
    private int currentPlayerIndex;
    private boolean gameRunning;
    private JMenuBar menuBar;
    private boolean doubleTurn; // Flag untuk double turn
    private int doubleTurnCount; // Counter untuk double turn
    private boolean hasProtection = false; // Flag untuk protection bonus
    private boolean guaranteedGreenDice = false; // Flag untuk dadu hijau terjamin
    private boolean extraRoll = false; // Flag untuk roll lagi
    private boolean[] primePowerUp; // Flag untuk power up kotak prima per player

    public SnakeLadderGame() {
        initializeGUI();
        showPlayerSetup();
    }

    private void initializeGUI() {
        setTitle("🎯 Game Ular Tangga OOP dengan Double Turn, Node Khusus & Power Up Prima 🎯");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize game components
        dice = new Dice();
        gameRunning = false;
        doubleTurn = false;
        doubleTurnCount = 0;

        // Initialize panels
        boardPanel = new BoardPanel();
        controlPanel = new ControlPanel();
        boardPanel.setControlPanel(controlPanel);

        // Create menu bar
        createMenuBar();

        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        // Set button listeners
        controlPanel.setRollButtonListener(new RollDiceListener());
        controlPanel.setSpeedControlListeners(
                new SpeedSliderListener(),
                new ResetSpeedListener()
        );

        pack();
        setLocationRelativeTo(null);
    }

    private void createMenuBar() {
        menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGameItem = new JMenuItem("Game Baru");
        JMenuItem debugBoardItem = new JMenuItem("Debug Board");
        JMenuItem debugGraphItem = new JMenuItem("Debug Graph");
        JMenuItem debugSpecialNodesItem = new JMenuItem("Debug Special Nodes");
        JMenuItem showSpecialNodesItem = new JMenuItem("Show Special Nodes");
        JMenuItem exitItem = new JMenuItem("Keluar");

        newGameItem.addActionListener(e -> showPlayerSetup());
        debugBoardItem.addActionListener(e -> boardPanel.printBoardLayout());
        debugGraphItem.addActionListener(e -> boardPanel.printGraph());
        debugSpecialNodesItem.addActionListener(e -> boardPanel.printSpecialNodes());
        showSpecialNodesItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    boardPanel.getSpecialNodesInfo(),
                    "Info Node Khusus",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.add(debugBoardItem);
        gameMenu.add(debugGraphItem);
        gameMenu.add(debugSpecialNodesItem);
        gameMenu.add(showSpecialNodesItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }

    private void showPlayerSetup() {
        String[] playerNames = PlayerDialog.showPlayerDialog(this);
        if (playerNames != null) {
            initializePlayers(playerNames);
            boardPanel.resetSpecialNodes(); // Reset node khusus untuk game baru
            resetBonusFlags();
            startGame();
        }
    }

    private void initializePlayers(String[] playerNames) {
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};
        players = new Player[playerNames.length];
        primePowerUp = new boolean[playerNames.length]; // Initialize power up array

        for (int i = 0; i < playerNames.length; i++) {
            players[i] = new Player(playerNames[i], colors[i], i);
            primePowerUp[i] = false; // Reset power up status
        }

        boardPanel.setPlayers(players);
        controlPanel.setPlayers(players);
        currentPlayerIndex = 0;
        gameRunning = true;
        doubleTurn = false;
        doubleTurnCount = 0;
    }

    private void resetBonusFlags() {
        hasProtection = false;
        guaranteedGreenDice = false;
        extraRoll = false;
        if (primePowerUp != null) {
            for (int i = 0; i < primePowerUp.length; i++) {
                primePowerUp[i] = false;
            }
        }
    }

    private void startGame() {
        controlPanel.addGameLog("=== GAME DIMULAI ===");
        controlPanel.addGameLog("Jumlah pemain: " + players.length);
        for (Player player : players) {
            controlPanel.addGameLog("Pemain " + (player.getPlayerNumber() + 1) + ": " + player.getName());
        }
        controlPanel.addGameLog("Papan: START di kiri bawah, FINISH di kanan atas");
        controlPanel.addGameLog("Aturan: Dadu Hijau (80%) = Maju, Dadu Merah (20%) = Mundur");
        controlPanel.addGameLog("Fitur SPECIAL: Dadu kelipatan 5 (5) = DOUBLE TURN!");
        controlPanel.addGameLog("🎯 FITUR BARU: 5 NODE KHUSUS hijau yang SALING TERHUBUNG!");
        controlPanel.addGameLog("🎁 Efek: Teleport, Extra Turn, Protection, Lompat Langkah, Roll Lagi!");
        controlPanel.addGameLog("🔥 POWER UP PRIMA: Mendarat di kotak prima -> power up aktif di giliran berikutnya!");
        controlPanel.addGameLog("📍 " + boardPanel.getSpecialNodesInfo());
        controlPanel.addGameLog("Fitur: Gunakan slider untuk mengatur kecepatan animasi");
        controlPanel.addGameLog("=================================");

        updateDisplay();
    }

    private void updateDisplay() {
        if (players != null && currentPlayerIndex < players.length) {
            Player currentPlayer = players[currentPlayerIndex];
            String status;

            if (doubleTurn) {
                status = String.format("Giliran: %s - Posisi: %d (DOUBLE TURN %d/2)",
                        currentPlayer.getName(),
                        currentPlayer.getPosition(),
                        doubleTurnCount);
            } else {
                status = String.format("Giliran: %s - Posisi: %d",
                        currentPlayer.getName(),
                        currentPlayer.getPosition());
            }

            // Tambahkan info bonus jika ada
            if (hasProtection) {
                status += " 🛡️";
            }
            if (guaranteedGreenDice) {
                status += " 🍀";
            }
            if (extraRoll) {
                status += " 🎲";
            }
            if (primePowerUp[currentPlayerIndex]) {
                status += " 🔥";
            }

            controlPanel.setCurrentPlayer(currentPlayer.getName());
            boardPanel.setStatusMessage(status);
            controlPanel.updatePlayerPositions();
        }
        boardPanel.repaint();
    }

    private class RollDiceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!gameRunning || boardPanel.isAnimating()) return;

            controlPanel.enableRollButton(false);
            animateDiceRoll();
        }
    }

    private void animateDiceRoll() {
        Player currentPlayer = players[currentPlayerIndex];

        if (doubleTurn) {
            controlPanel.addGameLog(currentPlayer.getName() + " mengocok dadu (Double Turn " + doubleTurnCount + "/2)...");
        } else if (extraRoll) {
            controlPanel.addGameLog(currentPlayer.getName() + " mengocok dadu (Bonus Roll)...");
            extraRoll = false;
        } else {
            controlPanel.addGameLog(currentPlayer.getName() + " mengocok dadu...");
        }

        Timer diceTimer = new Timer(100, new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (count < 5) {
                    controlPanel.setDiceResult((int)(Math.random() * 6) + 1, true);
                    count++;
                } else {
                    ((Timer)e.getSource()).stop();
                    processDiceRoll();
                }
            }
        });
        diceTimer.start();
    }

    private void processDiceRoll() {
        Player currentPlayer = players[currentPlayerIndex];

        // Gunakan dadu hijau terjamin jika ada bonus
        DiceResult result;
        if (guaranteedGreenDice) {
            int number = dice.roll().getNumber();
            result = new DiceResult(number, true);
            guaranteedGreenDice = false;
            controlPanel.addGameLog("🍀 " + currentPlayer.getName() + " menggunakan DADU HIJAU TERJAMIN!");
        } else {
            result = dice.roll();
        }

        // Tampilkan hasil dadu dengan info double turn jika ada
        controlPanel.setDiceResult(result.getNumber(), result.isGreen());

        int oldPosition = currentPlayer.getPosition();
        int diceNumber = result.getNumber();

        // Cek apakah power up prima aktif
        if (primePowerUp[currentPlayerIndex]) {
            controlPanel.addGameLog("🔥 " + currentPlayer.getName() + " POWER UP PRIMA AKTIF!");
            processPrimePowerUpMove(currentPlayer, oldPosition, diceNumber, result);
        } else {
            int newPosition = calculateNewPosition(oldPosition, result);

            String moveType = result.isGreen() ? "MAJU" : "MUNDUR";
            String logMessage = String.format("%s: %s %d langkah (%d → %d)",
                    currentPlayer.getName(), moveType, diceNumber, oldPosition, newPosition);

            // Tambahkan info double turn jika dapat kelipatan 5
            if (result.isMultipleOfFive()) {
                logMessage += " 🎯 DOUBLE TURN!";
                if (!doubleTurn) {
                    doubleTurn = true;
                    doubleTurnCount = 1;
                    controlPanel.addGameLog("🎉 " + currentPlayer.getName() + " dapat DOUBLE TURN! 🎉");
                }
            }

            controlPanel.addGameLog(logMessage);

            // Cek apakah mendarat di node khusus
            boolean landedOnSpecialNode = boardPanel.getSpecialNodes().contains(newPosition);
            if (landedOnSpecialNode) {
                controlPanel.addGameLog("🎯 " + currentPlayer.getName() + " mendarat di NODE KHUSUS " + newPosition + "!");
            }

            // Cek apakah mendarat di kotak prima untuk power up giliran berikutnya
            boolean landedOnPrime = boardPanel.isPrimeNumber(newPosition);
            if (landedOnPrime && newPosition > 1) {
                controlPanel.addGameLog("🔴 " + currentPlayer.getName() + " mendarat di KOTAK PRIMA " + newPosition + "! Power Up aktif untuk giliran berikutnya!");
                primePowerUp[currentPlayerIndex] = true;
            }

            // Buat final copies untuk digunakan dalam lambda
            final int finalNewPosition = newPosition;
            final boolean finalLandedOnSpecialNode = landedOnSpecialNode;
            final DiceResult finalResult = result;

            // Animasikan pergerakan player
            boardPanel.animatePlayerMovement(currentPlayer, oldPosition, newPosition, () -> {
                currentPlayer.setPosition(finalNewPosition);

                // Cek efek node khusus setelah animasi selesai
                if (finalLandedOnSpecialNode) {
                    checkSpecialNodeEffects(currentPlayer, finalNewPosition, finalResult);
                } else {
                    checkGameEndAndNextTurn(finalResult);
                }
            });
        }

        // Update kecepatan animasi berdasarkan slider
        updateAnimationSpeed();
    }

    private void processPrimePowerUpMove(Player player, int startPosition, int diceNumber, DiceResult result) {
        controlPanel.addGameLog("🎯 Power Up Mode: Mencari node terdekat dengan " + diceNumber + " langkah...");

        // Cari node terdekat dari posisi awal
        Integer nearestNode = boardPanel.getNearestSpecialNode(startPosition);

        if (nearestNode != null) {
            int distanceToNode = Math.abs(nearestNode - startPosition);

            if (distanceToNode <= diceNumber) {
                // Bisa mencapai node
                int remainingSteps = diceNumber - distanceToNode;
                controlPanel.addGameLog("📍 Menuju node " + nearestNode + " (" + distanceToNode + " langkah)");

                // Buat final copies untuk lambda
                final Integer finalNearestNode = nearestNode;
                final int finalRemainingSteps = remainingSteps;
                final DiceResult finalResult = result;

                // Pindah ke node
                boardPanel.animatePlayerMovement(player, startPosition, nearestNode, () -> {
                    player.setPosition(finalNearestNode);

                    if (finalRemainingSteps > 0) {
                        controlPanel.addGameLog("🔄 Sisa " + finalRemainingSteps + " langkah, menjelajahi graph node...");
                        exploreNodeGraph(player, finalNearestNode, finalRemainingSteps, finalResult);
                    } else {
                        controlPanel.addGameLog("✅ Power Up selesai!");
                        primePowerUp[player.getPlayerNumber()] = false;
                        checkSpecialNodeEffects(player, finalNearestNode, finalResult);
                    }
                });
            } else {
                // Tidak bisa mencapai node manapun, gerak normal
                controlPanel.addGameLog("❌ Tidak ada node yang bisa dijangkau, gerak normal...");
                int newPosition = calculateNewPosition(startPosition, result);

                // Buat final copy untuk lambda
                final int finalNewPosition = newPosition;
                final DiceResult finalResult = result;

                boardPanel.animatePlayerMovement(player, startPosition, newPosition, () -> {
                    player.setPosition(finalNewPosition);
                    primePowerUp[player.getPlayerNumber()] = false;
                    checkGameEndAndNextTurn(finalResult);
                });
            }
        } else {
            // Tidak ada node, gerak normal
            controlPanel.addGameLog("❌ Tidak ada node khusus, gerak normal...");
            int newPosition = calculateNewPosition(startPosition, result);

            // Buat final copy untuk lambda
            final int finalNewPosition = newPosition;
            final DiceResult finalResult = result;

            boardPanel.animatePlayerMovement(player, startPosition, newPosition, () -> {
                player.setPosition(finalNewPosition);
                primePowerUp[player.getPlayerNumber()] = false;
                checkGameEndAndNextTurn(finalResult);
            });
        }
    }

    private void exploreNodeGraph(Player player, int currentNode, int remainingSteps, DiceResult result) {
        if (remainingSteps <= 0) {
            controlPanel.addGameLog("✅ Power Up selesai!");
            primePowerUp[player.getPlayerNumber()] = false;
            checkSpecialNodeEffects(player, currentNode, result);
            return;
        }

        java.util.Map<Integer, java.util.List<Integer>> specialGraph = boardPanel.getSpecialGraph();
        if (specialGraph == null || !specialGraph.containsKey(currentNode)) {
            controlPanel.addGameLog("❌ Tidak ada node terhubung, Power Up berakhir!");
            primePowerUp[player.getPlayerNumber()] = false;
            checkSpecialNodeEffects(player, currentNode, result);
            return;
        }

        java.util.List<Integer> connectedNodes = specialGraph.get(currentNode);
        if (connectedNodes == null || connectedNodes.isEmpty()) {
            controlPanel.addGameLog("❌ Tidak ada node terhubung, Power Up berakhir!");
            primePowerUp[player.getPlayerNumber()] = false;
            checkSpecialNodeEffects(player, currentNode, result);
            return;
        }

        // Cari node terdekat dari koneksi
        Integer nextNode = null;
        int minDistance = Integer.MAX_VALUE;

        for (int node : connectedNodes) {
            // Cari node yang belum dikunjungi (sederhana: pilih yang berbeda dari current)
            if (node != currentNode) {
                int distance = Math.abs(node - currentNode);
                if (distance < minDistance) {
                    minDistance = distance;
                    nextNode = node;
                }
            }
        }

        if (nextNode != null && minDistance <= remainingSteps) {
            controlPanel.addGameLog("➡️  Menuju node " + nextNode + " (" + minDistance + " langkah)");

            // Buat final copies untuk lambda
            final Integer finalNextNode = nextNode;
            final int finalMinDistance = minDistance;
            final int finalRemainingSteps = remainingSteps;
            final DiceResult finalResult = result;

            boardPanel.animatePlayerMovement(player, currentNode, nextNode, () -> {
                player.setPosition(finalNextNode);
                int newRemaining = finalRemainingSteps - finalMinDistance;
                exploreNodeGraph(player, finalNextNode, newRemaining, finalResult);
            });
        } else {
            // Tidak bisa bergerak lebih lanjut di graph
            controlPanel.addGameLog("✅ Power Up selesai! Tidak bisa bergerak lebih lanjut di graph.");
            primePowerUp[player.getPlayerNumber()] = false;
            checkSpecialNodeEffects(player, currentNode, result);
        }
    }

    private void checkSpecialNodeEffects(Player player, int newPosition, DiceResult lastRoll) {
        Random random = new Random();
        int effect = random.nextInt(6); // 6 efek berbeda

        switch (effect) {
            case 0:
                // Teleport ke node khusus random lainnya
                Integer randomNode = boardPanel.getRandomSpecialNode(newPosition);
                if (randomNode != null) {
                    controlPanel.addGameLog("🚀 " + player.getName() + " TELEPORT ke node " + randomNode + "!");

                    // Buat final copy untuk lambda
                    final Integer finalRandomNode = randomNode;
                    final DiceResult finalLastRoll = lastRoll;

                    boardPanel.animatePlayerMovement(player, newPosition, randomNode, () -> {
                        player.setPosition(finalRandomNode);
                        checkGameEndAndNextTurn(finalLastRoll);
                    });
                } else {
                    checkGameEndAndNextTurn(lastRoll);
                }
                break;

            case 1:
                // Bonus extra turn
                controlPanel.addGameLog("🔄 " + player.getName() + " dapat EXTRA TURN dari node khusus!");
                if (!doubleTurn) {
                    doubleTurn = true;
                    doubleTurnCount = 1;
                }
                checkGameEndAndNextTurn(lastRoll);
                break;

            case 2:
                // Bonus dadu hijau terjamin untuk turn berikutnya
                guaranteedGreenDice = true;
                controlPanel.addGameLog("🍀 " + player.getName() + " dapat DADU HIJAU TERJAMIN untuk giliran berikutnya!");
                checkGameEndAndNextTurn(lastRoll);
                break;

            case 3:
                // Lompat 3-7 langkah maju
                int bonusSteps = random.nextInt(5) + 3; // 3-7 langkah
                int bonusPosition = Math.min(player.getPosition() + bonusSteps, 100);
                controlPanel.addGameLog("⚡ " + player.getName() + " LOMPAT " + bonusSteps +
                        " LANGKAH! (" + player.getPosition() + " → " + bonusPosition + ")");

                // Buat final copies untuk lambda
                final int finalBonusPosition = bonusPosition;
                final DiceResult finalLastRoll = lastRoll;

                boardPanel.animatePlayerMovement(player, player.getPosition(), bonusPosition, () -> {
                    player.setPosition(finalBonusPosition);
                    checkGameEndAndNextTurn(finalLastRoll);
                });
                break;

            case 4:
                // Protection dari dadu merah (1 turn)
                hasProtection = true;
                controlPanel.addGameLog("🛡️ " + player.getName() + " dapat PROTECTION dari dadu merah untuk 1 turn!");
                checkGameEndAndNextTurn(lastRoll);
                break;

            case 5:
                // Roll dadu lagi
                extraRoll = true;
                controlPanel.addGameLog("🎲 " + player.getName() + " dapat ROLL LAGI!");
                controlPanel.enableRollButton(true);
                updateDisplay();
                break;
        }

        // Tampilkan info koneksi fully connected
        controlPanel.addGameLog("📍 Node " + newPosition + " terhubung ke SEMUA node khusus lainnya!");
    }

    private int calculateNewPosition(int currentPosition, DiceResult result) {
        int newPosition;

        // Jika ada protection dan dadu merah, ubah jadi hijau
        if (hasProtection && result.isRed()) {
            controlPanel.addGameLog("🛡️ Protection aktif! Dadu merah diubah menjadi hijau!");
            newPosition = currentPosition + result.getNumber();
            hasProtection = false; // Protection habis setelah digunakan
        } else {
            if (result.isGreen()) {
                newPosition = currentPosition + result.getNumber();
            } else {
                newPosition = currentPosition - result.getNumber();
            }
        }

        if (newPosition < 1) newPosition = 1;
        if (newPosition > 100) newPosition = 100;

        return newPosition;
    }

    private void checkGameEndAndNextTurn(DiceResult result) {
        Player currentPlayer = players[currentPlayerIndex];

        // Cek jika pemain menang
        if (currentPlayer.getPosition() == 100) {
            gameRunning = false;
            controlPanel.addGameLog("🎉🎉🎉 " + currentPlayer.getName() + " MENANG! 🎉🎉🎉");
            boardPanel.setStatusMessage(currentPlayer.getName() + " MENANG!");

            JOptionPane.showMessageDialog(this,
                    "🎉 " + currentPlayer.getName() + " MENANG! 🎉\n\n" +
                            "Klasemen Akhir:\n" + getFinalRankings(),
                    "Game Selesai",
                    JOptionPane.INFORMATION_MESSAGE);

            controlPanel.enableRollButton(false);
            return;
        }

        // Logic untuk double turn
        if (doubleTurn) {
            if (doubleTurnCount < 2) {
                // Masih dalam double turn, giliran berikutnya masih pemain yang sama
                doubleTurnCount++;
                controlPanel.addGameLog("🔄 " + currentPlayer.getName() + " dapat giliran lagi! (" + doubleTurnCount + "/2)");
                controlPanel.enableRollButton(true);
                updateDisplay();
            } else {
                // Double turn selesai, lanjut ke pemain berikutnya
                controlPanel.addGameLog("✅ Double turn " + currentPlayer.getName() + " selesai");
                doubleTurn = false;
                doubleTurnCount = 0;
                currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
                controlPanel.enableRollButton(true);
                updateDisplay();
            }
        } else if (extraRoll) {
            // Bonus roll lagi, tetap di pemain yang sama
            controlPanel.enableRollButton(true);
            updateDisplay();
        } else {
            // Normal turn, lanjut ke pemain berikutnya
            // Jika dapat kelipatan 5, aktifkan double turn untuk pemain berikutnya
            if (result.isMultipleOfFive()) {
                doubleTurn = true;
                doubleTurnCount = 1;
                // Tetap di pemain yang sama untuk double turn
                controlPanel.addGameLog("🎉 " + currentPlayer.getName() + " dapat DOUBLE TURN! 🎉");
                controlPanel.enableRollButton(true);
                updateDisplay();
            } else {
                // Normal turn, lanjut ke pemain berikutnya
                currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
                controlPanel.enableRollButton(true);
                updateDisplay();
            }
        }
    }

    // Listeners untuk kontrol kecepatan dengan slider
    private class SpeedSliderListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            int speed = controlPanel.getSpeedValue();

            if (!((JSlider)e.getSource()).getValueIsAdjusting()) {
                boardPanel.getAnimationPanel().setAnimationSpeed(speed);
                updateSpeedLabel();

                String speedText = getSpeedDescription(speed);
                controlPanel.addGameLog("🎚️ Kecepatan animasi diubah: " + speedText);
            }
        }
    }

    private class ResetSpeedListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            controlPanel.setSpeedValue(100);
            boardPanel.getAnimationPanel().resetSpeed();
            updateSpeedLabel();
            controlPanel.addGameLog("🔄 Kecepatan animasi direset ke normal");
        }
    }

    private void updateAnimationSpeed() {
        int speed = controlPanel.getSpeedValue();
        boardPanel.getAnimationPanel().setAnimationSpeed(speed);
    }

    private void updateSpeedLabel() {
        int speed = controlPanel.getSpeedValue();
        String speedText = getSpeedDescription(speed);
        controlPanel.updateSpeedLabel(speedText);
    }

    private String getSpeedDescription(int speed) {
        if (speed <= 40) return "Sangat Cepat (" + speed + "ms)";
        if (speed <= 80) return "Cepat (" + speed + "ms)";
        if (speed <= 120) return "Normal (" + speed + "ms)";
        if (speed <= 200) return "Lambat (" + speed + "ms)";
        return "Sangat Lambat (" + speed + "ms)";
    }

    private String getFinalRankings() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < players.length; i++) {
            sb.append((i + 1)).append(". ").append(players[i].getName())
                    .append(" - Posisi: ").append(players[i].getPosition()).append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SnakeLadderGame().setVisible(true);
            }
        });
    }
}