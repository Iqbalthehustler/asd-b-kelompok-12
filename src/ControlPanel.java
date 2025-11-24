import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Hashtable;

public class ControlPanel extends JPanel {
    private JButton rollButton;
    private JLabel diceResultLabel;
    private JLabel currentPlayerLabel;
    private JTextArea gameLog;
    private JPanel playersInfoPanel;
    private Player[] players;
    private JLabel speedLabel;
    private JSlider speedSlider;
    private JButton resetSpeedButton;

    public ControlPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(350, 650));
        setBackground(new Color(250, 250, 250));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeComponents();
    }

    private void initializeComponents() {
        // Panel atas untuk informasi pemain
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(220, 220, 220));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        currentPlayerLabel = new JLabel("Pemain Saat Ini: -");
        currentPlayerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        currentPlayerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        diceResultLabel = new JLabel("Dadu: -");
        diceResultLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        diceResultLabel.setHorizontalAlignment(SwingConstants.CENTER);

        topPanel.add(currentPlayerLabel, BorderLayout.NORTH);
        topPanel.add(diceResultLabel, BorderLayout.CENTER);

        // Panel info pemain
        playersInfoPanel = new JPanel();
        playersInfoPanel.setLayout(new GridLayout(4, 1, 5, 5));
        playersInfoPanel.setBorder(BorderFactory.createTitledBorder("Info Pemain"));
        playersInfoPanel.setPreferredSize(new Dimension(300, 120));
        updatePlayersInfo();

        // Panel kontrol kecepatan animasi dengan slider
        JPanel speedControlPanel = createSpeedControlPanel();

        // Tombol roll dice
        rollButton = new JButton("🎲 Lempar Dadu 🎲");
        rollButton.setFont(new Font("Arial", Font.BOLD, 16));
        rollButton.setBackground(new Color(100, 150, 255));
        rollButton.setForeground(Color.WHITE);
        rollButton.setFocusPainted(false);
        rollButton.setBorder(BorderFactory.createRaisedBevelBorder());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(250, 250, 250));
        buttonPanel.add(rollButton);

        // Area log game
        gameLog = new JTextArea(12, 25);
        gameLog.setEditable(false);
        gameLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        gameLog.setBackground(new Color(240, 240, 240));
        gameLog.setBorder(BorderFactory.createTitledBorder("Log Permainan"));
        JScrollPane scrollPane = new JScrollPane(gameLog);

        // Panel tengah gabungan
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(250, 250, 250));

        // Tambahkan komponen dengan spacing yang tepat
        centerPanel.add(playersInfoPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        centerPanel.add(speedControlPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        centerPanel.add(buttonPanel);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
    }

    private JPanel createSpeedControlPanel() {
        JPanel speedPanel = new JPanel();
        speedPanel.setLayout(new BoxLayout(speedPanel, BoxLayout.Y_AXIS));
        speedPanel.setBorder(BorderFactory.createTitledBorder("Kontrol Kecepatan Animasi"));
        speedPanel.setBackground(new Color(240, 240, 240));
        speedPanel.setPreferredSize(new Dimension(300, 140));
        speedPanel.setMaximumSize(new Dimension(300, 140));

        // Label kecepatan
        speedLabel = new JLabel("Kecepatan: Normal (100ms)", SwingConstants.CENTER);
        speedLabel.setFont(new Font("Arial", Font.BOLD, 12));
        speedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Slider untuk kecepatan
        speedSlider = new JSlider(JSlider.HORIZONTAL, 20, 500, 100);
        speedSlider.setMajorTickSpacing(100);
        speedSlider.setMinorTickSpacing(20);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setSnapToTicks(false);

        // Custom label untuk slider
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(20, new JLabel("Sangat Cepat"));
        labelTable.put(100, new JLabel("Normal"));
        labelTable.put(500, new JLabel("Sangat Lambat"));
        speedSlider.setLabelTable(labelTable);

        speedSlider.setFont(new Font("Arial", Font.PLAIN, 9));
        speedSlider.setBackground(new Color(240, 240, 240));
        speedSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        speedSlider.setMaximumSize(new Dimension(280, 60));

        // Panel untuk slider
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
        sliderPanel.setBackground(new Color(240, 240, 240));
        sliderPanel.add(speedLabel);
        sliderPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        sliderPanel.add(speedSlider);

        // Tombol reset
        resetSpeedButton = new JButton("🔄 Reset Kecepatan");
        resetSpeedButton.setFont(new Font("Arial", Font.BOLD, 11));
        resetSpeedButton.setBackground(new Color(200, 200, 255));
        resetSpeedButton.setForeground(Color.BLACK);
        resetSpeedButton.setFocusPainted(false);
        resetSpeedButton.setBorder(BorderFactory.createRaisedBevelBorder());
        resetSpeedButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(resetSpeedButton);

        speedPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer atas
        speedPanel.add(sliderPanel);
        speedPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        speedPanel.add(buttonPanel);

        return speedPanel;
    }

    public void setSpeedControlListeners(ChangeListener sliderListener,
                                         ActionListener resetSpeedListener) {
        speedSlider.addChangeListener(sliderListener);
        resetSpeedButton.addActionListener(resetSpeedListener);
    }

    public void updateSpeedLabel(String speedText) {
        speedLabel.setText("Kecepatan: " + speedText);
    }

    public void setSpeedValue(int speed) {
        speedSlider.setValue(speed);
    }

    public int getSpeedValue() {
        return speedSlider.getValue();
    }

    public void setPlayers(Player[] players) {
        this.players = players;
        updatePlayersInfo();
    }

    private void updatePlayersInfo() {
        playersInfoPanel.removeAll();

        if (players != null) {
            for (Player player : players) {
                if (player != null) {
                    JPanel playerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    playerPanel.setBackground(new Color(240, 240, 240));
                    playerPanel.setPreferredSize(new Dimension(280, 25));

                    // Color indicator
                    JLabel colorLabel = new JLabel("●");
                    colorLabel.setForeground(player.getColor());
                    colorLabel.setFont(new Font("Arial", Font.BOLD, 16));

                    // Player info
                    JLabel infoLabel = new JLabel(player.getName() + ": Posisi " + player.getPosition());
                    infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));

                    playerPanel.add(colorLabel);
                    playerPanel.add(infoLabel);
                    playersInfoPanel.add(playerPanel);
                }
            }
        } else {
            JLabel noPlayersLabel = new JLabel("Belum ada pemain");
            noPlayersLabel.setHorizontalAlignment(SwingConstants.CENTER);
            playersInfoPanel.add(noPlayersLabel);
        }

        playersInfoPanel.revalidate();
        playersInfoPanel.repaint();
    }

    public void setRollButtonListener(ActionListener listener) {
        rollButton.addActionListener(listener);
    }

    public void setCurrentPlayer(String playerInfo) {
        currentPlayerLabel.setText("Pemain Saat Ini: " + playerInfo);
        updatePlayersInfo();
    }

    public void setDiceResult(int number, boolean isGreen) {
        String color = isGreen ? "Hijau" : "Merah";
        String colorCode = isGreen ? "#00AA00" : "#FF0000";
        String multipleInfo = (number % 5 == 0) ? " 🎯 DOUBLE TURN!" : "";

        diceResultLabel.setText(String.format(
                "<html>Dadu: <b style='font-size:16px'>%d</b> - <font color='%s'><b>%s</b></font>%s</html>",
                number, colorCode, color, multipleInfo
        ));
    }

    public void addGameLog(String message) {
        gameLog.append(message + "\n");
        gameLog.setCaretPosition(gameLog.getDocument().getLength());
    }

    public void enableRollButton(boolean enable) {
        rollButton.setEnabled(enable);
        if (enable) {
            rollButton.setBackground(new Color(100, 150, 255));
            rollButton.setText("🎲 Lempar Dadu 🎲");
        } else {
            rollButton.setBackground(Color.GRAY);
            rollButton.setText("Mengocok...");
        }
    }

    public void updatePlayerPositions() {
        updatePlayersInfo();
    }

    // HAPUS method setSpeedControlsEnabled - biarkan slider selalu aktif
    // public void setSpeedControlsEnabled(boolean enabled) {
    //     speedSlider.setEnabled(enabled);
    //     resetSpeedButton.setEnabled(enabled);
    // }
}