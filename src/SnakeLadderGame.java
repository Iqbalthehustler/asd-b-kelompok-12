import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

    public SnakeLadderGame() {
        initializeGUI();
        showPlayerSetup();
    }

    private void initializeGUI() {
        setTitle("🎯 Game Ular Tangga OOP dengan Double Turn 🎯");
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
        JMenuItem exitItem = new JMenuItem("Keluar");

        newGameItem.addActionListener(e -> showPlayerSetup());
        debugBoardItem.addActionListener(e -> boardPanel.printBoardLayout());
        debugGraphItem.addActionListener(e -> boardPanel.printGraph());
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.add(debugBoardItem);
        gameMenu.add(debugGraphItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }

    private void showPlayerSetup() {
        String[] playerNames = PlayerDialog.showPlayerDialog(this);
        if (playerNames != null) {
            initializePlayers(playerNames);
            startGame();
        }
    }

    private void initializePlayers(String[] playerNames) {
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};
        players = new Player[playerNames.length];

        for (int i = 0; i < playerNames.length; i++) {
            players[i] = new Player(playerNames[i], colors[i], i);
        }

        boardPanel.setPlayers(players);
        controlPanel.setPlayers(players);
        currentPlayerIndex = 0;
        gameRunning = true;
        doubleTurn = false;
        doubleTurnCount = 0;
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
        DiceResult result = dice.roll();

        // Tampilkan hasil dadu dengan info double turn jika ada
        controlPanel.setDiceResult(result.getNumber(), result.isGreen());

        int oldPosition = currentPlayer.getPosition();
        int newPosition = calculateNewPosition(oldPosition, result);

        String moveType = result.isGreen() ? "MAJU" : "MUNDUR";
        String logMessage = String.format("%s: %s %d langkah (%d → %d)",
                currentPlayer.getName(), moveType, result.getNumber(), oldPosition, newPosition);

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

        // Animasikan pergerakan player
        boardPanel.animatePlayerMovement(currentPlayer, oldPosition, newPosition, () -> {
            currentPlayer.setPosition(newPosition);
            checkGameEndAndNextTurn(result);
        });

        // Update kecepatan animasi berdasarkan slider
        updateAnimationSpeed();
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

    private int calculateNewPosition(int currentPosition, DiceResult result) {
        int newPosition;
        if (result.isGreen()) {
            newPosition = currentPosition + result.getNumber();
        } else {
            newPosition = currentPosition - result.getNumber();
        }

        if (newPosition < 1) newPosition = 1;
        if (newPosition > 100) newPosition = 100;

        return newPosition;
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