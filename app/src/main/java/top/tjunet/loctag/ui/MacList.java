package top.tjunet.loctag.ui;

import java.util.HashMap;
import java.util.Map;

public class MacList {
    public static Map<String, String> macMap = new HashMap<>();
    static {
        macMap.put("ac86-eth5", "D4:5D:64:79:6A:58");
        macMap.put("ac86-eth6", "D4:5D:64:79:6A:5C");
        macMap.put("nexus5", "AA:BB:CC:11:22:33");
        macMap.put("intel5300", "00:16:ea:12:34:56");
        macMap.put("AR9160-m", "00:15:6d:84:ee:a0");
        macMap.put("AR9160-c", "00:15:6D:84:EE:C1");
        macMap.put("k2p-2.4", "FC:7C:02:EF:41:C9");
        macMap.put("k2p-5", "FC:7C:02:FF:41:C9");
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
