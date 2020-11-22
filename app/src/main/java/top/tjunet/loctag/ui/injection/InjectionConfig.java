package top.tjunet.loctag.ui.injection;

import android.annotation.SuppressLint;
@SuppressLint("DefaultLocale")
public class InjectionConfig {

    public String frame = "data";
    public String file = "/sdcard/mpdu-data.hex";
    public String srcMac = "00:11:22:33:44:55";
    public String dstMac = "00:11:22:33:44:55";

    public int channel = 1;
    public int bandwidth = 20;

    public String phy = "dsss";
    public double rate = 1;
    public int mcs = 0;

    public int delay = 10;
    public int period = 1000;
    public int number = 10;

    public InjectionConfig() {

    }

}
