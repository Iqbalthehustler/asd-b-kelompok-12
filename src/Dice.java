import java.util.Random;

public class Dice {
    private Random random;

    public Dice() {
        this.random = new Random();
    }

    public DiceResult roll() {
        int number = random.nextInt(6) + 1;
        boolean isGreen = random.nextDouble() < 0.8;

        return new DiceResult(number, isGreen);
    }
}