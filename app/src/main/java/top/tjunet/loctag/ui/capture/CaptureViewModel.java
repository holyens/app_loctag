package top.tjunet.loctag.ui.capture;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CaptureViewModel extends ViewModel {

    public final MutableLiveData<String> mText = new MutableLiveData<>();

    public CaptureViewModel() {
        mText.setValue("This is capture fragment");
    }
}