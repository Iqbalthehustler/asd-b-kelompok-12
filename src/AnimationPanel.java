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
    private int animationSpeed;
    private int baseSpeed = 100;

    public AnimationPanel() {
        setOpaque(false);
        this.animationSpeed = baseSpeed;
        animationTimer = new Timer(animationSpeed, new AnimationListener());
    }

    public void animateMovement(Player player, int startPos, int endPos, Runnable onComplete) {
        this.animatedPlayer = player;
        this.startPosition = startPos;
        this.endPosition = endPos;
        this.onAnimationComplete = onComplete;
        this.currentStep = 0;
        this.totalSteps = Math.min(20, Math.abs(endPos - startPos) * 2);

        animationTimer.setDelay(animationSpeed);
        animationTimer.start();
    }

    public void setAnimationSpeed(int speed) {
        this.animationSpeed = speed;
        if (animationTimer.isRunning()) {
            animationTimer.setDelay(animationSpeed);
        }
    }

    public void increaseSpeed() {
        if (animationSpeed > 20) {
            animationSpeed -= 20;
            if (animationTimer.isRunning()) {
                animationTimer.setDelay(animationSpeed);
            }
        }
    }

    public void decreaseSpeed() {
        if (animationSpeed < 500) {
            animationSpeed += 20;
            if (animationTimer.isRunning()) {
                animationTimer.setDelay(animationSpeed);
            }
        }
    }

    public void resetSpeed() {
        this.animationSpeed = baseSpeed;
        if (animationTimer.isRunning()) {
            animationTimer.setDelay(animationSpeed);
        }
    }

    public int getAnimationSpeed() {
        return animationSpeed;
    }

    public String getSpeedLabel() {
        if (animationSpeed <= 40) return "Sangat Cepat";
        if (animationSpeed <= 80) return "Cepat";
        if (animationSpeed <= 120) return "Normal";
        if (animationSpeed <= 200) return "Lambat";
        return "Sangat Lambat";
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