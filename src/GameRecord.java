import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Color;

public class GameRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private final LocalDateTime timestamp;
    private final List<PlayerSnapshot> finalRankings;
    private final String winnerName;

    public GameRecord(List<Player> finalPlayers, String winner) {
        this.timestamp = LocalDateTime.now();
        this.winnerName = winner;
        // Simpan snapshot score juga
        this.finalRankings = finalPlayers.stream()
                .map(p -> new PlayerSnapshot(p.getName(), p.getPosition(), p.getColor(), p.getScore()))
                .collect(Collectors.toList());
    }

    public List<PlayerSnapshot> getFinalRankings() { return finalRankings; }
    public String getWinnerName() { return winnerName; }
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}

class PlayerSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final int position;
    private final int colorRGB;
    private final int score; // Field baru

    public PlayerSnapshot(String name, int position, Color color, int score) {
        this.name = name;
        this.position = position;
        this.colorRGB = color.getRGB();
        this.score = score;
    }

    public String getName() { return name; }
    public int getPosition() { return position; }
    public int getScore() { return score; } // Getter score
    public Color getColor() { return new Color(colorRGB); }
}