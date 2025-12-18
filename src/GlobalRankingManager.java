import java.io.*;
import java.util.*;

public class GlobalRankingManager {
    private static final String FILE_ACTIVE = "game_records.dat";
    private static final String FILE_ARCHIVE = "game_archives.dat";

    private List<GameRecord> activeRecords;
    private List<GameRecord> archivedRecords;

    public GlobalRankingManager() {
        this.activeRecords = loadRecords(FILE_ACTIVE);
        this.archivedRecords = loadRecords(FILE_ARCHIVE);
    }

    @SuppressWarnings("unchecked")
    private List<GameRecord> loadRecords(String fileName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (List<GameRecord>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void saveRecords(List<GameRecord> records, String fileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(records);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void addRecord(GameRecord record) {
        this.activeRecords.add(record);
        saveRecords(activeRecords, FILE_ACTIVE);
    }

    public List<GameRecord> getActiveRecords() {
        return Collections.unmodifiableList(activeRecords);
    }

    // --- FITUR RESET & ARSIP ---
    public void resetAndArchive() {
        if (activeRecords.isEmpty()) return;

        // Pindahkan ke arsip
        archivedRecords.addAll(activeRecords);
        activeRecords.clear();

        // Simpan perubahan
        saveRecords(activeRecords, FILE_ACTIVE);    // Jadi kosong
        saveRecords(archivedRecords, FILE_ARCHIVE); // Bertambah
    }

    // --- STATISTIK (Support Mode Arsip) ---
    private List<GameRecord> getTargetList(boolean useArchive) {
        return useArchive ? archivedRecords : activeRecords;
    }

    public List<String> getTop3ScoresViaPQ(boolean useArchive) {
        List<GameRecord> source = getTargetList(useArchive);
        if (source.isEmpty()) return Collections.singletonList("Belum ada data.");

        PriorityQueue<PlayerSnapshot> pq = new PriorityQueue<>(
                (p1, p2) -> Integer.compare(p2.getScore(), p1.getScore())
        );

        for (GameRecord r : source) pq.addAll(r.getFinalRankings());

        List<String> result = new ArrayList<>();
        int count = 0;
        while (!pq.isEmpty() && count < 3) {
            PlayerSnapshot p = pq.poll();
            result.add((count + 1) + ". " + p.getName() + " - ⭐ " + p.getScore());
            count++;
        }
        return result.isEmpty() ? Collections.singletonList("Belum ada data.") : result;
    }

    public List<String> getTop3WinnersViaPQ(boolean useArchive) {
        List<GameRecord> source = getTargetList(useArchive);
        if (source.isEmpty()) return Collections.singletonList("Belum ada data.");

        Map<String, Integer> winMap = new HashMap<>();
        for (GameRecord r : source) {
            winMap.put(r.getWinnerName(), winMap.getOrDefault(r.getWinnerName(), 0) + 1);
        }

        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
                (e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())
        );
        pq.addAll(winMap.entrySet());

        List<String> result = new ArrayList<>();
        int count = 0;
        while (!pq.isEmpty() && count < 3) {
            Map.Entry<String, Integer> e = pq.poll();
            result.add((count + 1) + ". " + e.getKey() + " (" + e.getValue() + "x Menang)");
            count++;
        }
        return result.isEmpty() ? Collections.singletonList("Belum ada data.") : result;
    }
}