package top.tjunet.loctag.jni;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

@SuppressLint("DefaultLocale")
public class Dot11Rt {
    final public static String[] frame_type= {
        "mgt", "ctl", "data", "ext"
    };
    final public static String[][] frame_subtype = {
        {
            "Association Request",
            "Association Response",
            "Reassociation Request",
            "Reassociation Response",
            "Probe Request",
            "Probe Response",
            "Timing Advertisement",
            "Reserved",
            "Beacon",
            "ATIM",
            "Disassociation",
            "Authentication",
            "Deauthentication",
            "Action",
            "Action No Ack",
            "Reserved"
        },
        {
            "Reserved",
            "Reserved",
            "Reserved",
            "Reserved",
            "Beamforming Report Poll",
            "VHT NDP Announcement",
            "Control Frame Extension",
            "Control Wrapper",
            "Block Ack Request (BlockAckReq)",
            "Block Ack (BlockAck)",
            "PS-Poll",
            "RTS",
            "CTS",
            "Ack",
            "CF-End",
            "CF-End +CF-Ack"
        },
        {
            "Data",
            "Data+CF-Ack",
            "Data+CF-Poll",
            "Data+CF-Ack +CF-Poll",
            "Null(no data)",
            "CF-Ack(no data)",
            "CF-Poll(no data)",
            "CF-Ack+CF-Poll(no data)",
            "QoS Data",
            "QoS Data+CF-Ack",
            "QoS Data+CF-Poll",
            "QoS Data+CF-Ack +CF-Poll",
            "QoS Null(no data)",
            "Reserved",
            "QoS CF-Poll(no data)",
            "QoS CF-Ack+CF-Poll (no data)"
        },
        {
            "DMG Beacon"
        }
    };

    public int status;
    public String txMac;
    public String rxMac;
    public double rate;
    public int cFreq;
    public int rssi;
    public int noise;
    public int type;
    public int subtype;
    public String SSID;

    public boolean isBeacon() {
        return status==0 && type == 0 && subtype == 8;
    }
    @NonNull
    public String toString() {
        return String.format("%d,%s,%s,%.1f,%d,%d,%d,%d,%d,%s", status, txMac, rxMac, rate, cFreq, rssi, noise, type, subtype, SSID);
    }

    public String toTagString() {
        return String.format("%s,%.1f,%d,%d,%d,%d,%d,%s", txMac, rate, cFreq, rssi, SSID);
    }

    public String toPrintString() {
        return String.format("%s->%s: %d %s/%s", txMac, rxMac, rssi, frame_type[type], frame_subtype[type][subtype]);
    }

}
