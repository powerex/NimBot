package logic;

public class GameStatus {
    private Level level;
    private Status Status;
    private int[] stones;

    public GameStatus(Level level, Status status, int[] stones) {
        this.level = level;
        this.Status = status;
        this.stones = stones;
    }

    public Level getLevel() {
        return level;
    }

    public GameStatus getGameStatus() {
        return this;
    }

    public Status getStatus() {
        return Status;
    }

    public int[] getStones() {
        return stones;
    }
}
