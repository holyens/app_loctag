package top.tjunet.loctag.ui.injection;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import top.tjunet.loctag.jni.CsiUtils;

@SuppressLint("DefaultLocale")
public class InjectPhyConf {
    public final static int RATES_RATE_MASK = 0x000000FF;

    public final static int RATES_VHT_MCS_MASK = 0x0000000F;
    public final static int RATES_VHT_MCS_SHIFT = 0;
    public final static int RATES_VHT_NSS_MASK = 0x000000F0;
    public final static int RATES_VHT_NSS_SHIFT = 4;

    public final static int RATES_HT_MCS_MASK = 0x00000007;
    public final static int RATES_HT_MCS_SHIFT = 0;
    public final static int RATES_HT_NSS_MASK = 0x00000078;
    public final static int RATES_HT_NSS_SHIFT = 3;

    public final static int RATES_TXEXP_MASK = 0x00000300;
    public final static int RATES_TXEXP_SHIFT = 8;
    public final static int RATES_BW_MASK = 0x00070000;
    public final static int RATES_BW_SHIFT = 16;
    public final static int RATES_STBC = 0x00100000;
    public final static int RATES_TXBF = 0x00200000;
    public final static int RATES_LDPC_CODING = 0x00400000;
    public final static int RATES_SHORT_GI = 0x00800000;
    public final static int RATES_SHORT_PREAMBLE = 0x00800000;
    public final static int RATES_ENCODING_MASK = 0x03000000;
    public final static int RATES_OVERRIDE_RATE = 0x40000000;
    public final static int RATES_OVERRIDE_MODE = 0x80000000;
    public final static int RATES_ENCODE_RATE = 0x00000000;
    public final static int RATES_ENCODE_HT = 0x01000000;
    public final static int RATES_ENCODE_VHT = 0x02000000;
    public final static int BW_20MHZ = 1;
    public final static int BW_40MHZ = 2;
    public final static int BW_80MHZ = 3;
    public final static int BW_160MHZ = 4;

    public final static int RATES_BW_UNSPECIFIED = 0x00000000;
    public final static int RATES_BW_20MHZ = (BW_20MHZ << RATES_BW_SHIFT);
    public final static int RATES_BW_40MHZ = (BW_40MHZ << RATES_BW_SHIFT);
    public final static int RATES_BW_80MHZ = (BW_80MHZ << RATES_BW_SHIFT);
    public final static int RATES_BW_160MHZ = (BW_160MHZ << RATES_BW_SHIFT);



    public String chanspecStr = "1/20";
    public int ratespec = 0xc1000000; // HT-MCS-0

    public int delay = 10;
    public int period = 1000;
    public int number = 10;

    public InjectPhyConf setInject(int delay, int period, int number) {
        this.delay = delay;
        this.period = period;
        this.number = number;
        return this;
    }

    public InjectPhyConf setHtMcs(int mcs, int nss) {
        ratespec = 0;
        ratespec |= (mcs<<RATES_HT_MCS_SHIFT)&RATES_HT_MCS_MASK;
        // ratespec |= (nss<<RATES_HT_NSS_SHIFT)&RATES_HT_NSS_MASK;
        ratespec |= RATES_OVERRIDE_RATE | RATES_OVERRIDE_MODE | RATES_ENCODE_HT;
        return this;
    }
    public InjectPhyConf setVhtMcs(int mcs, int nss) {
        ratespec = 0;
        ratespec |= (mcs<<RATES_VHT_MCS_SHIFT)&RATES_VHT_MCS_MASK;
        ratespec |= (1<<RATES_VHT_NSS_SHIFT)&RATES_VHT_NSS_MASK;
        ratespec |= RATES_OVERRIDE_RATE | RATES_OVERRIDE_MODE | RATES_ENCODE_VHT;
        return this;
    }
    public InjectPhyConf setLegacyRate(int rateIn500kbpsUnits ) {
        ratespec = rateIn500kbpsUnits & RATES_RATE_MASK;
        return this;
    }
    @NonNull
    public String toString() {
        return String.format("%s (%04x) %08x, %d %d %d", chanspecStr, CsiUtils.strToChanspec(chanspecStr), ratespec, delay, period, number);
    }
}
