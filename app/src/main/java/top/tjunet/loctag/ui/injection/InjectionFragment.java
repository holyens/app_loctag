package top.tjunet.loctag.ui.injection;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import java.util.List;

import eu.chainfire.libsuperuser.Shell;
import top.tjunet.loctag.R;

public class InjectionFragment extends Fragment {

    private InjectionViewModel injectionViewModel;
    private static Shell.Interactive rootSession;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        injectionViewModel =
                ViewModelProviders.of(getActivity()).get(InjectionViewModel.class);
        View root = inflater.inflate(R.layout.fragment_injection, container, false);
        // bind widgets
        final TextView textView_log = root.findViewById(R.id.textView_log);

        final Spinner spinner_frame = root.findViewById(R.id.spinner_frame);
        final EditText editText_file = root.findViewById(R.id.editText_file);
        final EditText editText_srcMac = root.findViewById(R.id.editText_srcMac);
        final EditText editText_dstMac = root.findViewById(R.id.editText_dstMac);

        final Spinner spinner_channel = root.findViewById(R.id.spinner_channel);
        final Spinner spinner_bandwidth = root.findViewById(R.id.spinner_bandwidth);
        final Spinner spinner_phy = root.findViewById(R.id.spinner_phy);
        final Spinner spinner_rate = root.findViewById(R.id.spinner_rate);
        final Spinner spinner_mcs = root.findViewById(R.id.spinner_mcs);

        final EditText editText_delay = root.findViewById(R.id.editText_delay);
        final EditText editText_period = root.findViewById(R.id.editText_period);
        final EditText editText_number = root.findViewById(R.id.editText_number);

        final TextView textView_chanspec = root.findViewById(R.id.textView_chanspec);
        final TextView textView_ratespec = root.findViewById(R.id.textView_ratespec);
        final TextView textView_leftTime = root.findViewById(R.id.textView_leftTime);

        final Button button_stop = root.findViewById(R.id.button_stop);
        final Button button_start = root.findViewById(R.id.button_start);

        // set listeners
        injectionViewModel.mLogLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView_log.setText(s);
            }
        });

        injectionViewModel.mConfigLiveData.observe(getViewLifecycleOwner(), new Observer<InjectionConfig>() {
            @Override
            public void onChanged(@Nullable InjectionConfig config) {
                spinner_frame.setSelection(((ArrayAdapter<String>)spinner_frame.getAdapter()).getPosition(String.valueOf(config.frame)));
                if (config.frame.equals("from file")) {
                    editText_file.setVisibility(View.INVISIBLE);
                } else {
                    editText_file.setVisibility(View.VISIBLE);
                    editText_file.setText(config.file);
                }
                editText_srcMac.setText(config.srcMac);
                editText_dstMac.setText(config.dstMac);

                spinner_channel.setSelection(((ArrayAdapter<String>)spinner_channel.getAdapter()).getPosition(String.valueOf(config.channel)));
                spinner_bandwidth.setSelection(((ArrayAdapter<String>)spinner_bandwidth.getAdapter()).getPosition(String.valueOf(config.bandwidth)));
                spinner_phy.setSelection(((ArrayAdapter<String>)spinner_phy.getAdapter()).getPosition(String.valueOf(config.phy)));
                spinner_rate.setSelection(((ArrayAdapter<String>)spinner_rate.getAdapter()).getPosition(String.valueOf(config.rate)));
                spinner_mcs.setSelection(((ArrayAdapter<String>)spinner_mcs.getAdapter()).getPosition(String.valueOf(config.mcs)));


                editText_delay.setText(String.valueOf(config.delay));
                editText_period.setText(String.valueOf(config.period));
                editText_number.setText(String.valueOf(config.number));
            }
        });
        spinner_channel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                injectionViewModel.mConfigLiveData.getValue().channel = Integer.parseInt(spinner_channel.getSelectedItem().toString());

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

        editText_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //injectionViewModel.getConfig().getValue().number = Integer.parseInt(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pref
                openRootShell();
            }
        });

        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootSession.addCommand(new String[] { "ifconfig wlan0 down"}, 0,
                        new Shell.OnCommandResultListener2() {

                            @Override
                            public void onCommandResult(int commandCode, int exitCode, @NonNull List<String> STDOUT, @NonNull List<String> STDERR) {
                                if (exitCode < 0) {
                                    Log.i("Shell","Error executing commands: exitCode " + exitCode);
                                } else {
                                    String x =  "ifconfig wlan0 down"+'\n'+join("\n", STDOUT);
                                    injectionViewModel.appendLog(x);
                                    Log.i("Shell", x);
                                }
                            }
                        });
            }
        });

        return root;
    }
    private static String join(String separator, List<String> input) {
        if (input == null || input.size() <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.size(); i++) {
            sb.append(input.get(i));
            // if not the last item
            if (i != input.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    private void sendRootCommand() {
        rootSession.addCommand(new String[] { "ifconfig wlan0 up"}, 0,
                new Shell.OnCommandResultListener2() {

                    @Override
                    public void onCommandResult(int commandCode, int exitCode, @NonNull List<String> STDOUT, @NonNull List<String> STDERR) {
                        if (exitCode < 0) {
                            Log.i("Shell","Error executing commands: exitCode " + exitCode);
                        } else {
                            String x =  "ifconfig wlan0 up"+'\n'+join("\n", STDOUT);
                            injectionViewModel.appendLog(x);
                            Log.i("Shell", x);
                        }
                    }
                });
        rootSession.addCommand(new String[] { injectionViewModel.getNexutilCmd()}, 0,
                new Shell.OnCommandResultListener2() {

                    @Override
                    public void onCommandResult(int commandCode, int exitCode, @NonNull List<String> STDOUT, @NonNull List<String> STDERR) {
                        if (exitCode < 0) {
                            Log.i("Shell","Error executing commands: exitCode " + exitCode);
                        } else {
                            String x =  injectionViewModel.getNexutilCmd()+'\n'+join("\n", STDOUT);
                            injectionViewModel.appendLog(x);
                            Log.i("Shell", x);
                        }
                    }
                });
        rootSession.addCommand(new String[] { injectionViewModel.getInjectutilCmd()}, 0,
                new Shell.OnCommandResultListener2() {

                    @Override
                    public void onCommandResult(int commandCode, int exitCode, @NonNull List<String> STDOUT, @NonNull List<String> STDERR) {
                        if (exitCode < 0) {
                            Log.i("Shell","Error executing commands: exitCode " + exitCode);
                        } else {
                            String x =  injectionViewModel.getInjectutilCmd()+'\n'+join("\n", STDOUT);
                            injectionViewModel.appendLog(x);
                            Log.i("Shell", x);
                        }
                    }
                });
    }

    private void openRootShell() {
        if (rootSession != null) {
            sendRootCommand();
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
                                // Shell is up: send our first request
                                try {
                                    InjectionConfig config = injectionViewModel.getConfig().getValue();
                                    Log.i("config", String.valueOf(config.channel));
                                    Log.i("config", String.valueOf(config.period));
                                    Log.i("config", String.valueOf(config.number));
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), "invalid channel/period/number", Toast.LENGTH_SHORT).show();
                                }
                                sendRootCommand();
                            }
                        }
                    });
        }
    }
}