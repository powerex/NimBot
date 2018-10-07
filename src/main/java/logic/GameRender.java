package logic;

import model.Icon;

public class GameRender {

    private static Icon icon = new Icon();

    public static String getRender(GameNim game) {
        int[] stones = game.getStones();
        StringBuilder sb = new StringBuilder();

        for (int n: stones) {
            if (n != 0) {
                for (int i=0; i<n; i++) {
                    sb.append(icon.getIcon("doughnut"));
                }
            } else {
                sb.append(icon.getIcon("lock"));            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
