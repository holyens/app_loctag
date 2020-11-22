package top.tjunet.loctag.ui.csi;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CsiViewModel extends ViewModel {

    public final MutableLiveData<String> mText =  new MutableLiveData<>();

    public CsiViewModel() {
        mText.setValue("This is csi fragment");
    }


}