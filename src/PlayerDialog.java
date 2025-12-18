import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerDialog {
    // --- PALET WARNA TEMA TANAH ---
    private static final Color BG_DARK_SOIL = new Color(45, 30, 15);   // Tanah Gelap
    private static final Color BG_LIGHT_SOIL = new Color(101, 67, 33); // Tanah Terang
    private static final Color TEXT_GOLD = new Color(255, 215, 0);     // Emas
    private static final Color TEXT_WHITE = new Color(240, 240, 240);
    private static final Color INPUT_SAND = new Color(245, 222, 179);  // Warna Pasir (utk Input)
    private static final Color BTN_GREEN = new Color(34, 139, 34);     // Rumput
    private static final Color BTN_RED = new Color(178, 34, 34);       // Merah Bata

    private static String[] resultContainer = null;

    public static String[] showPlayerDialog(JFrame parent, GlobalRankingManager mgr) {
        resultContainer = null;

        JDialog dialog = new JDialog(parent, "🐜 Setup Petualangan Semut", true);
        dialog.setSize(900, 650);

        // Custom Background Panel (Gradasi Tanah)
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // Gradasi dari Coklat Terang (Atas) ke Gelap (Bawah)
                GradientPaint gp = new GradientPaint(0, 0, BG_LIGHT_SOIL, 0, getHeight(), BG_DARK_SOIL);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        dialog.setContentPane(mainPanel);

        // --- 1. Panel Input (Kiri) ---
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.WEST);

        // --- 2. Panel Ranking & Log (Tengah) ---
        JPanel rankWrapper = new JPanel(new BorderLayout());
        rankWrapper.setOpaque(false); // Transparan agar background tanah terlihat

        // Panel List Ranking
        JPanel rankDisplay = createRankingDisplay(mgr.getActiveRecords());
        rankWrapper.add(rankDisplay, BorderLayout.CENTER);

        // Tombol Log (Reset & History)
        JPanel logBtns = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logBtns.setOpaque(false);

        JButton btnReset = createStyledButton("🗑️ Reset Log", new Color(205, 92, 92)); // Indian Red
        JButton btnHist = createStyledButton("📜 Buka Arsip Kuno", new Color(100, 149, 237)); // Cornflower Blue

        logBtns.add(btnReset);
        logBtns.add(btnHist);
        rankWrapper.add(logBtns, BorderLayout.SOUTH);

        mainPanel.add(rankWrapper, BorderLayout.CENTER);

        // --- 3. Panel Start/Cancel (Bawah) ---
        JPanel botBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botBtns.setOpaque(false);

        JButton btnOk = createStyledButton("MULAI PETUALANGAN", BTN_GREEN);
        btnOk.setFont(new Font("Arial", Font.BOLD, 16));

        JButton btnCancel = createStyledButton("BATAL", BTN_RED);

        botBtns.add(btnOk);
        botBtns.add(btnCancel);
        mainPanel.add(botBtns, BorderLayout.SOUTH);

        // --- Logic Tombol ---
        btnReset.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(dialog,
                    "Kubur kenangan lama ke arsip?\nLog aktif akan menjadi kosong.",
                    "Reset Log", JOptionPane.YES_NO_OPTION);
            if(c == JOptionPane.YES_OPTION) {
                mgr.resetAndArchive();
                rankWrapper.remove(rankDisplay);
                JPanel newP = createRankingDisplay(mgr.getActiveRecords());
                rankWrapper.add(newP, BorderLayout.CENTER);
                rankWrapper.revalidate(); rankWrapper.repaint();
                JOptionPane.showMessageDialog(dialog, "Log berhasil dikubur ke arsip!");
            }
        });

        btnHist.addActionListener(e -> {
            new RecordDisplayDialog((JFrame) SwingUtilities.getWindowAncestor(dialog), mgr, true).setVisible(true);
        });

        setupInputLogic(inputPanel, btnOk, btnCancel, dialog);

        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return resultContainer;
    }

    // --- HELPER: INPUT PANEL ---
    private static JPanel createInputPanel() {
        // Panel Transparan dengan Border
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(139, 69, 19), 2), // Saddle Brown border
                new EmptyBorder(20, 20, 20, 20)
        ));
        p.setPreferredSize(new Dimension(380, 500));

        // Background semi-transparan hitam untuk kontras
        JPanel bgBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 80)); // Hitam transparan
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        bgBox.setOpaque(false);
        p.add(bgBox, BorderLayout.CENTER);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        bgBox.add(content, BorderLayout.CENTER);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 5, 8, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Judul
        JLabel t = new JLabel("SETUP SARANG", SwingConstants.CENTER);
        t.setFont(new Font("Showcard Gothic", Font.PLAIN, 24));
        t.setForeground(TEXT_GOLD);
        g.gridx=0; g.gridy=0; g.gridwidth=2; content.add(t,g);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(139, 69, 19));
        g.gridy=1; content.add(sep, g);

        // Pilih Pemain
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ctrl.setOpaque(false);
        JLabel lblJml = new JLabel("Jumlah Semut:");
        lblJml.setForeground(TEXT_WHITE);
        lblJml.setFont(new Font("Arial", Font.BOLD, 14));

        JComboBox<Integer> cb = new JComboBox<>(new Integer[]{2,3,4});
        cb.setBackground(INPUT_SAND);

        ctrl.add(lblJml); ctrl.add(cb);
        g.gridy=2; content.add(ctrl, g);

        // Label Nama
        JLabel lblNama = new JLabel("Nama Pasukan:");
        lblNama.setForeground(TEXT_WHITE);
        lblNama.setFont(new Font("Arial", Font.BOLD, 14));
        g.gridy=3; content.add(lblNama, g);

        // Grid Input
        JPanel gd = new JPanel(new GridLayout(4, 2, 10, 15));
        gd.setOpaque(false);
        for(int i=0; i<4; i++) {
            JLabel l = new JLabel("P"+(i+1)+":");
            l.setForeground(TEXT_WHITE);
            l.setFont(new Font("Arial", Font.BOLD, 12));
            gd.add(l);

            JTextField tf = new JTextField("Player "+(i+1));
            tf.setBackground(INPUT_SAND);
            tf.setForeground(Color.BLACK);
            tf.setFont(new Font("Arial", Font.BOLD, 12));
            tf.setBorder(BorderFactory.createLineBorder(new Color(139, 69, 19), 1));

            if(i>=2) {
                tf.setEnabled(false);
                tf.setBackground(new Color(100, 80, 70)); // Darker disabled
            }
            gd.add(tf);
        }
        g.gridy=4; g.weighty=1.0; g.fill=GridBagConstraints.BOTH; content.add(gd, g);

        return p;
    }

    // --- HELPER: RANKING DISPLAY ---
    private static JPanel createRankingDisplay(List<GameRecord> list) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        // Border Titled Keren
        TitledBorder tb = BorderFactory.createTitledBorder(
                new LineBorder(TEXT_GOLD, 2),
                "✨ LAST 3 ADVENTURES ✨",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                TEXT_GOLD
        );
        p.setBorder(tb);

        if(list==null || list.isEmpty()){
            JLabel l = new JLabel("Belum ada jejak semut...", SwingConstants.CENTER);
            l.setForeground(Color.LIGHT_GRAY);
            p.add(l);
            return p;
        }

        List<GameRecord> r = new ArrayList<>(list.subList(Math.max(0,list.size()-3), list.size()));
        Collections.reverse(r);

        JPanel cards = new JPanel();
        cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));
        cards.setOpaque(false);
        cards.setBorder(new EmptyBorder(10,10,10,10));

        for(GameRecord rec : r) {
            JPanel c = new JPanel(new BorderLayout());
            c.setOpaque(false);
            // Background Card seperti batu lempeng
            c.setBorder(new EmptyBorder(5,5,5,5) {
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    g.setColor(new Color(245, 222, 179)); // Pasir
                    g.fillRoundRect(x, y, width, height, 15, 15);
                    g.setColor(new Color(139, 69, 19)); // Garis tepi coklat
                    g.drawRoundRect(x, y, width-1, height-1, 15, 15);
                }
            });

            // Isi Card
            JLabel time = new JLabel("⏰ " + rec.getFormattedTimestamp());
            time.setForeground(new Color(101, 67, 33));
            time.setFont(new Font("Arial", Font.ITALIC, 10));
            c.add(time, BorderLayout.NORTH);

            JLabel win = new JLabel("🏆 " + rec.getWinnerName(), SwingConstants.CENTER);
            win.setForeground(new Color(178, 34, 34)); // Merah bata
            win.setFont(new Font("Arial", Font.BOLD, 14));
            c.add(win, BorderLayout.CENTER);

            String d = rec.getFinalRankings().stream()
                    .map(s->s.getName()+"(⭐"+s.getScore()+")")
                    .collect(Collectors.joining(", "));

            JTextArea ta = new JTextArea(d);
            ta.setLineWrap(true); ta.setWrapStyleWord(true);
            ta.setEditable(false); ta.setOpaque(false);
            ta.setFont(new Font("Arial", Font.PLAIN, 11));
            ta.setForeground(Color.DARK_GRAY);
            c.add(ta, BorderLayout.SOUTH);

            cards.add(c);
            cards.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(cards);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        p.add(scroll);
        return p;
    }

    // --- HELPER: LOGIC INPUT (SAFE) ---
    private static void setupInputLogic(JPanel p, JButton ok, JButton can, JDialog d) {
        // Mencari komponen secara aman (seperti fix sebelumnya)
        // Struktur sudah fix di createInputPanel, tapi kita cari dinamis agar aman

        // 1. Cari ComboBox
        JComboBox<Integer> cb = null;
        // ComboBox ada di dalam 'bgBox' -> 'content' -> Panel urutan ke-2 (index 2 di gridbag)
        // Kita traverse rekursif atau cari berdasarkan tipe
        List<Component> allComps = getAllComponents(p);
        for(Component c : allComps) {
            if(c instanceof JComboBox) { cb = (JComboBox<Integer>)c; break; }
        }

        // 2. Cari TextFields
        List<JTextField> tfsList = new ArrayList<>();
        for(Component c : allComps) {
            if(c instanceof JTextField) tfsList.add((JTextField)c);
        }
        final JTextField[] tfs = tfsList.toArray(new JTextField[0]);

        if(cb != null && tfs.length == 4) {
            final JComboBox<Integer> finalCb = cb;

            ok.addActionListener(e -> {
                int n = (int)finalCb.getSelectedItem();
                String[] res = new String[n];
                for(int k=0; k<n; k++) {
                    String val = tfs[k].getText().trim();
                    res[k] = val.isEmpty() ? "Semut "+(k+1) : val;
                }
                resultContainer = res;
                d.dispose();
            });

            finalCb.addActionListener(e -> {
                int n = (int)finalCb.getSelectedItem();
                for(int k=0; k<4; k++) {
                    tfs[k].setEnabled(k<n);
                    tfs[k].setBackground(k<n ? INPUT_SAND : new Color(100, 80, 70));
                }
            });
        }
        can.addActionListener(e -> d.dispose());
    }

    // Helper rekursif untuk mencari komponen dalam panel berlapis
    private static List<Component> getAllComponents(final Container c) {
        Component[] comps = c.getComponents();
        List<Component> compList = new ArrayList<>();
        for (Component comp : comps) {
            compList.add(comp);
            if (comp instanceof Container) {
                compList.addAll(getAllComponents((Container) comp));
            }
        }
        return compList;
    }

    // Helper untuk membuat Tombol Bergaya
    private static JButton createStyledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 1),
                new EmptyBorder(8, 15, 8, 15)
        ));
        return b;
    }
}