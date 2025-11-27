import javax.swing.*;
import java.awt.*;

public class PlayerDialog {
    public static String[] showPlayerDialog(JFrame parent) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));

        JComboBox<Integer> playerCountCombo = new JComboBox<>(new Integer[]{2, 3, 4});
        JLabel countLabel = new JLabel("Jumlah Pemain:");

        JPanel namesPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField[] nameFields = new JTextField[4];

        for (int i = 0; i < 4; i++) {
            nameFields[i] = new JTextField("Pemain " + (i + 1), 15);
            namesPanel.add(new JLabel("Pemain " + (i + 1) + ":"));
            namesPanel.add(nameFields[i]);

            if (i >= 2) {
                nameFields[i].setEnabled(false);
            }
        }

        playerCountCombo.addActionListener(e -> {
            int count = (Integer) playerCountCombo.getSelectedItem();
            for (int i = 0; i < 4; i++) {
                nameFields[i].setEnabled(i < count);
            }
        });

        panel.add(countLabel);
        panel.add(playerCountCombo);
        panel.add(new JLabel("Nama Pemain:"));
        panel.add(namesPanel);

        int result = JOptionPane.showConfirmDialog(parent, panel,
                "Setup Pemain", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int playerCount = (Integer) playerCountCombo.getSelectedItem();
            String[] playerNames = new String[playerCount];

            for (int i = 0; i < playerCount; i++) {
                String name = nameFields[i].getText().trim();
                if (name.isEmpty()) {
                    name = "Pemain " + (i + 1);
                }
                playerNames[i] = name;
            }

            return playerNames;
        }

        return null;
    }
}