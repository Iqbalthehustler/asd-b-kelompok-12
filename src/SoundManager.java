import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    // --- Daftar File Suara ---
    // Pastikan 4 file ini ada di folder proyek (sejajar dengan folder src atau bin)
    private final String BGM_SETUP = "Opening.wav";
    private final String BGM_INGAME = "Backsound.wav";
    private final String SFX_DICE = "Dadu.WAV";
    private final String SFX_WIN = "Yippie.WAV";

    // --- Pengaturan Volume (Desibel) ---
    private static final float VOL_BGM = -10.0f; // Musik latar agak pelan
    private static final float VOL_SFX = 0.0f;   // Efek suara normal

    private Clip currentBgmClip; // Menyimpan musik latar yang sedang aktif

    public SoundManager() {
    }

    // --- Helper: Load File Audio ---
    private Clip loadClip(String filePath) {
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.err.println("❌ File audio tidak ditemukan: " + filePath);
                return null;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;
        } catch (Exception e) {
            System.err.println("❌ Error memuat audio " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    // --- Helper: Set Volume ---
    private void setVolume(Clip clip, float volumeDb) {
        if (clip == null) return;
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float max = gainControl.getMaximum();
            float min = gainControl.getMinimum();

            // Batasi agar tidak error
            if (volumeDb > max) volumeDb = max;
            if (volumeDb < min) volumeDb = min;

            gainControl.setValue(volumeDb);
        } catch (IllegalArgumentException e) {
            // Kontrol volume tidak didukung sistem
        }
    }

    // --- Logika BGM (Looping) ---

    // 1. Musik saat Setup Pemain
    public void playSetupMusic() {
        playBgm(BGM_SETUP);
    }

    // 2. Musik saat Game Berlangsung
    public void playInGameMusic() {
        playBgm(BGM_INGAME);
    }

    private void playBgm(String filePath) {
        stopBgm(); // Matikan musik sebelumnya jika ada

        currentBgmClip = loadClip(filePath);
        if (currentBgmClip != null) {
            setVolume(currentBgmClip, VOL_BGM);
            currentBgmClip.setFramePosition(0);
            currentBgmClip.loop(Clip.LOOP_CONTINUOUSLY); // Ulang terus
            currentBgmClip.start();
        }
    }

    public void stopBgm() {
        if (currentBgmClip != null) {
            if (currentBgmClip.isRunning()) currentBgmClip.stop();
            currentBgmClip.close();
            currentBgmClip = null;
        }
    }

    // --- Logika SFX (Sekali Main) ---

    public void playDiceRoll() {
        playSoundEffect(SFX_DICE);
    }

    public void playWinSound() {
        stopBgm(); // Matikan musik latar agar suara menang terdengar jelas
        playSoundEffect(SFX_WIN);
    }

    private void playSoundEffect(String filePath) {
        Clip clip = loadClip(filePath);
        if (clip != null) {
            setVolume(clip, VOL_SFX);
            clip.setFramePosition(0);
            clip.start();

            // Hapus clip dari memori setelah selesai main
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        }
    }
}