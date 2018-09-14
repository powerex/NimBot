package model;

import java.util.HashMap;
import java.util.Map;

public class Icon {

    private Map<String, String> icons;

    public Icon() {
        icons = new HashMap<String, String>();
        icons.put("doughnut", "🍩");
        icons.put("lock", "\uD83D\uDD12");
        icons.put("unlock", "\uD83D\uDD13");
    }

    public String getIcon(String name) {
        return icons.get(name);
    }
}
