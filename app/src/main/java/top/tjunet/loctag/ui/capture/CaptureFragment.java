package top.tjunet.loctag.ui.capture;

import android.os.Bundle;
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

import eu.chainfire.libsuperuser.Shell;
import top.tjunet.loctag.R;
import top.tjunet.loctag.jni.CsiCollect;


public class CaptureFragment extends Fragment {
    static {
        System.loadLibrary("JNICsi");
    }
    private CaptureViewModel captureViewModel;
    private static Shell.Interactive rootSession;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        captureViewModel =
                ViewModelProviders.of(this).get(CaptureViewModel.class);
        View root = inflater.inflate(R.layout.fragment_capture, container, false);
        final TextView textView = root.findViewById(R.id.text_capture);
        final Button button_ndkTest = root.findViewById(R.id.button_ndkTest);
        final Button button_root = root.findViewById(R.id.button_root);
        captureViewModel.mText.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        button_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRootShell();
            }
        });
        button_ndkTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Log.i("debug", "before openning");
                        int ret = CsiCollect.open("wlan0", "");
                        Log.i("debug", "after openning: "+ret);
                        CsiCollect.dot11RtLoop(2, new CsiCollect.JNIOnDot11RtReceivedCallback() {
                            @Override
                            public void onReceived(CsiCollect.Dot11Rt obj) {
                                Log.i("debug", String.format("%s->%s: %d %s/%s", obj.txMac, obj.rxMac, obj.rssi, obj.type, obj.subtype));
                            }
                        });
                        Log.i("debug", "after shutdown");
                        CsiCollect.shutdown();
                    }
                });
                thread.start();
            }
        });
        return root;
    }
    private void openRootShell() {
        if (rootSession != null) {
            return;
        } else {
            // start the shell in the background and keep it alive as long as the app is running
            rootSession = new Shell.Builder().
            useSU().
            setWantSTDERR(true).
            setWatchdogTimeout(10).
            setMinimalLogging(true).
            open(new Shell.OnShellOpenResultListener() {
                // Callback to report whether the shell was successfully started up
                @Override
                public void onOpenResult(boolean success, int reason) {
                    // note: this will FC if you rotate the phone while the dialog is up
                    if (!success) {
                        Log.i("Shell","Error opening root shell: exitCode " + reason);
                    } else {
                        Log.i("Shell","root shell opened");
                    }
                }
            });
        }
    };
}