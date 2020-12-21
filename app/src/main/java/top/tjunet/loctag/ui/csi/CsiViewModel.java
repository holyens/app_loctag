package top.tjunet.loctag.ui.csi;

import android.annotation.SuppressLint;
import android.text.Html;
import android.text.Spanned;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

@SuppressLint("DefaultLocale")
public class CsiViewModel extends ViewModel {

    //public final MutableLiveData<String> mText = new MutableLiveData<>();
    public final MutableLiveData<Integer> mCnt = new MutableLiveData<>();
    public final MutableLiveData<Integer> mSeq = new MutableLiveData<>();
    public final MutableLiveData<CsiFragment.State> mState = new MutableLiveData<>();
    public final MutableLiveData<Spanned> mFilename = new MutableLiveData<>();

    public CsiViewModel() {
        //mText.setValue("This is capture fragment");
        mCnt.setValue(0);
        mSeq.setValue(-100);
        mState.setValue(CsiFragment.State.idle);
        mFilename.setValue(Html.fromHtml("01/<font color='red'><b>01</b></font>.txt"));
    }
}
