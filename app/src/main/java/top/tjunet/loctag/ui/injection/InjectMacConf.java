package top.tjunet.loctag.ui.injection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import top.tjunet.loctag.jni.Dot11Rt;

public class InjectMacConf {

    public String frameType = "";
    public String addr1 = "";
    public String addr2 = "";
    public String addr3 = "";
    public String info = "";

    private File baseDir;
    public String filename = "mgt-beacon-example.bin";

    public InjectMacConf(File baseDir, String category, String filename) {
        this.baseDir = new File(baseDir, category);
        this.filename = filename;
    }

    public String[] listFiles() {
        return baseDir.list();
    }
    public String getAbsolutePath() {
        return new File(baseDir, filename).getAbsolutePath();
    }

    public InjectMacConf loadFile(String filename) {
        this.filename = filename;
        File file = new File(baseDir, filename);
        try {
            byte[] buf = new byte[(int)file.length()];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(buf);
            dis.close();
            int type = (buf[0]>>2)&0x03;
            int subtype = (buf[0]>>4)&0x0f;
            frameType = String.format("%s/%s", Dot11Rt.frame_type[type], Dot11Rt.frame_subtype[type][subtype]);
            addr1 = String.format("%02x:%02x:%02x:%02x:%02x:%02x", buf[4], buf[4+1], buf[4+2], buf[4+3], buf[4+4], buf[4+5]);
            addr2 = String.format("%02x:%02x:%02x:%02x:%02x:%02x", buf[10], buf[10+1], buf[10+2], buf[10+3], buf[10+4], buf[10+5]);
            addr3 = String.format("%02x:%02x:%02x:%02x:%02x:%02x", buf[16], buf[16+1], buf[16+2], buf[16+3], buf[16+4], buf[16+5]);
            info = String.format("flags %02x", buf[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }
}
