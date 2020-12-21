package top.tjunet.loctag.ui.injection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import top.tjunet.loctag.BashProcess;
import top.tjunet.loctag.R;

public class InjectionFragment extends Fragment {
    final static String TAG = "InjectionFragment";
    static {
        System.loadLibrary("JNICsi");
    }
    enum State{idle, injecting};
    private InjectionViewModel injectionViewModel;
    private State state = State.idle;
    Timer timer;
    BashProcess bp = null;
    InjectMacConf imc = null;
    InjectPhyConf ipc = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        injectionViewModel =
                ViewModelProviders.of(this).get(InjectionViewModel.class);
        imc = new InjectMacConf(new File("/sdcard"), "dot11pkts", "");
        ipc = new InjectPhyConf().setInject(0, 1000, 1).setHtMcs(1, 0);
        View root = inflater.inflate(R.layout.fragment_injection, container, false);
        /* 绑定UI控件 */
        final TextView textView_log = root.findViewById(R.id.injection_textView_log);

        final EditText editText_file = root.findViewById(R.id.injection_editText_file);
        final TextView textView_browser = root.findViewById(R.id.injection_textView_browser);
        final TextView textView_frameType = root.findViewById(R.id.injection_textView_frameType);
        final TextView textView_addr1 = root.findViewById(R.id.injection_textView_addr1);
        final TextView textView_addr2 = root.findViewById(R.id.injection_textView_addr2);
        final TextView textView_addr3 = root.findViewById(R.id.injection_textView_addr3);
        final TextView textView_macInfo = root.findViewById(R.id.injection_textView_macInfo);

        final EditText editText_chanspecStr = root.findViewById(R.id.injection_editText_chanspecStr);
        final Spinner spinner_phy = root.findViewById(R.id.injection_spinner_phy);
        final TextView textView_legacyRate = root.findViewById(R.id.injection_textView_legacyRate);
        final Spinner spinner_rateDsss = root.findViewById(R.id.injection_spinner_rateDsss);
        final Spinner spinner_rateOfdm = root.findViewById(R.id.injection_spinner_rateOfdm);
        final TextView textView_mcs = root.findViewById(R.id.injection_textView_mcs);
        final Spinner spinner_mcs = root.findViewById(R.id.injection_spinner_mcs);

        final EditText editText_delay = root.findViewById(R.id.injection_editText_delay);
        final EditText editText_period = root.findViewById(R.id.injection_editText_period);
        final EditText editText_number = root.findViewById(R.id.injection_editText_number);

        final TextView textView_phyInfo = root.findViewById(R.id.injection_textView_phyInfo);
        final TextView textView_leftTime = root.findViewById(R.id.injection_textView_leftTime);

        final Button button_root = root.findViewById(R.id.injection_button_root);
        final Button button_start = root.findViewById(R.id.injection_button_start);

        /* UI更新Observer */
        injectionViewModel.mLogLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView_log.setText(s);
            }
        });

        injectionViewModel.mPhyInfo.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String info) {
                textView_phyInfo.setText(info);
            }
        });
        injectionViewModel.mLeftTime.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String leftTime) {
                textView_leftTime.setText(leftTime);
            }
        });
        injectionViewModel.mState.observe(getViewLifecycleOwner(), new Observer<State>() {
            @Override
            public void onChanged(@Nullable State info) {
                if (state== InjectionFragment.State.idle)
                    button_start.setText("start");
                else
                    button_start.setText("stop");
            }
        });

        /* UI控件监听 */
        textView_browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 弹出式对话框 */
                final String[] availableFiles = imc.listFiles();
                Log.d(TAG, "files list: "+imc.getAbsolutePath());
                Log.d(TAG, "file list: "+availableFiles);
                final AlertDialog dialog_browser = new AlertDialog.Builder(getContext())
                        .setItems(availableFiles, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                imc.loadFile(availableFiles[which]);
                                editText_file.setText(availableFiles[which]);
                                textView_frameType.setText(imc.frameType);
                                textView_addr1.setText(imc.addr1);
                                textView_addr2.setText(imc.addr2);
                                textView_addr3.setText(imc.addr3);
                                textView_macInfo.setText(imc.info);
                            }
                        }).create();
                dialog_browser.show();
            }
        });
        spinner_phy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                final String phy = spinner_phy.getSelectedItem().toString();
                switch (phy) {
                    case "HT":
                        textView_legacyRate.setVisibility(View.GONE);
                        spinner_rateDsss.setVisibility(View.GONE);
                        spinner_rateOfdm.setVisibility(View.GONE);
                        textView_mcs.setVisibility(View.VISIBLE);
                        spinner_mcs.setVisibility(View.VISIBLE);
                        int mcs = Integer.parseInt(spinner_mcs.getSelectedItem().toString());
                        ipc.setHtMcs(mcs, 1);
                        break;
                    case "VHT":
                        textView_legacyRate.setVisibility(View.GONE);
                        spinner_rateDsss.setVisibility(View.GONE);
                        spinner_rateOfdm.setVisibility(View.GONE);
                        textView_mcs.setVisibility(View.VISIBLE);
                        spinner_mcs.setVisibility(View.VISIBLE);
                        mcs = Integer.parseInt(spinner_mcs.getSelectedItem().toString());
                        ipc.setVhtMcs(mcs, 1);
                        break;
                    case "DSSS":
                        textView_legacyRate.setVisibility(View.VISIBLE);
                        spinner_rateDsss.setVisibility(View.VISIBLE);
                        spinner_rateOfdm.setVisibility(View.GONE);
                        textView_mcs.setVisibility(View.GONE);
                        spinner_mcs.setVisibility(View.GONE);
                        int rateIn500kbpsUnits = Math.round(2*Float.parseFloat(spinner_rateDsss.getSelectedItem().toString())) ;
                        ipc.setLegacyRate(rateIn500kbpsUnits);
                        break;
                    case "OFDM":
                        textView_legacyRate.setVisibility(View.VISIBLE);
                        spinner_rateDsss.setVisibility(View.GONE);
                        spinner_rateOfdm.setVisibility(View.VISIBLE);
                        textView_mcs.setVisibility(View.GONE);
                        spinner_mcs.setVisibility(View.GONE);
                        rateIn500kbpsUnits = Math.round(2*Float.parseFloat(spinner_rateOfdm.getSelectedItem().toString())) ;
                        ipc.setLegacyRate(rateIn500kbpsUnits);
                        break;
                    default:
                        Log.d(TAG, "Error phy type");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        spinner_mcs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                final String phy = spinner_phy.getSelectedItem().toString();
                switch (phy) {
                    case "HT":
                        int mcs = Integer.parseInt(spinner_mcs.getSelectedItem().toString());
                        ipc.setHtMcs(mcs, 1);
                        break;
                    case "VHT":
                        mcs = Integer.parseInt(spinner_mcs.getSelectedItem().toString());
                        ipc.setVhtMcs(mcs, 1);
                        break;
                    default:
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        spinner_rateDsss.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int rateIn500kbpsUnits = Math.round(2*Float.parseFloat(spinner_rateDsss.getSelectedItem().toString())) ;
                ipc.setLegacyRate(rateIn500kbpsUnits);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        spinner_rateOfdm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int rateIn500kbpsUnits = Math.round(2*Float.parseFloat(spinner_rateOfdm.getSelectedItem().toString())) ;
                ipc.setLegacyRate(rateIn500kbpsUnits);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        editText_period.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //injectionViewModel.getConfig().getValue().period = Integer.parseInt(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        button_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    bp = new BashProcess("su");
                }catch (IOException e) {
                    Log.d(TAG, "open su error > "+e.toString());
                }
            }
        });

        button_start.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View v) {
                if (bp == null) {
                    Toast.makeText(getContext(), "Please root first", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (injectionViewModel.mState.getValue() == State.idle) {
                    injectionViewModel.mState.postValue(State.injecting);
                    ipc.chanspecStr = editText_chanspecStr.getText().toString();
                    ipc.setInject(Integer.parseInt(editText_delay.getText().toString()),
                            Integer.parseInt(editText_period.getText().toString()),
                            Integer.parseInt(editText_number.getText().toString()));
                    injectionViewModel.mPhyInfo.postValue(ipc.toString());
                    try {
                        String cmd = String.format("nexutil -Iwlan0 -m1 -k%s\n", ipc.chanspecStr);
                        bp.input(cmd);
                        Log.d(TAG, cmd + " > " + bp.getOutput(100));
                        cmd = String.format("injectutil -Iwlan0 -n%d -p%d -d%d -r0x%08x -f%s -l0\n", ipc.number, ipc.period, ipc.delay, ipc.ratespec, imc.getAbsolutePath());
                        bp.input(cmd);
                        Log.d(TAG, cmd + " > " + bp.getOutput(100));
                        if (ipc.number >=0) {
                            timer = new Timer();
                            timer.schedule(new TimerTask() {
                                int seconds2 = (ipc.delay/1000+ipc.number*ipc.period/1000)*2;
                                @Override
                                public void run() {
                                    seconds2 -= 1;
                                    int seconds = seconds2/2;

                                    String leftTime = String.format("%02d:%02d:%02d",
                                            TimeUnit.SECONDS.toHours(seconds),
                                            TimeUnit.SECONDS.toMinutes(seconds) -
                                            TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(seconds)),
                                            seconds -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)));
                                    injectionViewModel.mLeftTime.postValue(leftTime);
                                    if (seconds2<=0) {
                                        timer.cancel();
                                        injectionViewModel.mState.postValue(State.idle);
                                    }
                                }
                            }, 500, 500); //用于显示程序运行时间和通知UI更新
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (timer!=null) {
                        timer.cancel();
                    }
                    injectionViewModel.mState.postValue(State.idle);
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
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
        super.onDestroyView();
    }

}