import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class AnimationPanel extends JPanel {
    private Timer animationTimer;
    private int currentStep;
    private int totalSteps;
    private int startPosition;
    private int endPosition;
    private Player animatedPlayer;
    private Runnable onAnimationComplete;

    // Kecepatan tetap (Normal)
    private final int ANIMATION_SPEED = 80;

    public AnimationPanel() {
        setOpaque(false);
        animationTimer = new Timer(ANIMATION_SPEED, new AnimationListener());
    }

    public void animateMovement(Player player, int startPos, int endPos, Runnable onComplete) {
        this.animatedPlayer = player;
        this.startPosition = startPos;
        this.endPosition = endPos;
        this.onAnimationComplete = onComplete;
        this.currentStep = 0;
        this.totalSteps = Math.min(20, Math.abs(endPos - startPos) * 2);

        animationTimer.start();
    }

    private class AnimationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            currentStep++;
            if (currentStep >= totalSteps) {
                animationTimer.stop();
                animatedPlayer.setPosition(endPosition);
                if (onAnimationComplete != null) {
                    onAnimationComplete.run();
                }
            } else {
                double progress = (double) currentStep / totalSteps;
                double easedProgress = 1 - Math.pow(1 - progress, 1.5);
                int intermediatePos = startPosition +
                        (int) ((endPosition - startPosition) * easedProgress);
                animatedPlayer.setPosition(intermediatePos);
            }
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    public boolean isAnimating() {
        return animationTimer.isRunning();
    }
}