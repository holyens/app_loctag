package top.tjunet.loctag.ui.injection;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InjectionViewModel extends ViewModel {

    public final MutableLiveData<String> mLogLiveData = new MutableLiveData<>();

    public final MutableLiveData<InjectionFragment.State> mState = new MutableLiveData<>();
    public final MutableLiveData<String> mMacInfo = new MutableLiveData<>();
    public final MutableLiveData<String> mPhyInfo = new MutableLiveData<>();
    public final MutableLiveData<String> mLeftTime = new MutableLiveData<>();

    public InjectionViewModel() {
        mLogLiveData.setValue(">");
        mState.setValue(InjectionFragment.State.idle);
        mMacInfo.setValue("");
        mPhyInfo.setValue("");
        mLeftTime.setValue("00:00:00");
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