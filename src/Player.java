import java.awt.Color;
import java.io.Serializable;

public class Player implements Comparable<Player>, Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private int position; // Rentang 0 - 25
    private Color color;
    private int playerNumber;
    private int score; // Fitur Baru: Skor Bintang

    public Player(String name, Color color, int playerNumber) {
        this.name = name;
        this.position = 0;
        this.color = color;
        this.playerNumber = playerNumber;
        this.score = 0;
    }

    public String getName() { return name; }
    public int getPosition() { return position; }
    public Color getColor() { return color; }
    public int getPlayerNumber() { return playerNumber; }
    public int getScore() { return score; }

    public void setPosition(int position) {
        this.position = position;
    }

    public void addScore(int amount) {
        this.score += amount;
    }

    @Override
    public int compareTo(Player other) {
        // Sorting default berdasarkan Skor Tertinggi (untuk Priority Queue)
        return Integer.compare(other.score, this.score);
    }
}