
package logic;

import java.util.*;

public class GameNim {

    private final static int[] easySet = {3, 4, 5};
    private final static int[] mediumSet = {5, 6, 7};
    private final static int[] hardSet = {5, 7, 8, 9};
    private Status status;

    private Scanner scanner = new Scanner(System.in);

    private int[] stones;
    private String[] binary;
    private int[] r;
    private int[] preSet;

    private GameNim(int[] stones) {
        this.stones = stones;
        preSet = stones.clone();
        binary = new String[stones.length];
        status = Status.PLAYING;
    }

    public Status getStatus() {
        return status;
    }

/*    public int[] getRowIndexes() {
        List<Integer> list = new LinkedList<>();
        for (int i=0; i<stones.length; i++) {
            if (stones[i] != 0) list.add(i+1);
        }
        int[] array = new int[list.size()];
        list.toArray(new int[][]{array});
        return array;
    }*/

/*    public GameStatus getGameStatus() {
        return new GameStatus(this.level, this.status, this.stones);
    }*/

    public int[] getStones() {
        return stones;
    }

    public void setMove(int row, int number) {
        stones[row - 1] -= number;
        if (isOver()) status = Status.WIN;
    }

    public boolean isOver() {
        int sum = 0;
        for (int i: stones) sum += i;
        return sum == 0;
    }

    public GameNim(Level level) {
        switch(level) {
            case EASY: stones = Arrays.copyOf(easySet, easySet.length);
                break;
            case MEDIUM: stones = Arrays.copyOf(mediumSet, mediumSet.length);
                break;
            case HARD: stones = Arrays.copyOf(hardSet, hardSet.length);
                break;
            default: stones = new int[] {1, 2};
        }
        preSet = stones.clone();
        binary = new String[stones.length];
    }

    private void show() {
        for (int i=0; i<preSet.length; i++) {
            for (int j=0; j<preSet[i]; j++)
                if (j < stones[i])
                    System.out.print("*");
                else
                    System.out.print(".");
            System.out.println();
        }
        toBinary();
        for (int i=0; i<stones.length; i++) {
            System.out.println(binary[i]);
        }
    }

    private int isSafe() {
        int max = getMaxDigit();
        int pos = 0;
        while (pos < max && r[pos]%2 == 0) pos++;
        if (pos >= max) pos = -1;
        return pos;
    }

    public Move rightMove() {
        toBinary();
        int pos = isSafe();
        if (pos == -1) {
            int maxRow = 0;
            for (int i=1; i<stones.length; i++) {
                if (stones[i] > stones[maxRow]) maxRow = i;
            }
            stones[maxRow]--;
//            String ss = "Take 1 from " + (maxRow+1);
//            System.out.println(ss);
            toBinary();
            return new Move(maxRow+1, 1);
        } else {
            int i = 0;
            while (binary[i].charAt(pos) == '0') i++;
            StringBuilder mod = new StringBuilder(binary[i]);
            mod.setCharAt(pos, '0');
            pos++;
            for (;pos < getMaxDigit(); pos++)
                if (r[pos]%2 != 0)
                {
                    if (binary[i].charAt(pos) == '0')
                        mod.setCharAt(pos, '1');
                    else
                        mod.setCharAt(pos, '0');
                }
            binary[i] = mod.toString();
//            System.out.println("New binary: " + binary[i]);
            int newNumber = fromString(binary[i]);
            int number = stones[i] - newNumber;
            stones[i] = newNumber;
            toBinary();
            if (isOver()) status = Status.LOSE;
            return new Move(i+1, number);
        }
    }

    private int fromString(String binary) {
        int res = 0;
        int t = binary.length()-1;
        for (int i=t; i>=0; i--) {
            if (binary.charAt(t - i) == '1')
                res += (1 << i);
        }
        return res;
    }

    public static void main(String[] args) {
//        Game game = new Game(Level.EASY);
//        Game game = new Game(new int[] {10, 15, 20, 25});
        GameNim game = new GameNim(new int[] {1, 2});
        game.show();
        while (true) {
            game.userMove();
            if (game.isOver()) {
                System.out.println("User WIN!\nOne more time? /new");
                break;
            }
            game.rightMove();
            if (game.isOver()) {
                System.out.println("Bot WIN!");
                break;
            }
        }
    }

    private void toBinary() {
        int m = getMaxDigit();
        r = new int[m];
        for (int i=0; i<m; i++) r[i] = 0;

        for (int i=0; i<stones.length; i++) {
            binary[i] = "";
            binary[i] = Integer.toBinaryString(stones[i]);
            while (binary[i].length() < m)
                binary[i] = '0' + binary[i];

            //System.out.println(binary[i]);

            for (int j=0; j<m; j++) {
                if (binary[i].charAt(j) == '1') r[j]++;
            }

        }
    }

    private int getDigit(int n) {
        int d = 0;
        while (n != 0) {
            n >>= 1;
            d++;
        }
        return d;
    }

    private void userMove() {
        System.out.println("Enter row: ");
        int row = scanner.nextInt();
        System.out.println("Enter number: ");
        int number = scanner.nextInt();

        stones[row - 1] -= number;
        show();
    }

    private int getMaxDigit() {
        int max = 0;
        for (int i: stones) {
            if (getDigit(i) > max)
                max = getDigit(i);
        }
        return max;
    }

    public void cancelLastMove() {
    }

    public void stop() {
    }
}