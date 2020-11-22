package top.tjunet.loctag.ui.capture;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import top.tjunet.loctag.R;

public class CaptureFragment extends Fragment {

    static {
        System.loadLibrary("JNIPcap");
    }
    private CaptureViewModel captureViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        captureViewModel =
                ViewModelProviders.of(this).get(CaptureViewModel.class);
        View root = inflater.inflate(R.layout.fragment_capture, container, false);
        final TextView textView = root.findViewById(R.id.text_capture);
        final Button button_ndkTest = root.findViewById(R.id.button_ndkTest);

        captureViewModel.mText.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        button_ndkTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x = ndkDoSometing(2);
                captureViewModel.mText.setValue(String.valueOf(x));
                Log.i("debug", String.format("%d", 2));
            }
        });
        return root;
    }

    public static native int ndkDoSometing(int x);
}