package top.tjunet.loctag.jni;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
@SuppressLint("DefaultLocale")
public class Csi4339 {
    public int status;
    public int timestamp;
    public String srcMac;
    public int seq;
    public int chanspec;
    public int chipVer;
    public int coreNum;
    public int ssNum;
    public Complex[] csi;

//    // ssNum, coreNum, sub carrier
//    public int[] csiArrayShape() {
//        int[] size = {0 ,0, 0};
//        try {
//            size = new int[]{csi.length, csi[0].length, csi[0][0].length};
//        } catch (IndexOutOfBoundsException e) {
//            e.printStackTrace();
//        }
//        return size;
//    }

    @NonNull
    public String toString() {
        return String.format("%s,%d,[%d,%d],%x,%d", srcMac, seq, coreNum, ssNum, chanspec, csi.length);
    }

    public String toPrintString() {
        return String.format("%s,%d,[%d,%d],%x,%d", srcMac, seq, coreNum, ssNum, chanspec, csi.length);
    }
}
