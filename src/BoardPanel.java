import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class BoardPanel extends JPanel {
    public static final int TOTAL_STEPS = 25;
    private Point[] path;
    private Map<Integer, Integer> starScores;
    private Set<Integer> specialNodes;
    private Map<Integer, List<Integer>> specialGraph;

    private Player[] players;
    private String statusMsg = "Ant Adventure Begins!";
    private AnimationPanel animPanel;
    private ControlPanel ctrlPanel;

    // --- Caching untuk Performa ---
    // Kita menggambar background berat (rumput/tanah) ke gambar ini sekali saja
    private BufferedImage backgroundCache;
    private int lastWidth = -1, lastHeight = -1;

    // Aset Dekorasi
    private List<Point> rocks;
    private List<Point> roots;

    public BoardPanel() {
        setLayout(new OverlayLayout(this));
        animPanel = new AnimationPanel();
        add(animPanel);

        starScores = new HashMap<>();
        specialNodes = new HashSet<>();
        specialGraph = new HashMap<>();
        rocks = new ArrayList<>();
        roots = new ArrayList<>();

        initPath25();
        generateContent();
        setPreferredSize(new Dimension(800, 600)); // Resolusi sedikit dinaikkan
    }

    public void setControlPanel(ControlPanel cp) { this.ctrlPanel = cp; }
    public void setPlayers(Player[] p) { this.players = p; }
    public void setStatusMessage(String m) { this.statusMsg = m; repaint(); }

    private void initPath25() {
        path = new Point[TOTAL_STEPS + 1];
        // Koordinat manual
        path[0] = new Point(60, 550);
        path[1] = new Point(160, 570);
        path[2] = new Point(260, 550);
        path[3] = new Point(360, 570);
        path[4] = new Point(460, 550);
        path[5] = new Point(560, 520);

        path[6] = new Point(620, 450);
        path[7] = new Point(520, 420);
        path[8] = new Point(420, 450);
        path[9] = new Point(320, 420);
        path[10] = new Point(220, 450);
        path[11] = new Point(120, 420);

        path[12] = new Point(80, 320);
        path[13] = new Point(180, 290);
        path[14] = new Point(280, 320);
        path[15] = new Point(380, 290);
        path[16] = new Point(480, 320);
        path[17] = new Point(580, 280);

        path[18] = new Point(620, 200);
        path[19] = new Point(520, 170);
        path[20] = new Point(420, 200);
        path[21] = new Point(320, 170);
        path[22] = new Point(220, 150);

        path[23] = new Point(130, 110); // Dekat permukaan
        path[24] = new Point(220, 75);  // Di Rumput
        path[25] = new Point(400, 50);  // Di Langit (Finish)
    }

    public void generateContent() {
        Random r = new Random();
        starScores.clear();
        for(int i=1; i<TOTAL_STEPS; i++) {
            if(r.nextBoolean()) starScores.put(i, (r.nextInt(5)+1)*10);
        }
        specialNodes.clear();
        specialGraph.clear();
        while(specialNodes.size() < 3) specialNodes.add(r.nextInt(TOTAL_STEPS-4)+2);

        List<Integer> nodes = new ArrayList<>(specialNodes);
        for(Integer n : nodes) {
            List<Integer> neighbors = new ArrayList<>(nodes);
            neighbors.remove(n);
            specialGraph.put(n, neighbors);
        }

        // Generate Posisi Dekorasi Batu & Akar
        rocks.clear();
        roots.clear();
        for(int i=0; i<20; i++) rocks.add(new Point(r.nextInt(750), r.nextInt(400) + 150));
        for(int i=0; i<12; i++) roots.add(new Point(r.nextInt(750), 110));

        // Reset cache agar digambar ulang
        backgroundCache = null;
        repaint();
    }

    // Helpers
    public Point getPos(int i) { if(i<0) i=0; if(i>TOTAL_STEPS) i=TOTAL_STEPS; return path[i]; }
    public int getScore(int i) { return starScores.getOrDefault(i, 0); }
    public void takeScore(int i) { starScores.remove(i); }
    public Set<Integer> getSpecialNodes() { return specialNodes; }
    public Map<Integer, List<Integer>> getSpecialGraph() { return specialGraph; }
    public Integer getNearestSpecial(int pos) { return specialNodes.isEmpty() ? null : specialNodes.iterator().next(); }
    public Integer getRandomSpecial(int pos) {
        if(specialNodes.isEmpty()) return null;
        return new ArrayList<>(specialNodes).get(new Random().nextInt(specialNodes.size()));
    }
    public boolean isPrime(int n) {
        int[] p = {2,3,5,7,11,13,17,19,23};
        for(int x:p) if(x==n) return true;
        return false;
    }
    public void resetSpecialNodes() { generateContent(); }
    public void animate(Player p, int s, int e, Runnable cb) { animPanel.animateMovement(p, s, e, cb); }
    public boolean isAnimating() { return animPanel.isAnimating(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. GAMBAR BACKGROUND (Cached)
        // Jika ukuran berubah atau cache kosong, generate ulang background
        if (backgroundCache == null || getWidth() != lastWidth || getHeight() != lastHeight) {
            generateRealisticBackground(getWidth(), getHeight());
            lastWidth = getWidth();
            lastHeight = getHeight();
        }
        g2.drawImage(backgroundCache, 0, 0, null);

        // 2. GAMBAR TEROWONGAN (TUNNELS)
        drawTunnels(g2);

        // 3. GAMBAR RUANGAN (NODES)
        drawNodes(g2);

        // 4. GAMBAR PEMAIN
        drawPlayers(g2);

        // 5. STATUS BAR
        drawStatusBar(g2);
    }

    // --- PROCEDURAL GENERATION ENGINE ---
    private void generateRealisticBackground(int w, int h) {
        backgroundCache = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = backgroundCache.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int surfaceY = 110;

        // A. LANGIT (Realistic Sky Gradient)
        GradientPaint sky = new GradientPaint(0, 0, new Color(70, 130, 180), 0, surfaceY, new Color(173, 216, 230));
        g2.setPaint(sky);
        g2.fillRect(0, 0, w, surfaceY);

        // B. MATAHARI (Glowing Radial)
        Point sunPos = new Point(w - 80, 50);
        float[] dist = {0.0f, 0.4f, 1.0f};
        Color[] colors = {new Color(255, 255, 200), new Color(255, 215, 0, 100), new Color(255, 215, 0, 0)};
        RadialGradientPaint sunGlow = new RadialGradientPaint(sunPos, 60, dist, colors);
        g2.setPaint(sunGlow);
        g2.fillOval(sunPos.x - 60, sunPos.y - 60, 120, 120);
        g2.setColor(Color.WHITE);
        g2.fillOval(sunPos.x - 15, sunPos.y - 15, 30, 30); // Inti matahari

        // C. AWAN (Fluffy Clouds with Alpha)
        drawRealisticCloud(g2, 50, 40, 60);
        drawRealisticCloud(g2, 200, 20, 80);
        drawRealisticCloud(g2, 450, 60, 50);

        // D. TANAH (Detailed Soil Gradient & Strata)
        GradientPaint soilBase = new GradientPaint(0, surfaceY, new Color(101, 67, 33), 0, h, new Color(45, 30, 15));
        g2.setPaint(soilBase);
        g2.fillRect(0, surfaceY, w, h - surfaceY);

        // Lapisan Strata (Garis-garis horizontal samar)
        g2.setColor(new Color(0, 0, 0, 20));
        for (int i = surfaceY; i < h; i += 40) {
            g2.fillRect(0, i, w, 15);
        }

        // E. TEKSTUR TANAH (Noise - Ribuan titik)
        Random rnd = new Random();
        for (int i = 0; i < 5000; i++) {
            int tx = rnd.nextInt(w);
            int ty = rnd.nextInt(h - surfaceY) + surfaceY;
            // Variasi warna butiran tanah
            int tone = rnd.nextInt(3);
            if (tone == 0) g2.setColor(new Color(139, 69, 19, 100)); // Darker
            else if (tone == 1) g2.setColor(new Color(210, 180, 140, 80)); // Lighter
            else g2.setColor(new Color(60, 40, 20, 100)); // Stone color
            g2.fillOval(tx, ty, 3, 3);
        }

        // F. BATU & AKAR (Dekorasi)
        g2.setColor(new Color(80, 70, 60));
        for (Point p : rocks) {
            g2.fillOval(p.x, p.y, rnd.nextInt(15) + 15, rnd.nextInt(10) + 10);
        }
        g2.setColor(new Color(90, 60, 30));
        g2.setStroke(new BasicStroke(2));
        for (Point p : roots) {
            int len = rnd.nextInt(40) + 20;
            g2.drawArc(p.x, surfaceY, 20, len, 180, 180); // Akar menggantung
        }

        // G. RUMPUT REALISTIS (Blade by Blade)
        for (int x = 0; x < w; x += 2) {
            int grassH = rnd.nextInt(15) + 5;
            // Variasi warna hijau
            int gVal = rnd.nextInt(50) + 100;
            g2.setColor(new Color(34, gVal, 34));
            g2.drawLine(x, surfaceY, x, surfaceY - grassH);
        }

        g2.dispose();
    }

    // Helper menggambar awan yang lembut
    private void drawRealisticCloud(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(255, 255, 255, 180));
        g.fillOval(x, y, size, size);
        g.fillOval(x + size / 2, y - size / 4, size, size);
        g.fillOval(x + size, y, size, size);
        g.fillOval(x + size / 2, y + size / 4, size, size);
    }

    private void drawTunnels(Graphics2D g2) {
        // Outline Terowongan (Gelap)
        g2.setStroke(new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(30, 15, 5));
        for(int i=0; i<TOTAL_STEPS; i++) g2.drawLine(path[i].x, path[i].y, path[i+1].x, path[i+1].y);

        // Isi Terowongan (Terang - Efek 3D)
        g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(160, 110, 80));
        for(int i=0; i<TOTAL_STEPS; i++) g2.drawLine(path[i].x, path[i].y, path[i+1].x, path[i+1].y);

        g2.setStroke(new BasicStroke(1));
    }

    private void drawNodes(Graphics2D g2) {
        for(int i=0; i<=TOTAL_STEPS; i++) {
            Point p = path[i];

            // Efek 3D Sphere untuk Node
            int r = 26;
            Color baseColor;
            if (i == 0) baseColor = Color.GREEN;
            else if (i == TOTAL_STEPS) baseColor = Color.RED;
            else if (specialNodes.contains(i)) baseColor = new Color(148, 0, 211);
            else baseColor = new Color(220, 190, 150);

            // Radial Gradient untuk efek bola
            Point center = new Point(p.x - 5, p.y - 5);
            float[] dist = {0.0f, 1.0f};
            Color[] colors = {baseColor.brighter(), baseColor.darker()};
            RadialGradientPaint sphere = new RadialGradientPaint(center, r, dist, colors);
            g2.setPaint(sphere);
            g2.fillOval(p.x - r/2, p.y - r/2, r, r);

            // Border
            g2.setColor(new Color(50, 30, 10));
            g2.drawOval(p.x - r/2, p.y - r/2, r, r);

            // Text
            g2.setColor(i==TOTAL_STEPS || specialNodes.contains(i) ? Color.WHITE : Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            String lbl = (i==0)?"S":(i==TOTAL_STEPS)?"F":String.valueOf(i);
            int tw = g2.getFontMetrics().stringWidth(lbl);
            g2.drawString(lbl, p.x - tw/2, p.y + 4);

            // Star / Gem
            if(starScores.containsKey(i)) {
                drawStarShape(g2, p.x+10, p.y-12, 6, Color.CYAN);
            }
        }
    }

    private void drawStarShape(Graphics2D g, int x, int y, int size, Color c) {
        int[] xP = new int[10];
        int[] yP = new int[10];
        double angle = Math.PI / 2; // Mulai dari atas
        for(int i=0; i<10; i++) {
            int rad = (i%2==0) ? size : size/2;
            xP[i] = x + (int)(Math.cos(angle) * rad);
            yP[i] = y - (int)(Math.sin(angle) * rad);
            angle += Math.PI / 5;
        }
        g.setColor(c);
        g.fillPolygon(xP, yP, 10);
        g.setColor(Color.WHITE);
        g.drawPolygon(xP, yP, 10);
    }

    private void drawPlayers(Graphics2D g2) {
        if(players != null) {
            for(Player p : players) {
                Point loc = getPos(p.getPosition());
                int offX = (p.getPlayerNumber() % 2 == 0) ? -6 : 6;
                int offY = (p.getPlayerNumber() > 1) ? -6 : 6;

                // Semut dengan badan terpisah (Head, Thorax, Abdomen)
                g2.setColor(p.getColor());
                // Abdomen (Belakang)
                g2.fillOval(loc.x + offX - 8, loc.y + offY - 5, 8, 8);
                // Thorax (Tengah)
                g2.fillOval(loc.x + offX - 4, loc.y + offY - 4, 6, 6);
                // Head (Depan)
                g2.fillOval(loc.x + offX, loc.y + offY - 6, 7, 7);

                // Mata Putih
                g2.setColor(Color.WHITE);
                g2.fillOval(loc.x + offX + 2, loc.y + offY - 5, 2, 2);
            }
        }
    }

    private void drawStatusBar(Graphics2D g2) {
        // Kotak status transparan semi-modern
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(10, 10, 320, 35, 15, 15);
        g2.setColor(new Color(255, 215, 0)); // Gold text
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g2.drawString(statusMsg, 25, 33);
    }
}