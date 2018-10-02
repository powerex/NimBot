package logic;

public class GameStatus {
    private Level level;
    private GameStatus gameStatus;
    private int[] stones;

    public GameStatus(Level level, GameStatus gameStatus, int[] stones) {
        this.level = level;
        this.gameStatus = gameStatus;
        this.stones = stones;
    }

    public Level getLevel() {
        return level;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public int[] getStones() {
        return stones;
    }
}
