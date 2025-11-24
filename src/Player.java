import java.awt.Color;

public class Player {
    private String name;
    private int position;
    private Color color;
    private int playerNumber;

    public Player(String name, Color color, int playerNumber) {
        this.name = name;
        this.position = 1;
        this.color = color;
        this.playerNumber = playerNumber;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public Color getColor() {
        return color;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void move(int steps, boolean isGreen) {
        int oldPosition = position;

        if (isGreen) {
            position += steps;
        } else {
            position -= steps;
        }

        if (position < 1) position = 1;
        if (position > 100) position = 100;
    }
}