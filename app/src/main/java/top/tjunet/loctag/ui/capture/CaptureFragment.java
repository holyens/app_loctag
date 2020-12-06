package top.tjunet.loctag.ui.capture;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import top.tjunet.loctag.R;
import top.tjunet.loctag.jni.CsiCollect;


public class CaptureFragment extends Fragment {
    static {
        System.loadLibrary("android_shm");
    }
    private CaptureViewModel captureViewModel;
    int shmFd = -1;
    Process p = null;
    BufferedReader reader = null;
    DataOutputStream dos = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        captureViewModel =
                ViewModelProviders.of(this).get(CaptureViewModel.class);
        View root = inflater.inflate(R.layout.fragment_capture, container, false);
        final TextView textView = root.findViewById(R.id.text_capture);
        final Button button_openM2 = root.findViewById(R.id.button_openM2);
        final Button button_startCapture = root.findViewById(R.id.button_startCapture);
        final Button button_stopCapture = root.findViewById(R.id.button_stopCapture);
        captureViewModel.mText.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        button_openM2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    p = Runtime.getRuntime().exec("su");
//                    reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                    dos = new DataOutputStream(p.getOutputStream());
//                    dos.writeBytes("export LD_PRELOAD=libfakeioctl.so\n");
//                    dos.writeBytes("echo $LD_PRELOAD\n");
//                    Log.i("debug", reader.readLine());
//                    dos.writeBytes("nexutil -m2 -k1/20\n");
                    //            int read;
                    //            char[] buffer = new char[4096];
                    //            StringBuffer output = new StringBuffer();
                    //
                    //            while ((read = reader.read(buffer)) > 0) {
                    //                output.append(buffer, 0, read);
                    //            }
                    //
                    //            reader.close();
                    //            chmod.waitFor();
                    //            outputString =  output.toString();
                    shmFd = CsiCollect.createShmFromJni();
                    Log.i("debug", String.valueOf(shmFd));
                    // Thread.sleep(3*60*1000);
                } catch (Exception e) {
                    Log.i("debug", e.toString());
                }
            }
        });
        button_startCapture.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            //dos.writeBytes(String.format("nohup shmtest -f %d &\n", shmFd));
                            ParcelFileDescriptor cfd = ParcelFileDescriptor.fromFd(shmFd);
                            android.os.Parcel data = android.os.Parcel.obtain();
                            android.os.Parcel reply = android.os.Parcel.obtain();
                            //data.writeParcelable(pfd, 0);
                            // 或者

                            FileDescriptor fileDescriptor2 = cfd.getFileDescriptor();
                            data.writeFileDescriptor(fileDescriptor2);
                            mBinder.transact(0, data, reply, 0);

                            Log.i("debug",  CsiCollect.getDot11RtPacket(shmFd));
                            Thread.sleep(3*60*1000);
                            //Log.i("debug", String.format("%s->%s: %d %s/%s", obj.txMac, obj.rxMac, obj.rssi, obj.type, obj.subtype));
                            //Log.i("debug", reader.readLine());
                        } catch (IOException| InterruptedException e) {
                            Log.i("debug", e.toString());
                        }
                    }
                });
                thread.start();
            }
        });
        button_stopCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dos.writeBytes("exit\n");
                            dos.close();
                            reader.close();
                            p.waitFor();
                            shmFd = CsiCollect.closeShmFromJni(shmFd);
                        } catch (IOException|InterruptedException e) {
                            Log.i("debug", e.toString());
                        }
                    }
                });
                thread.start();
            }
        });
        return root;
    }
}