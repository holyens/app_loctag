package top.tjunet.loctag.ui.capture;

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
import top.tjunet.loctag.jni.CsiUtils;
import top.tjunet.loctag.jni.Dot11Rt;
import top.tjunet.loctag.ui.MacList;
import top.tjunet.loctag.ui.PtsGraphView;

@SuppressLint("DefaultLocale")
public class CaptureFragment extends Fragment {
    final static String TAG = "CsiFragment";
    static {
        System.loadLibrary("JNICsi");
    }
    enum State{idle, capturing};
    private CaptureViewModel captureViewModel;
    private State state = State.idle;
    BashProcess bp = null;
    DatagramSocket socket = null;
    FileHandler fileHandler = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        captureViewModel =
                ViewModelProviders.of(this).get(CaptureViewModel.class);
        fileHandler = new FileHandler(getContext().getExternalFilesDir(null), "rssi","01/01.txt");
        /* 绑定UI控件 */
        View root = inflater.inflate(R.layout.fragment_capture, container, false);
        final TextView textView_cnt = root.findViewById(R.id.capture_text_capture);
        final PtsGraphView view_rssi = root.findViewById(R.id.capture_view);
        final TextView textView_rssi = root.findViewById(R.id.capture_textView_rssi);
        final EditText editText_file = root.findViewById(R.id.capture_editText_file);
        final TextView textView_fileStatus = root.findViewById(R.id.capture_textView_fileStatus);
        final Button button_moveLeft = root.findViewById(R.id.capture_button_moveLeft);
        final Button button_moveRight = root.findViewById(R.id.capture_button_moveRight);
        final Button button_sub = root.findViewById(R.id.capture_button_sub);
        final Button button_add = root.findViewById(R.id.capture_button_add);
        final EditText editText_filter = root.findViewById(R.id.capture_editText_filter);

        final Button button_tips = root.findViewById(R.id.capture_button_tips);
        final EditText editText_chanspecStr = root.findViewById(R.id.capture_editText_chanspecStr);
        final EditText editText_cnt = root.findViewById(R.id.capture_editText_cnt);

        final Button button_openRoot = root.findViewById(R.id.capture_button_openRoot);
        final Button button_startCapture = root.findViewById(R.id.capture_button_startCapture);
        /* UI更新Observer */
        captureViewModel.mCnt.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer cnt) {
                textView_cnt.setText(String.valueOf(cnt));
            }
        });
        captureViewModel.mRssi.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer rssi) {
                textView_rssi.setText(String.format("%d dBm", rssi));
            }
        });
        captureViewModel.mState.observe(getViewLifecycleOwner(), new Observer<State>() {
            @Override
            public void onChanged(@Nullable State state) {
                if (state==State.idle)
                    button_startCapture.setText("start capture");
                else
                    button_startCapture.setText("stop capture");
                textView_fileStatus.setText(fileHandler.fileStatue());
            }
        });
        captureViewModel.mFilename.observe(getViewLifecycleOwner(), new Observer<Spanned>() {
            @Override
            public void onChanged(Spanned s) {
                editText_file.setText(s, EditText.BufferType.SPANNABLE);
            }
        });

        /* 弹出式对话框 */
        final String[] filter_exps = MacList.toStringArray();
        final AlertDialog dialog_filter = new AlertDialog.Builder(getContext())
                .setItems(filter_exps, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String srcMac = filter_exps[which].substring(0, filter_exps[which].indexOf('/'));
                        if (srcMac.length()<=2)
                            editText_filter.setText("");
                        else
                            editText_filter.setText("ether src "+srcMac);
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
                    dialog_filter.show();
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
            }
        });
        button_moveLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileHandler.moveFileNameCursor(-1);
                captureViewModel.mFilename.setValue(fileHandler.getSpannedFilename());
            }
        });
        button_moveRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileHandler.moveFileNameCursor(1);
                captureViewModel.mFilename.setValue(fileHandler.getSpannedFilename());
            }
        });
        button_sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileHandler.updateFileName(-1);
                captureViewModel.mFilename.setValue(fileHandler.getSpannedFilename());
                textView_fileStatus.setText(fileHandler.fileStatue());
            }
        });
        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileHandler.updateFileName(1);
                captureViewModel.mFilename.setValue(fileHandler.getSpannedFilename());
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
        button_startCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bp == null) {
                    Toast.makeText(getContext(), "Please root first", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String chanspecStr = editText_chanspecStr.getText().toString();
                final int pkgNum = Integer.parseInt(editText_cnt.getText().toString());
                final String filter = editText_filter.getText().toString();

                if (captureViewModel.mState.getValue() == State.idle) {
                    view_rssi.setScale(pkgNum, 100, pkgNum);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            captureViewModel.mState.postValue(State.capturing);
                            try {
                                int cnt = 0;
                                captureViewModel.mCnt.postValue(cnt);
                                view_rssi.postInvalidate();
                                bp.input(String.format("nexutil -m2 -k%s\n", chanspecStr));
                                String cmd = String.format("LD_PRELOAD=libfakeioctl.so csiutil -m2 -f\"%s\" -l0 -t1 -d1 -n%d >/dev/null 2>&1 &\n", filter, pkgNum);
                                Log.i(TAG, "su > "+cmd);
                                bp.input(cmd);
                                bp.input("jobs -l\n");
                                Log.d(TAG, "jobs -l >"+bp.getOutput(100));
                                if (socket==null || socket.isClosed())
                                    socket = new DatagramSocket(5502, InetAddress.getByName("127.0.0.1"));

                                fileHandler.createFile();
                                while (true) {
                                    byte[] buffer = new byte[1024];
                                    DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                                    socket.receive(response);
                                    // Log.i("debug", String.format("%d %d", response.getLength(), buffer[0]));
                                    if (buffer[0] == (byte) 0xc5) {
                                        Log.i(TAG, "receive loop exit");
                                        break;
                                    }
                                    Dot11Rt obj = CsiUtils.buffToDot11Rt(buffer, 0, response.getLength());
                                    captureViewModel.mCnt.postValue(++cnt);
                                    captureViewModel.mRssi.postValue(obj.rssi);
                                    view_rssi.addPoint(cnt, obj.status==0? obj.rssi+100: 0);
                                    view_rssi.postInvalidate();
                                    fileHandler.writeText(obj.toString()+'\n');

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
                            captureViewModel.mState.postValue(State.idle);
                        }
                    });
                    thread.start();
                } else {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                bp.input("kill %1\n");
                                Log.i(TAG, "kill %1 > "+bp.getOutput(500));
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
                            captureViewModel.mState.postValue(State.idle);
                            Log.i(TAG, "button_stopCapture finished");
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
            bp.input("cp /sdcard/nextools/fw_bcmdhd.bin.nex /system/vendor/firmware/fw_bcmdhd.bin\n");
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