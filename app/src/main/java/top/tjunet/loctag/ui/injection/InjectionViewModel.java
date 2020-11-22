package top.tjunet.loctag.ui.injection;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Locale;

public class InjectionViewModel extends ViewModel {

    public final MutableLiveData<String> mLogLiveData = new MutableLiveData<>();
    public final MutableLiveData<InjectionConfig> mConfigLiveData = new MutableLiveData<>();



    public InjectionViewModel() {
        mLogLiveData.postValue(">");
        mConfigLiveData.postValue(new InjectionConfig());
    }

    public LiveData<InjectionConfig> getConfig() {
        return mConfigLiveData;
    }

    public void setConfig(InjectionConfig config) {
        mConfigLiveData.setValue(config);
    }
    public String getNexutilCmd() {
        return String.format(Locale.ENGLISH, "nexutil -m1 -k%d/%d", mConfigLiveData.getValue().channel, mConfigLiveData.getValue().bandwidth);
    }

    public String getInjectutilCmd() {
        return String.format(Locale.ENGLISH, "injectutil -p %d -n %d", mConfigLiveData.getValue().period, mConfigLiveData.getValue().number);
    }

    /**
     * Get the content of log textView.
     * @return the content of log textView
     */
    public LiveData<String> getLog() {
        return mLogLiveData;
    }

    /**
     * Append a string to log textView
     * @param s appended string
     */
    public void appendLog(String s) {
        mLogLiveData.setValue(mLogLiveData+s);
    }

    /**
     * Clear the content of log textView
     */
    public void clearLog() {
        mLogLiveData.setValue("");
    }
}