package top.tjunet.loctag.ui;

import java.util.HashMap;
import java.util.Map;

public class MacList {
    public static Map<String, String> macMap = new HashMap<>();
    static {
        macMap.put("AR9580-TX", "B4:EE:B4:B7:0B:3C");
        macMap.put("AC86U-eth5", "D4:5D:64:79:6A:58");
        macMap.put("nexus5", "AA:BB:CC:11:22:33");
        macMap.put("intel5300", "00:16:ea:12:34:56");
    }

    public static String[] toStringArray() {
        String[] sa = new String[macMap.size()+1];
        int i=0;
        for (Map.Entry<String, String> entry : macMap.entrySet())
            sa[i++] = entry.getValue()+"/"+entry.getKey();
        sa[i] = ""+"/None";
        return sa;
    }
}
