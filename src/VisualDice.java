import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class VisualDice extends JPanel {
    // Struktur Data Hasil
    public static class Result {
        public int number;
        public boolean isGreen;

        public Result(int number, boolean isGreen) {
            this.number = number;
            this.isGreen = isGreen;
        }
        public boolean isGreen() { return isGreen; }
        public boolean isRed() { return !isGreen; }
        public int getNumber() { return number; }
        public boolean isMultipleOfFive() { return number == 5; }
    }

    private int value = 1;
    private boolean isRolling = false;
    // Dadu selalu putih, titik selalu hitam
    private final Color diceColor = Color.WHITE;
    private final Color dotColor = Color.BLACK;

    private Timer rollTimer;
    private int animationStep = 0;
    private DiceCallback callback;
    private boolean enabled = true;
    private Random randomLogic;

    // Interface baru dengan onRollStart
    public interface DiceCallback {
        void onRollStart();    // Dipanggil saat KLIK (untuk sound)
        void onRollFinished(); // Dipanggil saat BERHENTI (untuk gerak)
    }

    public VisualDice() {
        this.randomLogic = new Random();

        setPreferredSize(new Dimension(100, 100));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (enabled && !isRolling && callback != null) {
                    callback.onRollStart(); // Panggil sound manager di sini
                    startRollAnimation();
                }
            }
        });

        // Timer animasi (setiap 80ms ganti angka)
        rollTimer = new Timer(80, e -> {
            value = randomLogic.nextInt(6) + 1; // Visual acak
            animationStep++;
            repaint();

            // Durasi diperpanjang ke 15 frame (sekitar 1.2 detik) agar pas dengan sound
            if (animationStep > 15) {
                rollTimer.stop();
                isRolling = false;
                if (callback != null) callback.onRollFinished();
            }
        });
    }

    public Result rollLogic() {
        int number = randomLogic.nextInt(6) + 1;
        boolean isGreen = randomLogic.nextDouble() < 0.8;
        return new Result(number, isGreen);
    }

    public void setCallback(DiceCallback callback) {
        this.callback = callback;
    }

    public void startRollAnimation() {
        isRolling = true;
        animationStep = 0;
        rollTimer.start();
    }

    public void setFinalResult(int number) {
        this.value = number;
        // Tidak ada perubahan warna diceColor di sini (tetap putih)
        repaint();
    }

    public void setInteractionEnabled(boolean enabled) {
        this.enabled = enabled;
        setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w, h) - 10;
        int x = (w - size) / 2;
        int y = (h - size) / 2;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillRoundRect(x + 5, y + 5, size, size, 20, 20);

        // Body Dadu (Selalu Putih/Off-white saat rolling)
        if (isRolling && animationStep % 2 == 0) {
            g2.setColor(new Color(245, 245, 245));
        } else {
            g2.setColor(diceColor);
        }
        g2.fillRoundRect(x, y, size, size, 20, 20);

        // Border
        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, size, size, 20, 20);

        // Dots
        drawDots(g2, x, y, size);

        // Teks ajakan klik
        if (enabled && !isRolling) {
            g2.setColor(new Color(0,0,0, 50));
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            String text = "KLIK";
            g2.drawString(text, x + (size - fm.stringWidth(text))/2, y + size + 15);
        }
    }

    private void drawDots(Graphics2D g2, int x, int y, int size) {
        g2.setColor(dotColor);
        int dotSize = size / 5;
        int center = size / 2 - dotSize / 2;
        int left = size / 4 - dotSize / 2;
        int right = size * 3 / 4 - dotSize / 2;

        if (value % 2 != 0) g2.fillOval(x + center, y + center, dotSize, dotSize);
        if (value > 1) { g2.fillOval(x + left, y + left, dotSize, dotSize); g2.fillOval(x + right, y + right, dotSize, dotSize); }
        if (value > 3) { g2.fillOval(x + right, y + left, dotSize, dotSize); g2.fillOval(x + left, y + right, dotSize, dotSize); }
        if (value == 6) { g2.fillOval(x + left, y + center, dotSize, dotSize); g2.fillOval(x + right, y + center, dotSize, dotSize); }
    }
}