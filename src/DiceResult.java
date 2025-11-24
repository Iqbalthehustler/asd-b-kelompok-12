public class DiceResult {
    private int number;
    private boolean isGreen;

    public DiceResult(int number, boolean isGreen) {
        this.number = number;
        this.isGreen = isGreen;
    }

    public int getNumber() {
        return number;
    }

    public boolean isGreen() {
        return isGreen;
    }

    public boolean isRed() {
        return !isGreen;
    }

    // Method baru: cek apakah angka dadu kelipatan 5
    public boolean isMultipleOfFive() {
        return number % 5 == 0;
    }

    @Override
    public String toString() {
        String color = isGreen ? "Hijau" : "Merah";
        String multipleInfo = isMultipleOfFive() ? " (DOUBLE TURN!)" : "";
        return String.format("Dadu: %d (%s)%s", number, color, multipleInfo);
    }
}