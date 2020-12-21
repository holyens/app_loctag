package top.tjunet.loctag.jni;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

public class CsiConf {

    public String chanspecStr = "1/20";
    public boolean csiCollect = false;
    public int nssMask = 0x01;
    public int coreMask = 0x01;
    public boolean usePktFilter = false;
    public byte firstPktByte = 0x00;
    public String[] cmpSrcMacs = new String[]{};
    public int delay = 0;

    /**
     * make csi params
     *
     * @param  chanspecStr
     *         Channel specification <channel>/<bandwidth>
     *
     * @param  nssMask
     *         bitmask with spatial streams to capture
     *         (e.g., 0x7 = 0b0111 capture first 3 ss)
     *
     * @param  coreMask
     *         bitmask with cores where to activate capture
     *         (e.g., 0x5 = 0b0101 set core 0 and 2)
     */
    public CsiConf(String chanspecStr, int nssMask, int coreMask) {
        this.chanspecStr = chanspecStr;
        this.nssMask = nssMask;
        this.coreMask = coreMask;
        // default
    }
    @NonNull
    @SuppressLint("DefaultLocale")
    public String toString() {
        return String.format("%s,%b,[%d,%d],%b#%02x,{%s},%d", chanspecStr, csiCollect, nssMask, coreMask, usePktFilter, firstPktByte, TextUtils.join(",",cmpSrcMacs), delay);
    }
    /**
     * make csi params
     *
     * @param  csiCollect
     *         enable/disable CSI collection (false = disable, default is true)
     */
    public CsiConf setEnable(boolean csiCollect) {
        this.csiCollect = csiCollect;
        return this;
    }

    public CsiConf setFirstPktByteFilter(byte firstPktByte) {
        this.firstPktByte = firstPktByte;
        usePktFilter = true;
        return this;
    }

    public CsiConf setSrcMacFilter(String srcMacsStr) {
        cmpSrcMacs = srcMacsStr.matches("\\W*")? new String[0] : srcMacsStr.split("\\|");
        return this;
    }

    public CsiConf setSrcMacFilter(String[] srcMacs) {
        cmpSrcMacs = srcMacs;
        return this;
    }

    public CsiConf setDelay(int delay) {
        this.delay = delay;
        return this;
    }


    public String toBase64() {
        byte[] buff = new byte[34];
        int chanspec = CsiUtils.strToChanspec(chanspecStr);
        buff[0] = (byte)(chanspec & 0xff);
        buff[1] = (byte)((chanspec>>8) & 0xff);
        buff[2] = (byte)(csiCollect? 1:0);
        buff[3] = (byte)(((nssMask&0x0f)<<4)|(coreMask&0x0f));
        buff[4] = (byte)(usePktFilter? 1:0);
        buff[5] = firstPktByte;
        buff[6] = (byte)(cmpSrcMacs.length);
        buff[7] = 0;
        try {
            for (int i=0; i<cmpSrcMacs.length; i++) {
                String[] macByteStrs = cmpSrcMacs[i].split(":");
                if (macByteStrs.length !=6)
                    return null;
                for (int j=0; j<6; j++) {
                    buff[8+i*6+j] =  (byte)Integer.parseInt(macByteStrs[j], 16);
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }
        buff[32] = (byte)(delay & 0xff);
        buff[33] = (byte)((delay>>8) & 0xff);

        return Base64.encodeToString(buff, Base64.DEFAULT);
    }

}
