package top.tjunet.loctag.ui.csi;

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

public class CsiFragment extends Fragment {

    private CsiViewModel csiViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        csiViewModel =
                ViewModelProviders.of(this).get(CsiViewModel.class);
        View root = inflater.inflate(R.layout.fragment_csi, container, false);
        final TextView textView = root.findViewById(R.id.text_csi);

        csiViewModel.mText.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }
}