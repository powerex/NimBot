
package logic;

import java.util.Scanner;

public class GameNim {

    private final static int[] easySet = {3, 4, 5};
    private final static int[] mediumSet = {5, 6, 7};
    private final static int[] hardSet = {5, 7, 8, 9};

    private Scanner scanner = new Scanner(System.in);

    private int[] stones;
    private String[] binary;
    private int[] r;
    private int[] preSet = null;

    public GameNim(int[] stones) {
        this.stones = stones;
        preSet = stones.clone();
        binary = new String[stones.length];
    }

    public int[] getStones() {
        return stones;
    }

    public int[] getPreSet() {
        return preSet;
    }

    public void setMove(int row, int number) {
        stones[row - 1] -= number;
    }

    public boolean isOver() {
        int sum = 0;
        for (int i: stones) sum += i;
        return sum == 0;
    }

    public GameNim(Level level) {
        switch(level) {
            case EASY: stones = easySet; break;
            case MEDIM: stones = mediumSet; break;
            case HARD: stones = hardSet; break;
            default: stones = new int[] {1, 2};
        }
        preSet = stones.clone();
        binary = new String[stones.length];
    }

    public void show() {
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

    private String rightove() {
        toBinary();
        int pos = isSafe();
        if (pos == -1) {
            int maxRow = 0;
            for (int i=1; i<stones.length; i++) {
                if (stones[i] > stones[maxRow]) maxRow = i;
            }
            stones[maxRow]--;
            String ss = "Take 1 from " + (maxRow+1);
            System.out.println(ss);
            toBinary();
            return ss;
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
            String ss = "Take " + number + " from " + (i+1);
            System.out.println(ss);
            toBinary();
            return ss;
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
                System.out.println("User WIN!");
                break;
            }
            game.rightove();
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

    public void userMove() {
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
}