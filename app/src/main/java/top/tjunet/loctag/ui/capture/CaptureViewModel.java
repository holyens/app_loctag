package top.tjunet.loctag.ui.capture;

import android.annotation.SuppressLint;
import android.text.Html;
import android.text.Spanned;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

@SuppressLint("DefaultLocale")
public class CaptureViewModel extends ViewModel {

    //public final MutableLiveData<String> mText = new MutableLiveData<>();
    public final MutableLiveData<Integer> mCnt = new MutableLiveData<>();
    public final MutableLiveData<Integer> mRssi = new MutableLiveData<>();
    public final MutableLiveData<CaptureFragment.State> mState = new MutableLiveData<>();
    public final MutableLiveData<Spanned> mFilename = new MutableLiveData<>();

    public CaptureViewModel() {
        //mText.setValue("This is capture fragment");
        mCnt.setValue(0);
        mRssi.setValue(-100);
        mState.setValue(CaptureFragment.State.idle);
        mFilename.setValue(Html.fromHtml("01/<font color='red'><b>01</b></font>.txt"));
    }
}
