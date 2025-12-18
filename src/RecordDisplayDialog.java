import javax.swing.*;
import java.awt.*;

public class RecordDisplayDialog extends JDialog {

    // Constructor menerima: Owner, Manager, dan Boolean Mode
    public RecordDisplayDialog(Frame owner, GlobalRankingManager mgr, boolean isArchiveMode) {
        super(owner, isArchiveMode ? "📚 ARSIP HISTORY (LAMA)" : "🏆 GLOBAL STATS (SAAT INI)", true);
        setSize(600, 450);
        setLayout(new GridLayout(1, 2, 10, 10));

        String suffix = isArchiveMode ? " (ARSIP)" : " (AKTIF)";

        // Kolom 1: Top 3 Scores
        JTextArea txtScores = new JTextArea();
        txtScores.setEditable(false);
        txtScores.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtScores.append("=== 🏆 TOP 3 SCORES" + suffix + " ===\n\n");

        for(String s : mgr.getTop3ScoresViaPQ(isArchiveMode)) {
            txtScores.append(s + "\n");
        }

        // Kolom 2: Top 3 Winners
        JTextArea txtWinners = new JTextArea();
        txtWinners.setEditable(false);
        txtWinners.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtWinners.append("=== 👑 TOP 3 WINNERS" + suffix + " ===\n\n");

        for(String s : mgr.getTop3WinnersViaPQ(isArchiveMode)) {
            txtWinners.append(s + "\n");
        }

        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBorder(BorderFactory.createEmptyBorder(10,10,10,5));
        p1.add(new JScrollPane(txtScores));

        JPanel p2 = new JPanel(new BorderLayout());
        p2.setBorder(BorderFactory.createEmptyBorder(10,5,10,10));
        p2.add(new JScrollPane(txtWinners));

        add(p1); add(p2);
        setLocationRelativeTo(owner);
    }
}