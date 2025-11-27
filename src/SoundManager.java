import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private Map<String, Clip> soundClips;
    private boolean soundEnabled;

    public SoundManager() {
        soundClips = new HashMap<>();
        soundEnabled = true;
        loadSounds();
    }

    private void loadSounds() {
        try {
            // Load default sounds (akan diganti dengan file audio Anda)
            loadSound("dice_roll", "sounds/dice_roll.wav");
            loadSound("player_move", "sounds/player_move.wav");
            loadSound("power_up", "sounds/power_up.wav");
            loadSound("special_node", "sounds/special_node.wav");
            loadSound("win", "sounds/win.wav");
        } catch (Exception e) {
            System.out.println("Warning: Could not load sound files. Using fallback sounds.");
            createFallbackSounds();
        }
    }

    private void loadSound(String name, String filePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                    new File(filePath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            soundClips.put(name, clip);
        } catch (Exception e) {
            System.out.println("Could not load sound: " + filePath);
        }
    }

    private void createFallbackSounds() {
        // Create simple beep sounds as fallback
        try {
            soundClips.put("dice_roll", createBeepClip(400, 100));
            soundClips.put("player_move", createBeepClip(300, 50));
            soundClips.put("power_up", createBeepClip(600, 200));
            soundClips.put("special_node", createBeepClip(800, 150));
            soundClips.put("win", createBeepClip(1000, 500));
        } catch (Exception e) {
            System.out.println("Could not create fallback sounds");
        }
    }

    private Clip createBeepClip(int frequency, int duration) throws LineUnavailableException {
        Clip clip = AudioSystem.getClip();
        AudioFormat audioFormat = new AudioFormat(44100, 8, 1, true, false);
        byte[] buffer = new byte[44100 * duration / 1000];

        for (int i = 0; i < buffer.length; i++) {
            double angle = i / (44100.0 / frequency) * 2.0 * Math.PI;
            buffer[i] = (byte)(Math.sin(angle) * 127.0);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        AudioInputStream audioInputStream = new AudioInputStream(bais, audioFormat, buffer.length);
        clip.open(audioInputStream);
        return clip;
    }

    public void playSound(String soundName) {
        if (!soundEnabled) return;

        Clip clip = soundClips.get(soundName);
        if (clip != null) {
            // Stop sound jika sedang diputar
            if (clip.isRunning()) {
                clip.stop();
            }
            // Reset ke awal
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void playSoundLoop(String soundName, int loopCount) {
        if (!soundEnabled) return;

        Clip clip = soundClips.get(soundName);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.loop(loopCount);
        }
    }

    public void stopSound(String soundName) {
        Clip clip = soundClips.get(soundName);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) {
            // Stop semua sound yang sedang diputar
            for (Clip clip : soundClips.values()) {
                if (clip.isRunning()) {
                    clip.stop();
                }
            }
        }
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}