package logic;

public class Move {
    private int row;
    private int number;

    Move(int row, int number) {
        this.row = row;
        this.number = number;
    }

    public int getRow() {
        return row;
    }

    public int getNumber() {
        return number;
    }
}
