package top.tjunet.loctag.ui.csi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import top.tjunet.loctag.BashProcess;
import top.tjunet.loctag.FileHandler;
import top.tjunet.loctag.R;
import top.tjunet.loctag.jni.Csi4339;
import top.tjunet.loctag.jni.CsiConf;
import top.tjunet.loctag.jni.CsiUtils;
import top.tjunet.loctag.ui.MacList;
import top.tjunet.loctag.ui.PtsGraphView;

@SuppressLint("DefaultLocale")
public class CsiFragment extends Fragment {
    final static String TAG = "CsiFragment";
    static {
        System.loadLibrary("JNICsi");
    }
    enum State{idle, capturing};
    private CsiViewModel csiViewModel;
    private State state = State.idle;
    BashProcess bp = null;
    DatagramSocket socket = null;
    FileHandler fileHandler = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        csiViewModel =
                ViewModelProviders.of(this).get(CsiViewModel.class);
        fileHandler = new FileHandler(getContext().getExternalFilesDir(null), "csi","01/01.txt");
        /* 绑定UI控件 */
        View root = inflater.inflate(R.layout.fragment_csi, container, false);
        final TextView textView_cnt = root.findViewById(R.id.csi_text_csi);
        final PtsGraphView view_rssi = root.findViewById(R.id.csi_view);
        final TextView textView_rssi = root.findViewById(R.id.csi_textView_rssi);
        final EditText editText_file = root.findViewById(R.id.csi_editText_file);
        final TextView textView_fileStatus = root.findViewById(R.id.csi_textView_fileStatus);
        final Button button_moveLeft = root.findViewById(R.id.csi_button_moveLeft);
        final Button button_moveRight = root.findViewById(R.id.csi_button_moveRight);
        final Button button_sub = root.findViewById(R.id.csi_button_sub);
        final Button button_add = root.findViewById(R.id.csi_button_add);
        final EditText editText_srcMacs = root.findViewById(R.id.csi_editText_srcMacs);

        final Button button_tips = root.findViewById(R.id.csi_button_tips);
        final EditText editText_firstPktByte = root.findViewById(R.id.csi_editText_firstPktByte);
        final EditText editText_nssMask = root.findViewById(R.id.csi_editText_nssMask);
        final EditText editText_coreMask = root.findViewById(R.id.csi_editText_coreMask);
        final EditText editText_delay = root.findViewById(R.id.csi_editText_delay);
        final EditText editText_chanspecStr = root.findViewById(R.id.csi_editText_chanspecStr);
        final EditText editText_cnt = root.findViewById(R.id.csi_editText_cnt);

        final Button button_openRoot = root.findViewById(R.id.csi_button_openRoot);
        final Button button_startCsi = root.findViewById(R.id.csi_button_startCsi);
        /* UI更新Observer */
        csiViewModel.mCnt.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer cnt) {
                textView_cnt.setText(String.valueOf(cnt));
            }
        });
        csiViewModel.mSeq.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer rssi) {
                textView_rssi.setText(String.format("%d", rssi));
            }
        });
        csiViewModel.mState.observe(getViewLifecycleOwner(), new Observer<State>() {
            @Override
            public void onChanged(@Nullable State state) {
                if (state==State.idle)
                    button_startCsi.setText("start csi");
                else
                    button_startCsi.setText("stop csi");
                textView_fileStatus.setText(fileHandler.fileStatue());
            }
        });
        csiViewModel.mFilename.observe(getViewLifecycleOwner(), new Observer<Spanned>() {
            @Override
            public void onChanged(Spanned s) {
                editText_file.setText(s, EditText.BufferType.SPANNABLE);
            }
        });

        /* 弹出式对话框 */
        final String[] knownSrcMacs = MacList.toStringArray();
        final AlertDialog dialog_srcMacs = new AlertDialog.Builder(getContext())
                .setItems(knownSrcMacs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String srcMac = knownSrcMacs[which].substring(0, knownSrcMacs[which].indexOf('/'));
                        if (srcMac.length()<=2)
                            editText_srcMacs.setText("");
                        else
                            editText_srcMacs.setText(srcMac);
                    }
                }).create();

        /* UI控件监听 */
        textView_rssi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bp == null) {
                    Toast.makeText(getContext(), "Please root first", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    bp.input("jobs -l\n");
                    Log.d(TAG, "jobs -l >"+bp.getOutput(100));
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        button_tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_srcMacs.show();
            }
        });
        textView_fileStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = fileHandler.getFileUri();
                if (uri==null)
                    return ;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(uri, "text/plain");
                startActivity(intent);
                // com.android.htmlviewer
            }
        });
        button_moveLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileHandler.moveFileNameCursor(-1);
                csiViewModel.mFilename.setValue(fileHandler.getSpannedFilename());
            }
        });
        button_moveRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileHandler.moveFileNameCursor(1);
                csiViewModel.mFilename.setValue(fileHandler.getSpannedFilename());
            }
        });
        button_sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileHandler.updateFileName(-1);
                csiViewModel.mFilename.setValue(fileHandler.getSpannedFilename());
                textView_fileStatus.setText(fileHandler.fileStatue());
            }
        });
        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileHandler.updateFileName(1);
                csiViewModel.mFilename.setValue(fileHandler.getSpannedFilename());
                textView_fileStatus.setText(fileHandler.fileStatue());
            }
        });
        button_openRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    bp = new BashProcess("su");
                }catch (IOException e) {
                    Log.d(TAG, "open su error > "+e.toString());
                }
            }
        });
        button_startCsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bp == null) {
                    Toast.makeText(getContext(), "Please root first", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String srcMacsStr = editText_srcMacs.getText().toString();
                final String firstByteStr = editText_firstPktByte.getText().toString();
                final int nssMask = Integer.parseInt(editText_nssMask.getText().toString());
                final int coreMask = Integer.parseInt(editText_coreMask.getText().toString());
                final int delay = Integer.parseInt(editText_delay.getText().toString());
                final String chanspecStr = editText_chanspecStr.getText().toString();
                final int pkgNum = Integer.parseInt(editText_cnt.getText().toString());

                final CsiConf csiConf = new CsiConf(chanspecStr, nssMask, coreMask)
                        .setDelay(delay)
                        .setSrcMacFilter(srcMacsStr)
                        .setEnable(true);
                if (firstByteStr.matches("[0-9a-fA-F]{1,2}"))
                    csiConf.setFirstPktByteFilter((byte)Integer.parseInt(firstByteStr, 16));
                if (csiViewModel.mState.getValue() == State.idle) {
                    view_rssi.setScale(pkgNum, 100, pkgNum);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            csiViewModel.mState.postValue(State.capturing);
                            try {
                                int cnt = 0;
                                csiViewModel.mCnt.postValue(cnt);
                                view_rssi.postInvalidate();
                                Log.d(TAG, "CsiConf > "+csiConf.toString()+" "+csiConf.toBase64());
                                bp.input(String.format("nexutil -m1\n"));
                                bp.input(String.format("nexutil -Iwlan0 -s500 -b -l34 -v%s\n", csiConf.toBase64()));
                                String cmd = String.format("csiutil -m2 -f\"dst port 5500\" -l0 -t2 -d1 -n%d >/dev/null 2>&1 &\n", pkgNum);
                                Log.d(TAG, "su > "+cmd);
                                bp.input(cmd);
                                bp.input("jobs -l\n");
                                Log.d(TAG, "jobs -l >"+bp.getOutput(100));
                                if (socket==null || socket.isClosed())
                                    socket = new DatagramSocket(5502, InetAddress.getByName("127.0.0.1"));

                                fileHandler.createFile();
                                while (true) {
                                    byte[] buffer = new byte[2048];
                                    DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                                    socket.receive(response);
                                    // Log.i("debug", String.format("%d %d", response.getLength(), buffer[0]));
                                    if (buffer[0] == (byte) 0xc5) {
                                        Log.d(TAG, "receive loop exit");
                                        break;
                                    }
                                    Csi4339 obj = CsiUtils.buffToCsi4339(buffer, 0, response.getLength());
                                    csiViewModel.mCnt.postValue(++cnt);
                                    csiViewModel.mSeq.postValue(obj.seq);
                                    //view_rssi.addPoint(cnt, obj.status==0? obj.rssi+100: 0);
                                    //view_rssi.postInvalidate();
                                    fileHandler.writeText(obj.toString()+'\n');
                                    // csiViewModel.mText.postValue(obj.toPrintString());
                                    // Log.i("debug", msg);
                                    // Thread.sleep(10000);
                                }
                            } catch (Exception ex) {
                                Log.d(TAG, ex.getMessage());
                                ex.printStackTrace();
                            }
                            if (socket != null) {
                                if (!socket.isClosed()){
                                    socket.close();
                                }
                                socket = null;
                            }
                            fileHandler.closeFile();
                            csiViewModel.mState.postValue(State.idle);
                        }
                    });
                    thread.start();
                } else {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                bp.input("kill %1\n");
                                Log.d(TAG, "kill %1 > "+bp.getOutput(500));
                                if (socket != null) {
                                    if (!socket.isClosed()){
                                        socket.close();
                                    }
                                    socket = null;
                                }
                                fileHandler.closeFile();
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                            csiViewModel.mState.postValue(State.idle);
                            Log.d(TAG, "button_stopCsi finished");
                        }
                    });
                    thread.start();
                }
            }
        });
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.actionbar_menu, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionbar_reloadFirmware:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        reloadFirmware();
                    }
                }).start();
                return true;

            case R.id.actionbar_restartNic:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        restartNic();
                    }
                }).start();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void reloadFirmware() {
        if (bp == null) {
            Toast.makeText(getContext(), "Please root first", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            bp.input("mount -o rw,remount /system\n");
            bp.input("cp /sdcard/nextools/fw_bcmdhd.bin.csi /system/vendor/firmware/fw_bcmdhd.bin\n");
            bp.input("ifconfig wlan0 down\n");
            bp.input("ifconfig wlan0 up\n");
            Log.d(TAG, "reloadFirmware > "+bp.getOutput(100));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void restartNic() {
        if (bp == null) {
            Toast.makeText(getContext(), "Please root first", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            bp.input("ifconfig wlan0 down\n");
            bp.input("ifconfig wlan0 up\n");
            Log.d(TAG, "restartNic > "+bp.getOutput(100));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onDestroyView() {
        try {
            if (bp !=null)
                bp.exit();
            if (socket != null) {
                if (!socket.isClosed()){
                    socket.close();
                }
                socket = null;
            }
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
        super.onDestroyView();
    }
}