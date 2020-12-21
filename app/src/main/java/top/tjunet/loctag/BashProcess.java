package top.tjunet.loctag;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.lang.Runtime;
import java.io.IOException;

public class BashProcess {
    private Process p;
    private BufferedReader br;
    private DataOutputStream dos;

    public BashProcess(String cmd) throws IOException {
        this(new String[]{cmd});
    }

    public BashProcess(String[] cmds) throws IOException {
        p = Runtime.getRuntime().exec(cmds);
        br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        dos = new DataOutputStream(p.getOutputStream());
    }

    public void input(String cmd) throws IOException {
        dos.writeBytes(cmd);
        dos.flush();
    }

    public String getOutput(int waitForMillis) throws IOException, InterruptedException {
        Thread.sleep(waitForMillis);
        StringBuilder out = new StringBuilder();
        while (br.ready()) {
            out.append(br.readLine()).append('\n');
        }
        return out.toString();
    }

    public void exit() throws IOException {
        if (dos!=null) {
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
        }
        if (br!=null) {
            br.close();
        }
        //p.waitFor();
        p.destroy();
    }
}

