package top.tjunet.loctag.jni;

public class CsiUtils {
    // public enum Type {DOT11, DOT11_RT, CSI};

    public static native Dot11Rt buffToDot11Rt(byte[] buf, int offset, int len);
    public static native Csi4339 buffToCsi4339(byte[] buf, int offset, int len);
    public static native int strToChanspec(String Str);
    public static native String chanspecToStr(int chanspec);

    public static String csiConfToCmd(CsiConf csiConf) {
        return "nexutil -Iwlan0 -s500 -b -l34 -v" + csiConf.toBase64();
    }

    public static int chan2freq(int chan) {
        if (chan>=1 && chan<=13)
            return 2407 + chan*5;
        else if (chan>=34 && chan<=165)
            return 5000 + chan*5;
        else
            return 0;
    }

    public static int freq2chan(int freq) {
        if (freq>=2412 && freq<=2472)
            return (freq-2407)/5;
        else if (freq>=5170 && freq<=5825)
            return (freq-5000)/5;
        else
            return 0;
    }
}
