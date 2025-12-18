import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

public class RankingDialog extends JDialog {

    // Interface untuk menangani dua aksi berbeda
    public interface DialogAction {
        void onContinue();  // Lanjut main (akumulasi skor)
        void onMainMenu();  // Balik ke menu (reset)
    }

    public RankingDialog(Frame owner, Player[] players, DialogAction action) {
        super(owner, "Adventure Result", true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- 1. Header (Judul & Pemenang) ---
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setBackground(new Color(135, 206, 235)); // Sky Blue
        pnlHeader.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("✨ ADVENTURE COMPLETE! ✨");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Cari Pemenang (Posisi >= 25)
        String winnerName = "Unknown";
        for (Player p : players) {
            if (p.getPosition() >= 25) {
                winnerName = p.getName();
                break;
            }
        }

        JLabel lblWinner = new JLabel("🏆 WINNER: " + winnerName);
        lblWinner.setFont(new Font("Arial", Font.BOLD, 24));
        lblWinner.setForeground(Color.YELLOW);
        lblWinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Efek bayangan teks sederhana
        lblWinner.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        pnlHeader.add(lblTitle);
        pnlHeader.add(lblWinner);

        // --- 2. Scoreboard List ---
        JPanel pnlList = new JPanel();
        pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS));
        pnlList.setBackground(Color.WHITE);
        pnlList.setBorder(new EmptyBorder(20, 40, 20, 40));

        // Sort pemain berdasarkan Total Skor (Tertinggi di atas)
        Arrays.sort(players);

        for (int i = 0; i < players.length; i++) {
            Player p = players[i];

            JPanel pnlItem = new JPanel(new BorderLayout());
            pnlItem.setBackground(new Color(245, 245, 255));
            pnlItem.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,1,0, Color.LIGHT_GRAY),
                    new EmptyBorder(10, 10, 10, 10)
            ));

            // Nama & Rank
            JLabel lblName = new JLabel("#" + (i+1) + " " + p.getName());
            lblName.setFont(new Font("Arial", Font.BOLD, 14));
            lblName.setForeground(p.getColor().darker());

            // Total Skor
            JLabel lblScore = new JLabel("⭐ Total Score: " + p.getScore());
            lblScore.setFont(new Font("Arial", Font.BOLD, 14));
            lblScore.setForeground(new Color(255, 140, 0)); // Orange

            pnlItem.add(lblName, BorderLayout.WEST);
            pnlItem.add(lblScore, BorderLayout.EAST);
            pnlList.add(pnlItem);
            pnlList.add(Box.createVerticalStrut(5));
        }

        JScrollPane scroll = new JScrollPane(pnlList);
        scroll.setBorder(null);

        // --- 3. Action Buttons ---
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pnlBtn.setBackground(Color.WHITE);

        // Tombol Main Menu (Merah/Abu)
        JButton btnMenu = createButton("🏠 Main Menu", new Color(200, 100, 100));
        btnMenu.addActionListener(e -> {
            dispose();
            action.onMainMenu();
        });

        // Tombol Lanjut Main (Hijau/Biru)
        JButton btnNext = createButton("🚀 Next Round (Keep Score)", new Color(60, 179, 113));
        btnNext.addActionListener(e -> {
            dispose();
            action.onContinue();
        });

        pnlBtn.add(btnMenu);
        pnlBtn.add(btnNext);

        add(pnlHeader, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(pnlBtn, BorderLayout.SOUTH);

        setSize(450, 500);
        setLocationRelativeTo(owner);
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }
}