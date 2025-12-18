import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Arrays;

public class ControlPanel extends JPanel {
    // --- PALET WARNA TEMA SEMUT ---
    private static final Color BG_SKY_TOP = new Color(135, 206, 235);   // Langit
    private static final Color BG_SOIL_BOT = new Color(101, 67, 33);    // Tanah
    private static final Color WOOD_PANEL = new Color(139, 69, 19);     // Kayu/Tanah Gelap
    private static final Color PAPER_BG = new Color(245, 222, 179);     // Warna Kertas/Pasir
    private static final Color TEXT_DARK = new Color(50, 30, 10);       // Teks Coklat Tua
    private static final Color ACCENT_GREEN = new Color(34, 139, 34);   // Hijau Daun

    private VisualDice dice;
    private JLabel lblPlayer, lblDiceStatus;
    private JTextArea txtLog;
    private JPanel pnlRankContent; // Panel isi ranking

    public ControlPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(340, 600)); // Lebar sedikit ditambah
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initializeUI();
    }

    // Custom Background Gradient (Langit ke Tanah)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Gradasi menyatu dengan BoardPanel
        GradientPaint gp = new GradientPaint(0, 0, BG_SKY_TOP, 0, getHeight(), BG_SOIL_BOT);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void initializeUI() {
        // --- 1. HEADER (Info Pemain) ---
        JPanel pnlHeader = new TransparentPanel(new BorderLayout());
        pnlHeader.setBorder(new EmptyBorder(0, 0, 15, 0));

        lblPlayer = new JLabel("Menunggu Semut...", SwingConstants.CENTER);
        lblPlayer.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPlayer.setForeground(Color.WHITE);
        // Efek Shadow pada teks
        lblPlayer.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            public void paint(Graphics g, JComponent c) {
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(new Color(0,0,0,100));
                g.drawString(lblPlayer.getText(), 2, lblPlayer.getHeight()-4); // Shadow
                super.paint(g, c);
            }
        });

        pnlHeader.add(lblPlayer, BorderLayout.CENTER);

        // --- 2. CENTER AREA (Ranking & Dice) ---
        JPanel pnlCenter = new TransparentPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));

        // A. RANKING PANEL (Style Kayu/Batu)
        JPanel pnlRankContainer = new RoundedPanel(15, WOOD_PANEL);
        pnlRankContainer.setLayout(new BorderLayout());
        pnlRankContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblRankTitle = new JLabel("🏆 LEADERBOARD", SwingConstants.CENTER);
        lblRankTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblRankTitle.setForeground(Color.ORANGE);
        pnlRankContainer.add(lblRankTitle, BorderLayout.NORTH);

        pnlRankContent = new TransparentPanel();
        pnlRankContent.setLayout(new BoxLayout(pnlRankContent, BoxLayout.Y_AXIS));
        pnlRankContainer.add(pnlRankContent, BorderLayout.CENTER);

        // B. DICE PANEL (Area Dadu)
        JPanel pnlDiceArea = new TransparentPanel(new BorderLayout());
        pnlDiceArea.setBorder(new EmptyBorder(20, 0, 10, 0));

        // Wrapper agar dadu di tengah dengan background lingkaran
        JPanel diceWrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                // Gambar lingkaran cahaya di belakang dadu
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillOval(getWidth()/2 - 60, getHeight()/2 - 60, 120, 120);
            }
        };
        diceWrapper.setOpaque(false);

        dice = new VisualDice(); // Dadu Visual
        diceWrapper.add(dice);

        lblDiceStatus = new JLabel("Giliranmu!", SwingConstants.CENTER);
        lblDiceStatus.setFont(new Font("Arial", Font.BOLD, 16));
        lblDiceStatus.setForeground(Color.WHITE);
        lblDiceStatus.setBorder(new EmptyBorder(10,0,0,0));

        pnlDiceArea.add(diceWrapper, BorderLayout.CENTER);
        pnlDiceArea.add(lblDiceStatus, BorderLayout.SOUTH);

        pnlCenter.add(pnlRankContainer);
        pnlCenter.add(pnlDiceArea);

        // --- 3. FOOTER (Log Jurnal) ---
        JPanel pnlLog = new RoundedPanel(15, PAPER_BG);
        pnlLog.setLayout(new BorderLayout());
        pnlLog.setBorder(new EmptyBorder(5, 5, 5, 5));
        pnlLog.setPreferredSize(new Dimension(300, 180)); // Tinggi fix agar layout rapi

        JLabel lblLogTitle = new JLabel("📜 Catatan Ekspedisi", SwingConstants.LEFT);
        lblLogTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblLogTitle.setForeground(TEXT_DARK);
        lblLogTitle.setBorder(new EmptyBorder(5, 5, 5, 5));

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Monospaced", Font.BOLD, 12));
        txtLog.setForeground(TEXT_DARK);
        txtLog.setBackground(PAPER_BG);
        txtLog.setLineWrap(true);
        txtLog.setWrapStyleWord(true);

        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(null);
        scrollLog.setOpaque(false);
        scrollLog.getViewport().setOpaque(false);

        // Custom Scrollbar agar menyatu
        scrollLog.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(139, 69, 19, 100);
            }
        });

        pnlLog.add(lblLogTitle, BorderLayout.NORTH);
        pnlLog.add(scrollLog, BorderLayout.CENTER);

        // Menyusun Layout Utama
        add(pnlHeader, BorderLayout.NORTH);
        add(pnlCenter, BorderLayout.CENTER);
        add(pnlLog, BorderLayout.SOUTH);
    }

    // --- PUBLIC METHODS (API untuk SnakeLadderGame) ---

    public VisualDice getVisualDice() { return dice; }

    public void setPlayers(Player[] p) {} // Placeholder

    public void setCurPlayer(String text) {
        lblPlayer.setText(text);
    }

    public void setStatus(String text, Color c) {
        lblDiceStatus.setText(text);
        lblDiceStatus.setForeground(c);
    }

    public void enableDice(boolean enable) {
        dice.setInteractionEnabled(enable);
        if (enable) {
            setStatus("KLIK DADU!", Color.YELLOW);
        }
    }

    public void clearLog() {
        txtLog.setText("");
    }

    public void addLog(String text) {
        txtLog.append("• " + text + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    public void updateRank(Player[] players) {
        pnlRankContent.removeAll();
        if (players != null) {
            Player[] sorted = players.clone();
            Arrays.sort(sorted); // Sort berdasarkan Score

            for (int i = 0; i < sorted.length; i++) {
                Player p = sorted[i];

                // Panel Baris Ranking
                JPanel row = new TransparentPanel(new BorderLayout());
                row.setBorder(new EmptyBorder(5, 5, 5, 5));

                // Ikon Semut & Nama
                JLabel lblName = new JLabel((i + 1) + ". 🐜 " + p.getName());
                lblName.setFont(new Font("Arial", Font.BOLD, 12));

                // Warna Emas/Perak/Perunggu untuk Top 3
                if (i == 0) lblName.setForeground(new Color(255, 215, 0)); // Gold
                else if (i == 1) lblName.setForeground(new Color(224, 224, 224)); // Silver
                else if (i == 2) lblName.setForeground(new Color(205, 127, 50)); // Bronze
                else lblName.setForeground(Color.WHITE);

                // Info Skor & Posisi
                JLabel lblInfo = new JLabel("<html><font color='#FFA500'>⭐" + p.getScore() + "</font> <font color='#AAAAAA'>| Pos:" + p.getPosition() + "</font></html>");
                lblInfo.setFont(new Font("Arial", Font.PLAIN, 11));

                row.add(lblName, BorderLayout.CENTER);
                row.add(lblInfo, BorderLayout.EAST);

                // Garis pemisah tipis
                if (i < sorted.length - 1) {
                    row.setBorder(BorderFactory.createCompoundBorder(
                            new EmptyBorder(2,0,2,0),
                            BorderFactory.createMatteBorder(0,0,1,0, new Color(255,255,255,50))
                    ));
                }

                pnlRankContent.add(row);
            }
        }
        pnlRankContent.revalidate();
        pnlRankContent.repaint();
    }

    // --- HELPER CLASSES (Inner Classes untuk styling) ---

    // Panel yang benar-benar transparan
    private static class TransparentPanel extends JPanel {
        public TransparentPanel(LayoutManager layout) { super(layout); setOpaque(false); }
        public TransparentPanel() { setOpaque(false); }
    }

    // Panel dengan sudut membulat dan warna solid
    private static class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }
    }
}