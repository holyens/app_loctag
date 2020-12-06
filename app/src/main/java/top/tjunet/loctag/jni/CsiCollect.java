package top.tjunet.loctag.jni;

public class CsiCollect {
    public enum Type {DOT11, DOT11_RT, CSI};
    public class Dot11Rt {
        public int status;
        public String txMac;
        public String rxMac;
        public float rate;
        public int cFreq;
        public int rssi;
        public int noise;
        public String type;
        public String subtype;
        public String SSID;
    }
    public interface JNIOnDot11RtReceivedCallback {
        void onReceived(Dot11Rt obj);
    }
    public static native int createShmFromJni();
    public static native String getDot11RtPacket(int fd);
    public static native int closeShmFromJni(int fd);

    public static native int open(String devName, String filterExp);
    public static native int dot11RtLoop(int num, JNIOnDot11RtReceivedCallback jniOnDot11RtReceivedCallback);
    public static native int shutdown();
}
